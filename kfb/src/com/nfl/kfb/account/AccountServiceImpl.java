/**
 * 
 */
package com.nfl.kfb.account;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.achieve.AchieveMapper;
import com.nfl.kfb.mapper.admin.AdminMapper;
import com.nfl.kfb.mapper.admin.AdminSubtract;
import com.nfl.kfb.mapper.dungeon.Dungeon;
import com.nfl.kfb.mapper.dungeon.DungeonMapper;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.gamble.GambleProb;
import com.nfl.kfb.mapper.inv.Inv;
import com.nfl.kfb.mapper.inv.InvMapper;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.AttendanceLog;
import com.nfl.kfb.mapper.logging.logs.GambleLog;
import com.nfl.kfb.mapper.logging.logs.LoginLog;
import com.nfl.kfb.mapper.logging.logs.ReviewLog;
import com.nfl.kfb.mapper.logging.logs.TutorialLog;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 *
 */
@Service(value="AccountServiceImpl")
public class AccountServiceImpl extends AbstractKfbService implements AccountService {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
	
	@Autowired
	private AccountMapper accountMapper;
	
	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;
	
	@Autowired
	private AchieveMapper achieveMapper;
	
	@Autowired
	private InvenMapper invenMapper;
	
	@Autowired
	private InvMapper invMapper;
	
	@Autowired
	private RankMapper rankMapper;
	
	@Autowired
	private AdminMapper adminMapper;
	
	@Autowired
	private MailMapper mailMapper;
	
	@Autowired
	private GambleMapper gambleMapper;
	
	@Autowired
	private DungeonMapper dungeonMapper;

	@Override
	public Account getAccount(String appId) {
		return accountMapper.selectAccount(appId);
	}
	
	@Override
	public int[][] getDefaultInvenItem() {
		return DebugOption.DEFAULT_INVEN_ITEM;
	}
	
	@Override
	@Transactional
	public Account createAccount(String appId, int sessionKey, DateUtil dateUtil, String appType, String appVer, String ccode,String img) throws Exception {
		Account account = new Account();
		account.setAppId(appId);
		account.setSessionKey(sessionKey);
		account.setCreDt(dateUtil.getNowEpoch());
		account.setLoginDt(dateUtil.getNowEpoch());
		account.setLoginType(appType);
		account.setLoginVer(appVer);
		account.setCcode(ccode);
		account.setImg(img);
		// INVEN에 아이템 insert
		for (int[] invenItem : getDefaultInvenItem()) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			// 쿼리 직접 실행
			invenMapper.insertIgnoreItem(appId, chId, itemId, itemLv, itemCnt);
		}
		
		accountMapper.insertAccount(account);
		
		return account;
	}

	@Override
	public void loginAccount(DateUtil dateUtil, Account account, String appType, String appVer, String ccode, LoginLog loginLog) {
		// 관리자가 차감 내역을 입력해 놓은것이 있으면 로그인시에 차감함
		AdminSubtract adminSubtract = adminMapper.selectAdminSubtract(account.getAppId());
		if (adminSubtract != null) {
			// addAccountItem() 은 최종값이 (-)일 경우 exception을 발생시키므로 여기선 그냥 일일이 빼자
			account.setGold( Math.max(0, account.getGold() - adminSubtract.getSubGold()) );
			loginLog.setAddGold(loginLog.getAddGold() - adminSubtract.getSubGold());
			account.setBall( Math.max(0, account.getBall() - adminSubtract.getSubBall()) );
			loginLog.setAddBall(loginLog.getAddBall() - adminSubtract.getSubBall());
			account.setPunch( Math.max(0, account.getPunch() - adminSubtract.getSubPunch()) );
			loginLog.setAddPunch(loginLog.getAddPunch() - adminSubtract.getSubPunch());
			
			adminMapper.deleteAdminSubtract(account.getAppId());
		}
		account.setLoginDt(dateUtil.getNowEpoch());
		account.setLoginType(appType);
		account.setLoginVer(appVer);
		account.setCcode(ccode);
		account.regenPunch(dateUtil.getNowEpoch());
		accountMapper.updateLogin(account);
	}

	@Override
	public void setPushOption(String appId, boolean pushOption) {
		accountMapper.updatePushOption(appId, pushOption? 1 : 0);
	}

	@Override
	public boolean isAttendance(int lastAttendanceEpoch, DateUtil dateUtil) {
		int todayStartEpoch = dateUtil.getTodayStartEpoch();
		int yesterdayStartEpoch = dateUtil.getTodayStartEpoch() - DateUtil.ONE_DAY_EPOCH;
		
		// 이미 출석 했음. 변화 없음
		if (lastAttendanceEpoch >= todayStartEpoch) {
			return false;
		}
		
		// 출석이 너무 오래되었음. 다시 시작.
		if (lastAttendanceEpoch < yesterdayStartEpoch) {
			return true;
		}
		
		return true;
	}
	
	@Transactional
	@Override
	public void rewardAttendance(Account account, int chId, DateUtil dateUtil, AttendanceLog attendanceLog, int gamblePoint, int shopVer) throws Exception {
		int attendanceCount = account.getAttCnt();
		int attendanceDateEpoch = account.getAttDt();
		
		int yesterdayStartEpoch = dateUtil.getTodayStartEpoch() - DateUtil.ONE_DAY_EPOCH;
		
		// 출석이 너무 오래되었음. 다시 시작.
		if (attendanceDateEpoch < yesterdayStartEpoch) {
			account.setAttCnt(0);
			account.setAttDt(dateUtil.getNowEpoch());
		}
		else {
			// 출석 날짜 + 1
			account.setAttCnt((attendanceCount + 1) % DebugOption.ATTENDANCE_REWARD.length);
			account.setAttDt(dateUtil.getNowEpoch());
		}
		attendanceLog.setAttCnt(account.getAttCnt());
		
		int attendanceDay = account.getAttCnt();
		int rewardItemId = DebugOption.ATTENDANCE_REWARD[attendanceDay][0];
		int rewardItemCnt = DebugOption.ATTENDANCE_REWARD[attendanceDay][1];
		
		Shop shop = resourceService.getShop(shopVer, rewardItemId);
		
		if (isAccountItem(shop.getItemId())) {
			addAccountItem(dateUtil, account, shop, rewardItemCnt, attendanceLog);
		}
		else if (isInvenItem(shop.getItemId())) {
			Inven newItem = createInvenItemFromShop(account.getAppId(), chId, shop, rewardItemCnt, false);
			Inven existItem = invenMapper.selectItem(account.getAppId(), newItem.getChId(), newItem.getItemId());
			
			attendanceLog.setItemId(shop.getItemId());
			attendanceLog.setItemCnt(rewardItemCnt);
			
			final ITEM_TYPE itemType = DebugOption.getItemType(newItem.getItemId());
			
			switch (itemType) {
			case CHARACTER:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(Math.min(DebugOption.MAX_CHARACTER_LV, existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case SKILL:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemCnt(existItem.getItemCnt() + newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case CONSUME:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemCnt(existItem.getItemCnt() + newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case EQUIP:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(existItem.getItemLv() + newItem.getItemLv());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case PET:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(Math.min(DebugOption.MAX_PET_LV, existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			default:		// 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException("Cannot add item to inven. this is not inven item");
			}
		}
		
		// ACCOUNT는 무조건 바뀌므로 update
		accountMapper.updateAttendanceRw(account);
		
		// 겜블 포인트 얻음
		if (gamblePoint != 0) {
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}
	}

	@Override
	public List<Integer> getAchieveList(String appId, int week) {
		return achieveMapper.selectAchieve(appId, week);
	}

	@Transactional
	@Override
	public AchieveRewardResponse rewardAchieve(DateUtil dateUtil, Account account, int week, int achieveIdx, int chId
			, Shop rewardItem, int rewardValue, AchieveLog achieveLog) throws DuplicateKeyException, Exception {
		achieveMapper.insertAchieve(account.getAppId(), week, achieveIdx, dateUtil.getNowEpoch());
		
		AchieveRewardResponse achieveRewardResponse = new AchieveRewardResponse(ReturnCode.SUCCESS);
		
		if (isAccountItem(rewardItem.getItemId())) {
			addAccountItem(dateUtil, account, rewardItem, rewardValue, achieveLog);
			accountMapper.updateAccountItem(account);
			achieveRewardResponse.setITID(rewardItem.getItemId());
			achieveRewardResponse.setITCNT(rewardValue);
		}
		else if (isInvenItem(rewardItem.getItemId())) {
			Inven newItem = createInvenItemFromShop(account.getAppId(), chId, rewardItem, rewardValue, false);
			Inven existItem = invenMapper.selectItem(account.getAppId(), newItem.getChId(), newItem.getItemId());
			
			achieveLog.setItemId(rewardItem.getItemId());
			achieveLog.setItemCnt(rewardValue);
			
			achieveRewardResponse.setITID(newItem.getItemId());
			achieveRewardResponse.setITCNT(rewardValue);
			
			final ITEM_TYPE itemType = DebugOption.getItemType(newItem.getItemId());
			
			switch (itemType) {
			case CHARACTER:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(Math.min(DebugOption.MAX_CHARACTER_LV, existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case SKILL:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemCnt(existItem.getItemCnt() + newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case CONSUME:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemCnt(existItem.getItemCnt() + newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case EQUIP:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(existItem.getItemLv() + newItem.getItemLv());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case PET:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(Math.min(DebugOption.MAX_PET_LV, existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			default:		// 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException("Cannot add item to inven. this is not inven item");
			}
		}
		
		achieveRewardResponse.setGOT(false);
		achieveRewardResponse.setWK(week);
		achieveRewardResponse.setGD(account.getGold());
		achieveRewardResponse.setBL(account.getBall());
		achieveRewardResponse.setPN(account.getPunch());
		achieveRewardResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
		
		return achieveRewardResponse;
	}

	@Override
	public List<Inven> getInven(String appId) {
		return invenMapper.selectAllItem(appId);
	}

	@Override
	public Map<String, Object> getInvenInfoMap(List<Inven> allInven) {
		int[][] invenItemList = new int[allInven.size()][];
		for (int i=0; i<allInven.size(); i++) {
			Inven invenItem = allInven.get(i);
			invenItemList[i] = new int[4];
			invenItemList[i][0] = invenItem.getChId();
			invenItemList[i][1] = invenItem.getItemId();
			invenItemList[i][2] = invenItem.getItemLv();
			invenItemList[i][3] = invenItem.getItemCnt();
		}
		return getInvenInfoMap(invenItemList);
	}
	
	@Override
	@SuppressWarnings("unused")
	public Map<String, Object> getInvenInfoMap(int[][] invenItemList) {
		Map<String, Object> invenItemMap = new HashMap<String, Object>();
		
		// 아이템을 분류
		List<int[]> invenInfoChaList = new ArrayList<int[]>();
		List<int[]> invenInfoEquipList = new ArrayList<int[]>();
		List<int[]> invenInfoSkillList = new ArrayList<int[]>();
		List<int[]> invenInfoPetList = new ArrayList<int[]>();
		List<int[]> invenInfoConsumeList = new ArrayList<int[]>();
		
		for (int[] invenItem : invenItemList) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			ITEM_TYPE itemType = DebugOption.getItemType(itemId);
			switch (itemType) {
			case CONSUME:			invenInfoConsumeList.add(invenItem);			break;
			case PET:				invenInfoPetList.add(invenItem);				break;
			case CHARACTER:			invenInfoChaList.add(invenItem);				break;
			case EQUIP:				invenInfoEquipList.add(invenItem);				break;
			case SKILL:				invenInfoSkillList.add(invenItem);				break;
			default:
				break;
			}
		}
		
		// 소모아이템
		List<Map<String, Object>> consumeItemData = new ArrayList<Map<String,Object>>();
		for (int[] invenItem : invenInfoConsumeList) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			Map<String, Object> itemInfo = getInvenInfoConsume(itemId, itemCnt);
			consumeItemData.add(itemInfo);
		}
		invenItemMap.put("IT", consumeItemData);
		
		// 소환수아이템
		List<Map<String, Object>> petItemData = new ArrayList<Map<String,Object>>();
		for (int[] invenItem : invenInfoPetList) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			Map<String, Object> itemInfo = getInvenInfoPet(itemId, itemLv);
			petItemData.add(itemInfo);
		}
		invenItemMap.put("PET", petItemData);
		
		// 캐릭터
		Map<Integer, Map<String, Object>> characterItemMap = new HashMap<Integer, Map<String,Object>>();
		Map<Integer, List<Map<String, Object>>> equipItemMap = new HashMap<Integer, List<Map<String,Object>>>();
		Map<Integer, List<Map<String, Object>>> skillItemMap = new HashMap<Integer, List<Map<String,Object>>>();
		for (int[] invenItem : invenInfoChaList) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			characterItemMap.put(new Integer(chId), getInvenInfoCharacter(itemId, itemLv));
			equipItemMap.put(new Integer(chId), new ArrayList<Map<String,Object>>());
			skillItemMap.put(new Integer(chId), new ArrayList<Map<String,Object>>());
		}
		
		// 장비
		for (int[] invenItem : invenInfoEquipList) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			List<Map<String,Object>> equipItemData = equipItemMap.get(new Integer(chId));
			if (equipItemData == null)		// 캐릭터는 없는데 장비만 있네?
				continue;
			
			equipItemData.add(getInvenInfoEquip(itemId, itemLv));
		}
		
		// 스킬
		for (int[] invenItem : invenInfoSkillList) {
			int chId = invenItem[0];
			int itemId = invenItem[1];
			int itemLv = invenItem[2];
			int itemCnt = invenItem[3];
			
			List<Map<String,Object>> skillItemData = skillItemMap.get(new Integer(chId));
			if (skillItemData == null)		// 캐릭터는 없는데 스킬만 있네?
				continue;
			
			skillItemData.add(getInvenInfoSkill(itemId, itemLv, itemCnt));
		}
		
		List<Map<String, Object>> characterData = new ArrayList<Map<String,Object>>();
		for (Integer chaId : characterItemMap.keySet()) {
			Map<String, Object> oneCharacterData = new HashMap<String, Object>();
			oneCharacterData.putAll(characterItemMap.get(chaId));
			oneCharacterData.put("EQ", equipItemMap.get(chaId));
			oneCharacterData.put("SKL", skillItemMap.get(chaId));
			
			characterData.add(oneCharacterData);
		}
		
		invenItemMap.put("CH", characterData);
		
		return invenItemMap;
	}
	
	/**
	 * CH.ID, CH.LV
	 * @param itemId
	 * @param lv
	 * @return
	 */
	private Map<String, Object> getInvenInfoCharacter(int itemId, int lv) {
		Map<String, Object> consumItemMap = new HashMap<String, Object>();
		consumItemMap.put("ID", itemId);
		consumItemMap.put("LV", lv);
		return consumItemMap;
	}
	
	/**
	 * EQ.ID, EQ.LV
	 * @param itemId
	 * @param lv
	 * @return
	 */
	private Map<String, Object> getInvenInfoEquip(int itemId, int lv) {
		Map<String, Object> consumItemMap = new HashMap<String, Object>();
		consumItemMap.put("ID", itemId);
		consumItemMap.put("LV", lv);
		return consumItemMap;
	}
	
	/**
	 * SKL.ID, SKL.LV, SKL.CNT
	 * @param itemId
	 * @param lv
	 * @param cnt
	 * @return
	 */
	private Map<String, Object> getInvenInfoSkill(int itemId, int lv, int cnt) {
		Map<String, Object> consumItemMap = new HashMap<String, Object>();
		consumItemMap.put("ID", itemId);
		consumItemMap.put("LV", lv);
		consumItemMap.put("CNT", cnt);
		return consumItemMap;
	}
	
	/**
	 * PET.ID, PET.LV
	 * @param itemId
	 * @param lv
	 * @return
	 */
	private Map<String, Object> getInvenInfoPet(int itemId, int lv) {
		Map<String, Object> consumItemMap = new HashMap<String, Object>();
		consumItemMap.put("ID", itemId);
		consumItemMap.put("LV", lv);
		return consumItemMap;
	}
	
	/**
	 * IT.ID, IT.CNT
	 * @param itemId
	 * @param cnt
	 * @return
	 */
	private Map<String, Object> getInvenInfoConsume(int itemId, int cnt) {
		Map<String, Object> consumItemMap = new HashMap<String, Object>();
		consumItemMap.put("ID", itemId);
		consumItemMap.put("CNT", cnt);
		return consumItemMap;
	}
	
	@Transactional
	@Override
	public TutorialResponse rewardTutorial(DateUtil dateUtil, Account account, Shop item, int itemCnt, TutorialLog tutorialLog) {
		if (!isAccountItem(item.getItemId())) {
			// 튜토리얼 보상 아이템은 CH_ID를 받지않기때문에 무조건 ACCOUNT 아이템이어야 함
			throw new RuntimeException("Tutorial reward must be accountItem");
		}
		
		addAccountItem(dateUtil, account, item, itemCnt, tutorialLog);
		
		account.setTuto(1);
		// ACCOUNT는 무조건 바뀌므로 update
		accountMapper.updateTutorialRw(account);
		
		TutorialResponse tutorialResponse = new TutorialResponse(ReturnCode.SUCCESS);
		tutorialResponse.setITID(item.getItemId());
		tutorialResponse.setITCNT(itemCnt);
		tutorialResponse.setGD(account.getGold());
		tutorialResponse.setBL(account.getBall());
		tutorialResponse.setPN(account.getPunch());
		tutorialResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
		
		return tutorialResponse;
	}
	
	@Transactional
	@Override
	public ReviewResponse rewardReview(DateUtil dateUtil, Account account, Shop item, int itemCnt, ReviewLog reviewLog) {
		if (!isAccountItem(item.getItemId())) {
			// 리뷰보상 아이템은 CH_ID를 받지않기때문에 무조건 ACCOUNT 아이템이어야 함
			throw new RuntimeException("Review reward must be accountItem");
		}
		
		addAccountItem(dateUtil, account, item, itemCnt, reviewLog);
		
		account.setRev(1);
		// ACCOUNT는 무조건 바뀌므로 update
		accountMapper.updateReviewRw(account);
		
		ReviewResponse reviewResponse = new ReviewResponse(ReturnCode.SUCCESS);
		reviewResponse.setITID(item.getItemId());
		reviewResponse.setITCNT(itemCnt);
		reviewResponse.setGD(account.getGold());
		reviewResponse.setBL(account.getBall());
		reviewResponse.setPN(account.getPunch());
		reviewResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
		
		return reviewResponse;
	}
	
	@Override
	public KakaoTokenResponse kakaoTokenCheck(String access_token, String user_id, String sdkver) throws ClientProtocolException, IOException, URISyntaxException {
		DefaultHttpClient httpclient = null;
		try {
			URIBuilder builder = new URIBuilder();
			builder.setScheme("https").setHost("api.kakao.com").setPath("/v1/token/check.json")
						.setParameter("user_id", user_id)
						.setParameter("client_id", DebugOption.KAKAO_CLINET_ID)
						.setParameter("sdkver", sdkver)
						.setParameter("access_token", access_token);
			URI uri = builder.build();
//			logger.debug("KakaoTokenCheck uri={}", uri);
			HttpGet httpGet = new HttpGet(uri);
			
			httpclient = new DefaultHttpClient();
			HttpParams httpParams = httpclient.getParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
			HttpConnectionParams.setSoTimeout(httpParams, 5000);
			
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String responseJson = EntityUtils.toString(entity);
//			logger.info("KakaoTokenCheck uri={}, Response={}", uri, responseJson);
			EntityUtils.consume(entity);
			
			try {
				KakaoTokenCheck kakaoTokenCheck = parseJson(KakaoTokenCheck.class, responseJson);
				
				KakaoTokenResponse kakaoTokenResponse = new KakaoTokenResponse(ReturnCode.SUCCESS);
				kakaoTokenResponse.setSTATUS(kakaoTokenCheck.getStatus());

				return kakaoTokenResponse;
			} catch (Exception e) {
				KakaoTokenResponse kakaoTokenErrorResponse = new KakaoTokenResponse(ReturnCode.INVALID_KAKAO_TOKEN);
				return kakaoTokenErrorResponse;
			}
		} finally {
			if (httpclient != null && httpclient.getConnectionManager() != null) {
				httpclient.getConnectionManager().shutdown();
			}
		}
	}
	
	@Override
	public Inv getInv(String appId) {
		return invMapper.selectInv(appId);
	}
	
	@Override
	public void withdraw(DateUtil dateUtil, String appId) {
		try {
			accountMapper.deleteAccount(appId);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			invenMapper.deleteInven(appId);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			invMapper.deleteInv(appId);
			
			// 카카오에서 31일 제한에 걸리므로 따로 지울 필요는 없다
			// 그리고 재설치했을때 이전에 초대한 시각을 가지고 있으면 좋으므로 지우지 않음
//			invMapper.deleteInvList(appId);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			achieveMapper.deleteAllAchieve(appId, dateUtil.getThisWeek());
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			mailMapper.deleteAllMail(appId);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			mailMapper.deleteAllPunch(appId);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			rankMapper.removeRank(appId, dateUtil.getThisWeek());
			rankMapper.removeRank(appId, dateUtil.getThisWeek()-1);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			rankMapper.removeGateRank(appId, dateUtil.getThisWeek());
			rankMapper.removeGateRank(appId, dateUtil.getThisWeek()-1);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
		try {
			accountMapper.deletePushToken(appId);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
	}
	
	@Override
	public void registerPushToken(String appId, String loginType,
			String loginVer, String ccode, String pushToken, int nowEpoch) {
		// ignore db exception
		try {
			accountMapper.insertOrUpdatePushToken(appId, pushToken, nowEpoch, loginType, loginVer, ccode);
		} catch (Exception e) {
			logger.error("ID="+appId, e);
		}
	}
	
	@Override
	public void updatePushToken(Account account) {
		// ignore db exception
		try {
			accountMapper.updatePushTokenLogin(account.getAppId(), account.getLoginDt(), account.getLoginType(), account.getLoginVer(), account.getCcode());
		} catch (Exception e) {
			logger.error("ID="+account.getAppId(), e);
		}
	}
	
	@Override
	public boolean isBlockAccount(DateUtil dateUtil, String appId) {
		return accountMapper.countBlockAccount(appId, dateUtil.getNowEpoch()) > 0;
	}
	
	private GambleProb removeRandomGambleProb(Random random, List<GambleProb> gambleProbList, boolean missProb) {
		// sum of prob
		int maxProb = 0;
		for (GambleProb gambleProb : gambleProbList) {
			int tmpProb = missProb? gambleProb.getMissProb() : gambleProb.getProbability();
			maxProb += tmpProb;
		}
		
		int randomInteger = random.nextInt(maxProb);
		
		// find
		int prob = 0;
		Iterator<GambleProb> gambleProbIter = gambleProbList.iterator();
		while (gambleProbIter.hasNext()) {
			GambleProb gambleProb = gambleProbIter.next();
			
			int tmpProb = missProb? gambleProb.getMissProb() : gambleProb.getProbability();
			if (tmpProb <= 0)
				continue;
			
			prob += tmpProb;
			if (randomInteger < prob) {
				gambleProbIter.remove();
				return gambleProb;
			}
		}
		
		return null;
	}
	
	@Transactional
	@Override
	public GambleResponse gamble(DateUtil dateUtil, Account account, int chId, List<GambleProb> gambleProbList, GambleLog gambleLog, int shopVer) {
		int gamblePoint = gambleMapper.selectGamblePoint(account.getAppId());
		
		// 겜블 포인트 확인
		if (gamblePoint < DebugOption.GAMBLE_POINT_TO_OPEN) {
			throw new RuntimeException("Not enough gamble point. GameblePoint=" + gamblePoint);
		}
		
		GambleResponse gambleResponse = new GambleResponse(ReturnCode.SUCCESS);
		
		Random random = new Random();
		
		GambleProb selectedGambleProb = removeRandomGambleProb(random, gambleProbList, false);
		
		logger.debug("SELECTED_GAMBLE GAMBLE_ID[{}] ITEM_ID[{}] ITEM_CNT[{}] PROB[{}]"
				, selectedGambleProb.getGambleProbId(), selectedGambleProb.getItemId(), selectedGambleProb.getItemCnt(), selectedGambleProb.getProbability());
		
		for (int i=0; i<DebugOption.GAMBLE_MISSED_CNT; i++) {
			GambleProb missedGambleProb = removeRandomGambleProb(random, gambleProbList, true);
			logger.debug("MISSED_GAMBLE GAMBLE_ID[{}] ITEM_ID[{}] ITEM_CNT[{}] PROB[{}]"
					, missedGambleProb.getGambleProbId(), missedGambleProb.getItemId(), missedGambleProb.getItemCnt(), missedGambleProb.getMissProb());
			gambleResponse.addMissItem(missedGambleProb.getItemId(), missedGambleProb.getItemCnt());
		}
		
		final int selectedItemId = selectedGambleProb.getItemId();
		final int selectedItemCnt = selectedGambleProb.getItemCnt();
		Shop selectedItem = resourceService.getShop(shopVer, selectedItemId);
		
		if (selectedItem == null) {
			throw new RuntimeException("Unknown itemId=" + selectedItemId);
		}
		
		if (isAccountItem(selectedItem.getItemId())) {
			addAccountItem(dateUtil, account, selectedItem, selectedItemCnt, gambleLog);
			accountMapper.updateAccountItem(account);
			
			gambleResponse.setITID(selectedItem.getItemId());
			gambleResponse.setITCNT(selectedItemCnt);
		}
		else if (isInvenItem(selectedItem.getItemId())) {
			Inven newItem = createInvenItemFromShop(account.getAppId(), chId, selectedItem, selectedItemCnt, false);
			Inven existItem = invenMapper.selectItem(account.getAppId(), newItem.getChId(), newItem.getItemId());
			
			gambleLog.setItemId(selectedItem.getItemId());
			gambleLog.setItemCnt(selectedItemCnt);
			
			gambleResponse.setITID(newItem.getItemId());
			gambleResponse.setITCNT(selectedItemCnt);
			
			final ITEM_TYPE itemType = DebugOption.getItemType(newItem.getItemId());
			
			switch (itemType) {
			case CHARACTER:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(Math.min(DebugOption.MAX_CHARACTER_LV, existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case SKILL:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemCnt(existItem.getItemCnt() + newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case CONSUME:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemCnt(existItem.getItemCnt() + newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case EQUIP:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(existItem.getItemLv() + newItem.getItemLv());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			case PET:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				}
				else {
					existItem.setItemLv(Math.min(DebugOption.MAX_PET_LV, existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException("updateItem affectedRow is not 1. something wrong.");
				}
				break;
				
			default:		// 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException("Cannot add item to inven. this is not inven item");
			}
		}
		
		
		
		// update gamble point
		gambleMapper.increaseGamblePoint(account.getAppId(), -DebugOption.GAMBLE_POINT_TO_OPEN);
		gamblePoint -= DebugOption.GAMBLE_POINT_TO_OPEN;
		
		gambleResponse.setGD(account.getGold());
		gambleResponse.setBL(account.getBall());
		gambleResponse.setPN(account.getPunch());
		gambleResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
		gambleResponse.setGP(gamblePoint);
		
		gambleLog.setGamblePoint(gamblePoint);
		gambleLog.setNowGold(account.getGold());
		gambleLog.setNowBall(account.getBall());
		gambleLog.setNowPunch(account.getPunch());
		
		return gambleResponse;
	}
	
	
	@Override
	public List<Account> getAccountOfRecommand(String Appid,int count)
	{
		return accountMapper.selectFriendlyRecommendList(Appid,count);
	}
	
	
	@Override
	public List<Account> getAccountList(int count) {
		List<Account> selectAccountList = accountMapper.selectAccountList(count);
		return selectAccountList;
	}

	@Override
	public int getGamblePoint(String appId) {
		return gambleMapper.selectGamblePoint(appId);
	}

	@Override
	public Dungeon getDungeon(String appId) {
		return dungeonMapper.selectDungeon(appId);
	}

	@Override
	public void insertDungeon(Dungeon dungeon) {
		dungeonMapper.insertDungeon(dungeon);
	}
	
	@Override
	public void updateDungeon(Dungeon dungeon) {
		dungeonMapper.updateDungeon(dungeon);
	}
	
	@Transactional
	@Override
	public boolean changeNickname(String nickname,String appId){
//		int count = accountMapper.countOfNickname(nickname);
//		if(count > 0){
//			return false;
//		}
		try{
			accountMapper.updateNickname(nickname, appId);
		}catch(Exception exception){
			try {
				nickname = new String(nickname.getBytes("GB2312"),"GB2312");
			} catch (UnsupportedEncodingException e) {
				nickname = appId.substring(0,10);
			}
			accountMapper.updateNickname(nickname, appId);
		}
		return true;
	}
	

	
	
}
