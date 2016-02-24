/**
 * 
 */
package com.nfl.kfb.task;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import javax.annotation.PreDestroy;

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
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.AttendanceLog;
import com.nfl.kfb.mapper.logging.logs.GambleLog;
import com.nfl.kfb.mapper.logging.logs.LoginLog;
import com.nfl.kfb.mapper.logging.logs.ReviewLog;
import com.nfl.kfb.mapper.logging.logs.TutorialLog;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.task.BaseTask;
import com.nfl.kfb.mapper.task.BaseTaskMapper;
import com.nfl.kfb.mapper.task.RoleTask;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_CATEGORY;
import com.nfl.kfb.util.DebugOption.TASK_STATE;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 * 
 */
@Service(value = "TaskServiceImpl")
public class TaskServiceImpl extends AbstractKfbService implements TaskService {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskServiceImpl.class);

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

	@Autowired
	private BaseTaskMapper baseTaskMapper;

	@Override
	public List<RoleTask> getTaskList(String appId) {
		List<RoleTask> selectRoleTaskByAppId = baseTaskMapper
				.selectRoleTaskByAppId(appId);
		return selectRoleTaskByAppId;
	}

	@Override
	public void giveItem(Shop rewardItem, Account account, int rewardValue,
			GameLog achieveLog, int chId) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		if (isAccountItem(rewardItem.getItemId())) {
			addAccountItem(dateUtil, account, rewardItem, rewardValue,
					achieveLog);
			accountMapper.updateAccountItem(account);
		} else if (isInvenItem(rewardItem.getItemId())) {
			Inven newItem = createInvenItemFromShop(account.getAppId(), chId,
					rewardItem, rewardValue, false);
			Inven existItem = invenMapper.selectItem(account.getAppId(),
					newItem.getChId(), newItem.getItemId());

			achieveLog.setItemId(rewardItem.getItemId());
			achieveLog.setItemCnt(rewardValue);

			final ITEM_TYPE itemType = DebugOption.getItemType(newItem
					.getItemId());

			switch (itemType) {
			case CHARACTER:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					existItem.setItemLv(Math.min(DebugOption.MAX_CHARACTER_LV,
							existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
				}
				break;

			case SKILL:
				
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					existItem.setItemCnt(existItem.getItemCnt()
							+ newItem.getItemCnt());
					//existItem.setItemLv(1);
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
				}
				break;

			case CONSUME:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					existItem.setItemCnt(existItem.getItemCnt()
							+ newItem.getItemCnt());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
				}
				break;

			case EQUIP:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					existItem.setItemLv(existItem.getItemLv()
							+ newItem.getItemLv());
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
				}
				break;

			case PET:
				if (existItem == null) {
					invenMapper.insertItem(newItem);
				} else {
					existItem.setItemLv(Math.min(DebugOption.MAX_PET_LV,
							existItem.getItemLv() + newItem.getItemLv()));
					int affectedRow = invenMapper.updateItem(existItem);
					if (affectedRow != 1)
						throw new RuntimeException(
								"updateItem affectedRow is not 1. something wrong.");
				}
				break;

			default: // 그외의 아이템은 INVEN에 들어가는 아이템이 아님
				throw new RuntimeException(
						"Cannot add item to inven. this is not inven item");
			}
		}

	}
	
	@Override
	public void deleteTask(final TASK_CATEGORY task) {
		int[] array = resourceService.getBaseTaskList().stream().filter(new Predicate<BaseTask>() {
			@Override
			public boolean test(BaseTask t) {
				if(t.getTaskCategory() == task.getValue()){
					return true;
				}
				return false;
			}
		}).mapToInt(new ToIntFunction<BaseTask>() {
			@Override
			public int applyAsInt(BaseTask value) {
				return value.getTaskId();
			}
		}).toArray();
		HashSet<Integer> set = new HashSet<Integer>();
		for (Integer integer : array) {
			if(integer != 1010){
				set.add(integer);
			}
			
		}
		if(set.size() > 0){
			baseTaskMapper.deleteTaskByTaskId(set);
		}
	}

}
