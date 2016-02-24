/**
 * 
 */
package com.nfl.kfb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 7. 19.
 *
 */
public abstract class AbstractKfbService {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractKfbService.class);
	
	@Autowired
	TaskController taskController;
	
	protected void addBall(Account account, int ball) {
		int newBall = account.getBall() + ball;
		account.setBall(newBall);
	}
	
	protected void addGold(Account account, int gold) {
		int newGold = account.getGold() + gold;
		account.setGold(newGold);
		if(gold > 0)
		{
			//tasktask
			List<TaskDetail> newTaskDetailList = taskController.newTaskDetailList(TASK_TYPE.GET_ALL_GOLD, gold);
			taskController.submitTaskDetailInfo(account.getAppId(), newTaskDetailList);
		}
	}
	
	protected boolean isAccountItem(int itemId) {
		ITEM_TYPE itemType = DebugOption.getItemType(itemId);
		switch (itemType) {
		case BALL:
		case GOLD:
		case PUNCH:
			return true;
		default:
			return false;
		}
	}
	
	protected boolean isInvenItem(int itemId) {
		ITEM_TYPE itemType = DebugOption.getItemType(itemId);
		switch (itemType) {
		case CHARACTER:
		case CONSUME:
		case EQUIP:
		case PET:
		case SKILL:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * ACCOUNT관련 아이템값을 Account객체에 적용(값만 적용됨)<br>
	 * @param dateUtil
	 * @param account
	 * @param item
	 * @param itemCnt
	 * @param gameLog
	 * @return			Account값 변경이 있는가
	 */
	protected boolean addAccountItem(DateUtil dateUtil, Account account, Shop item, int itemCnt, GameLog gameLog) {
		if (account.getBall() < 0 || account.getGold() < 0 || account.getPunch() < 0)
			throw new RuntimeException("Wrong now accountItem. NOW_GOLD=" + account.getGold() + ", NOW_BALL=" + account.getBall() + ", NOW_PUNCH=" + account.getPunch());
		
		final int addCnt = item.getCnt() * itemCnt;
		final ITEM_TYPE itemType = DebugOption.getItemType(item.getItemId());
		
		switch (itemType) {
		case BALL:
			addBall(account, addCnt);
			if (account.getBall() < 0)
				throw new RuntimeException("Couldnot add ACCOUNT.BL=" + addCnt + ", ACCOUNT.BL < 0");
			if (gameLog != null) {
				gameLog.setItemId(item.getItemId());
				gameLog.setItemCnt(gameLog.getItemCnt() + itemCnt);
				gameLog.setAddBall(gameLog.getAddBall() + addCnt);
				gameLog.setNowBall(account.getBall());
				gameLog.setNowGold(account.getGold());
				gameLog.setNowPunch(account.getPunch());
			}
			return true;
			
		case GOLD:
			addGold(account, addCnt);
			if (account.getGold() < 0)
				throw new RuntimeException("Couldnot add ACCOUNT.GD=" + addCnt + ", ACCOUNT.BL < 0");
			if (gameLog != null) {
				gameLog.setItemId(item.getItemId());
				gameLog.setItemCnt(gameLog.getItemCnt() + itemCnt);
				gameLog.setAddGold(gameLog.getAddGold() + addCnt);
				gameLog.setNowBall(account.getBall());
				gameLog.setNowGold(account.getGold());
				gameLog.setNowPunch(account.getPunch());
			}
			return true;
			
		case PUNCH:
			account.regenPunch(dateUtil.getNowEpoch());
			account.setPunch(account.getPunch() + addCnt);
			if (account.getPunch() < 0)
				throw new RuntimeException("Couldnot add ACCOUNT.PUNCH=" + addCnt + ", ACCOUNT.PUNCH < 0");
			if (gameLog != null) {
				gameLog.setItemId(item.getItemId());
				gameLog.setItemCnt(gameLog.getItemCnt() + itemCnt);
				gameLog.setAddPunch(gameLog.getAddPunch() + addCnt);
				gameLog.setNowBall(account.getBall());
				gameLog.setNowGold(account.getGold());
				gameLog.setNowPunch(account.getPunch());
			}
			return true;
			
		default:
			throw new RuntimeException("Cannot add item to account. this is not account item");
		}			
	}
	
	protected Inven createInvenItemFromShop(String appId, int chId, Shop shop, int cnt, boolean isBuy) {
		final ITEM_TYPE itemType = DebugOption.getItemType(shop.getItemId());
		
		final int itemCnt = shop.getCnt() * cnt;
		
		switch (itemType) {
		case CHARACTER:
			if (isBuy && itemCnt > DebugOption.MAX_CHARACTER_LV)
				throw new RuntimeException("addCnt exceed MAX_CHARACTER_LV");
			return new Inven(appId, shop.getItemId(), shop.getItemId(), itemCnt, 1);
			
		case SKILL:
			return new Inven(appId, chId, shop.getItemId(), isBuy? Math.min(DebugOption.MAX_SKILL_LV, itemCnt) : 0, itemCnt);
			
		case CONSUME:
			return new Inven(appId, Inven.CHARACTER_ID_FOR_COMMON_ITEM, shop.getItemId(), 1, itemCnt);
			
		case EQUIP:
			if (isBuy && itemCnt > DebugOption.MAX_EQUIP_LV)
				throw new RuntimeException("addCnt exceed MAX_EQUIP_LV");
			return new Inven(appId, chId, shop.getItemId(), isBuy? itemCnt : 1, 1);
			
		case PET:
			if (isBuy && itemCnt > DebugOption.MAX_PET_LV)
				throw new RuntimeException("addCnt exceed MAX_PET_LV");
			return new Inven(appId, Inven.CHARACTER_ID_FOR_COMMON_ITEM, shop.getItemId(), isBuy? itemCnt : 1, 1);
			
		default:		// 그외의 아이템은 INVEN에 들어가는 아이템이 아님
			throw new RuntimeException("Cannot create invenItem from shop. this is not inven item");
		}
	}

	protected Set<String> asFidSet(String ... FID) {
		Set<String> appIdSet = new HashSet<String>();
		if (FID == null) {
			return appIdSet;
		}
		
		for (String fAppId : FID) {
			if (fAppId != null && fAppId.trim().length()>0) {
				appIdSet.add(fAppId);
			}
		}
		return appIdSet;
	}
	
	protected <T> T parseJson(Class<T> clazz, String jsonString) throws Exception {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			T returnClazz = objectMapper.readValue(jsonString, clazz);
			return returnClazz;
		} catch (Exception e) {
			logger.error(jsonString, e);
			throw e;
		}
	}

}
