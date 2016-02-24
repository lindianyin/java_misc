/**
 * 
 */
package com.nfl.kfb.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mail.MailService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.account.RoleReward;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.GameLog.GAMELOG_TYPE;
import com.nfl.kfb.mapper.logging.LoggingMapper;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.DeleteDailyResetTask;
import com.nfl.kfb.mapper.logging.logs.DeleteWeeklyResetTask;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.task.BaseTask;
import com.nfl.kfb.mapper.task.BaseTaskMapper;
import com.nfl.kfb.mapper.task.RoleTask;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.ResourceService.GAME_OPTION;
import com.nfl.kfb.shop.BuyResponse;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.REWARD_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_CATEGORY;
import com.nfl.kfb.util.DebugOption.TASK_STATE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;
import com.sun.glass.ui.android.Activity;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 * 
 */
@Controller
@RequestMapping(value = "/task", method = { RequestMethod.POST,
		RequestMethod.GET })
public class TaskController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory
			.getLogger(TaskController.class);

	private static final String URL_GET_TASK_LIST = "/gettasklist";

	private static final String URL_GET_TASK_REWARD = "/gettaskreward";

	private static final String URL_SUBMIT_TASK = "/submitTask";

	private static final String URL_RESET_DAILY = "/resetdaily";

	private static final String URL_GET_GIFT = "/gift";//领取礼包

	@Autowired
	@Qualifier("MailServiceImpl")
	private MailService mailService;

	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	@Qualifier("TaskServiceImpl")
	private TaskService taskService;

	@Autowired
	private InvenMapper invenMapper;

	@Autowired
	private BaseTaskMapper baseTaskMapper;

	@Autowired
	private LoggingMapper loggingMapper;

	@Autowired
	private AccountMapper accountMapper;
	
	
	private BaseTask rt;

	private Integer integer;

	@RequestMapping(value = URL_RESET_DAILY)
	@ResponseBody
	public JsonResponse resetDaily() {
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		deleteTask(TASK_CATEGORY.DAILY_TASK);
		deleteTask(TASK_CATEGORY.ACTIVITY);
		return jr;
	}

	@RequestMapping(value = URL_GET_TASK_LIST)
	@ResponseBody
	public JsonResponse getTaskList(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			configDailyResetTask(ID, TASK_CATEGORY.ACHIEVE);
			configDailyResetTask(ID, TASK_CATEGORY.DAILY_TASK);
			configDailyResetTask(ID, TASK_CATEGORY.ACTIVITY);
			List<RoleTask> taskList = taskService.getTaskList(ID);
			JsonResponse resp = new JsonResponse(ReturnCode.SUCCESS);
			final List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();
			List<BaseTask> bt = resourceService.getBaseTaskList();
			for (int i = 0; i < taskList.size(); i++) {
				final int taskId = taskList.get(i).getTaskId();
				if (taskList.get(i).getTaskState() == TASK_STATE.OVER
						.getValue()) {
					int taskCategory = bt.stream()
							.filter((new Predicate<BaseTask>() {

								@Override
								public boolean test(BaseTask t) {
									if (t.getTaskId() == taskId) {
										return true;
									}
									return false;
								}

							})).findFirst().get().getTaskCategory();
					if (taskCategory != TASK_CATEGORY.ACHIEVE.getValue()) {
						continue;
					}
				}
				RoleTask t = taskList.get(i);
				TaskInfo taskInfo = new TaskInfo();
				BaseTask selectBaseTaskByTaskId = baseTaskMapper
						.selectBaseTaskByTaskId(t.getTaskId());
				taskInfo.setTaskCategory(selectBaseTaskByTaskId
						.getTaskCategory());
				taskInfo.setAppId(t.getAppId());
				taskInfo.setId(t.getId());
				taskInfo.setTaskDescribe(selectBaseTaskByTaskId
						.getTaskDescribe());
				taskInfo.setTaskId(t.getTaskId());
				taskInfo.setTaskImg(selectBaseTaskByTaskId.getTaskImg());
				taskInfo.setTaskName(selectBaseTaskByTaskId.getTaskName());
				taskInfo.setTaskProgess(t.getTaskProgess());
				taskInfo.setTaskProgess1(t.getTaskProgess1());
				taskInfo.setTaskReward(selectBaseTaskByTaskId.getTaskReward());
				taskInfo.setTaskState(t.getTaskState());
				taskInfo.setTaskTarget(selectBaseTaskByTaskId.getTaskTarget());
				taskInfo.setTaskTarget1(selectBaseTaskByTaskId.getTaskTarget1());
				taskInfo.setTaskType(selectBaseTaskByTaskId.getTaskType());
				taskInfoList.add(taskInfo);
			}
			taskInfoList.sort(new Comparator<TaskInfo>() {

				@Override
				public int compare(TaskInfo o1, TaskInfo o2) {
					int taskProgess = o1.getTaskProgess();
					int taskTarget = o1.getTaskTarget();
					float t1 = (float) taskProgess / taskTarget;

					int taskProgess1 = o2.getTaskProgess();
					int taskTarget1 = o2.getTaskTarget();
					float t2 = (float) taskProgess1 / taskTarget1;

					if (t1 - t2 < 0) {
						return 1;
					} else if (t1 - t2 > 0) {
						return -1;
					} else {
						return 0;
					}
				}
			});

			resp.put("taskInfoList", taskInfoList);
			return resp;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	// 领取礼包
	@Transactional
	@RequestMapping(value = URL_GET_GIFT)
	@ResponseBody
	public JsonResponse getGiftReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			//logger.info("----gift.kfb----------->getGiftReward,ID:{}",ID);
			//logger.info("----gift.kfb----------->getGiftReward,ID:{}",ID);
			//logger.info("----gift.kfb----------->getGiftReward,ID:{}",ID);
			logger.info("----gift.kfb----------->getGiftReward,ID:{}",ID);
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			BuyResponse jr = new BuyResponse(ReturnCode.SUCCESS);
			
			
			RoleReward roleReward = accountMapper.selectRoleReward(ID, REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
			if(roleReward == null){//没有礼包
				logger.info("----gift.kfb----------->{}","can not get big gifts!!");
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}
			
			
			RoleReward roleReward1 = accountMapper.selectRoleReward(ID, REWARD_TYPE.GIFT_REWARD.getValue());
			if(roleReward1 != null){//已经领取过礼包
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				logger.info("----gift.kfb----------->{}","already get big gifts!!");
				return jr;
			}
			
			
			
			String giftReward = resourceService
					.getGameOption(GAME_OPTION.GIFT_REWARD);
			
			logger.info("--------------------->giftReward:{}",giftReward);

			StringTokenizer st = new StringTokenizer(giftReward, ",");
			List<String> list = new ArrayList<String>();
			while (st.hasMoreElements()) {
				String nextElement = (String) st.nextElement();
				list.add(nextElement);
			}
			HashMap<Integer, Integer> rewardmap = new HashMap<Integer, Integer>();
			for (int i = 0; i < list.size(); i += 2) {
				int itemId = Integer.parseInt(list.get(i));
				int itemCount = Integer.parseInt(list.get(i + 1));
				rewardmap.put(itemId, itemCount);
			}
			if (rewardmap.size() > 0) {
				// 给奖励
				final DateUtil dt = new DateUtil(System.currentTimeMillis());
				Iterator<Entry<Integer, Integer>> iterator = rewardmap
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<Integer, Integer> next = iterator.next();
					Shop rewardItem = resourceService.getShop(0, next.getKey());
					int rewardValue = next.getValue();
					int chId = 0;
					Inven selectMaxLvPet = invenMapper.selectMaxLvPet(account
							.getAppId());
					if (selectMaxLvPet != null) {
						chId = selectMaxLvPet.getChId();
					}

					AchieveLog achieveLog = new AchieveLog(dt, ID);
					taskService.giveItem(rewardItem, account, rewardValue,
							achieveLog, chId);
					loggingService.insertGameLog(achieveLog);
				}

			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			String result = "成功领取";

			if (rewardmap.size() > 0) {
				Integer[] array = rewardmap.keySet().toArray(new Integer[0]);
				Integer itemId = array[0];
				Integer count = rewardmap.get(array[0]);
				jr.setITID(itemId);
				jr.setITCNT(count);

				for (int i = 0; i < array.length; i++) {
					int _count = rewardmap.get(array[i]);
					if (array[i] == DebugOption.REWARD_ITEM_GOLD) {
						result += "铜钱x" + _count + ",";
					} else if (array[i] == DebugOption.REWARD_ITEM_GOLD) {
						result += "如意珠x" + _count + ",";
					} else if (array[i] == DebugOption.REWARD_ITEM_PUNCH) {
						result += "拳头x" + _count + ",";
					} else {
						String name = resourceService.getShop(0, array[i])
								.getName();
						result += name + "x" + _count + ",";
					}
				}
				if (result.lastIndexOf(",") == result.length() - 1) {
					result = result.substring(0, result.length() - 1);
				}

				jr.put("result", result);
				
				logger.info("--------------------->result:{}",result);
				
//				RoleReward rr = new RoleReward();
//				rr.setAppId(ID);
//				rr.setGettime(new Date());
//				rr.setType(REWARD_TYPE.GIFT_REWARD.getValue());
//				accountMapper.insertRoleReward(rr);
				accountMapper.updateRoleReward(ID,REWARD_TYPE.GIFT_REWARD.getValue());
				logger.info("--------------------->{}","updateRoleReward");
			} else {
				jr.setITID(0);
				jr.setITCNT(0);
			}

			jr.setGD(account.getGold());
			jr.setBL(account.getBall());
			jr.setPN(account.getPunch());
			jr.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
			jr.setADDGP(0);

			
			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@Transactional
	@RequestMapping(value = URL_GET_TASK_REWARD)
	@ResponseBody
	public JsonResponse getTaskReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "TID", required = true) long TID) {
		try {

			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			// JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			BuyResponse jr = new BuyResponse(ReturnCode.SUCCESS);
			RoleTask roleTask = baseTaskMapper.selectRoleTaskById(TID, ID);
			if (roleTask == null
					|| roleTask.getTaskState() != TASK_STATE.NOT_OVER
							.getValue()) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}
			final int taskId = roleTask.getTaskId();
			List<BaseTask> baseTaskList = resourceService.getBaseTaskList();
			BaseTask bt = null;
			for (BaseTask baseTask : baseTaskList) {
				if (baseTask.getTaskId() == taskId) {
					bt = baseTask;
					break;
				}
			}
			if (null == bt) {
				throw new Exception("不存在" + taskId + "号任务");
			}

			if (roleTask.getTaskProgess() < bt.getTaskTarget()
					|| roleTask.getTaskProgess1() < bt.getTaskTarget1()) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}

			String taskReward = bt.getTaskReward();
			StringTokenizer st = new StringTokenizer(taskReward, ",");
			List<String> list = new ArrayList<String>();
			while (st.hasMoreElements()) {
				String nextElement = (String) st.nextElement();
				list.add(nextElement);
			}
			HashMap<Integer, Integer> rewardmap = new HashMap<Integer, Integer>();
			for (int i = 0; i < list.size(); i += 2) {
				int itemId = Integer.parseInt(list.get(i));
				int itemCount = Integer.parseInt(list.get(i + 1));
				rewardmap.put(itemId, itemCount);
			}
			if (rewardmap.size() > 0) {
				// 给奖励
				final DateUtil dt = new DateUtil(System.currentTimeMillis());
				Iterator<Entry<Integer, Integer>> iterator = rewardmap
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<Integer, Integer> next = iterator.next();
					Shop rewardItem = resourceService.getShop(0, next.getKey());
					int rewardValue = next.getValue();
					int chId = 0;
					// ITEM_TYPE itemType = DebugOption.getItemType(rewardItem
					// .getItemId());
					// if (itemType == itemType.PET) {
					// chId = rewardItem.getItemId();
					// }
					Inven selectMaxLvPet = invenMapper.selectMaxLvPet(account
							.getAppId());
					if (selectMaxLvPet != null) {
						chId = selectMaxLvPet.getChId();
					}

					AchieveLog achieveLog = new AchieveLog(dt, ID);
					taskService.giveItem(rewardItem, account, rewardValue,
							achieveLog, chId);
					loggingService.insertGameLog(achieveLog);
				}
				roleTask.setTaskState(TASK_STATE.OVER.getValue());
			}
			// 设置任务为完成
			baseTaskMapper.updateRoleTask(roleTask);
			// jr.put("coin", account.getGold());
			// jr.put("ball", account.getBall());
			// jr.put("punch", account.getPunch());

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			String result = "成功领取";

			if (rewardmap.size() > 0) {
				Integer[] array = rewardmap.keySet().toArray(new Integer[0]);
				Integer itemId = array[0];
				Integer count = rewardmap.get(array[0]);
				jr.setITID(itemId);
				jr.setITCNT(count);

				for (int i = 0; i < array.length; i++) {
					int _count = rewardmap.get(array[i]);
					if (array[i] == DebugOption.REWARD_ITEM_GOLD) {
						result += "铜钱x" + _count + ",";
					} else if (array[i] == DebugOption.REWARD_ITEM_GOLD) {
						result += "如意珠x" + _count + ",";
					} else if (array[i] == DebugOption.REWARD_ITEM_PUNCH) {
						result += "拳头x" + _count + ",";
					} else {
						String name = resourceService.getShop(0, array[i])
								.getName();
						result += name + "x" + _count + ",";
					}
				}
				if (result.lastIndexOf(",") == result.length() - 1) {
					result = result.substring(0, result.length() - 1);
				}

				jr.put("result", result);
			} else {
				jr.setITID(0);
				jr.setITCNT(0);
			}

			jr.setGD(account.getGold());
			jr.setBL(account.getBall());
			jr.setPN(account.getPunch());
			jr.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
			jr.setADDGP(0);

			if (bt.getTaskCategory() == TASK_CATEGORY.DAILY_TASK.getValue()) {
				{
					// tasktask
					List<TaskDetail> newTaskDetailList = newTaskDetailList(
							TASK_TYPE.FINISH_TASK_TIMES, 1);
					submitTaskDetailInfo(account.getAppId(), newTaskDetailList);
				}
			} else if (bt.getTaskCategory() == TASK_CATEGORY.ACHIEVE.getValue()) {
				{
					// tasktask
					List<TaskDetail> newTaskDetailList = newTaskDetailList(
							TASK_TYPE.FINISH_ACHIEVE, 1);
					submitTaskDetailInfo(account.getAppId(), newTaskDetailList);
				}
			}

			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_SUBMIT_TASK)
	@ResponseBody
	@Transactional
	public JsonResponse submitTask(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "TR", required = true) String TR) {
		try {
			//logger.info("#################" + TR);
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			TaskDetail[] parseJson = DebugOption.parseJson(TaskDetail[].class,
					TR);

			List<TaskDetail> parseJsonList = Arrays.asList(parseJson);
			submitTaskDetailInfo(ID, parseJsonList);
			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	public boolean submitTaskDetailInfo(String appId,
			List<TaskDetail> listTaskDetail) {
		// System.out.println("####"+listTaskDetail.size());
		List<RoleTask> roleTask = baseTaskMapper.selectRoleTaskByAppId(appId);

		List<BaseTask> baseTaskList = resourceService.getBaseTaskList();

		List<TaskInfoAll> taskInfoAllList = new ArrayList<TaskInfoAll>();

		for (RoleTask rt : roleTask) {
			for (BaseTask bt : baseTaskList) {
				for (TaskDetail td : listTaskDetail) {
					if (rt.getTaskId() == bt.getTaskId()
							&& bt.getTaskType() == td.getTaskType()) {
						TaskInfoAll tia = new TaskInfoAll();
						tia.setBaseTask(bt);
						tia.setRoleTask(rt);
						tia.setTaskDetail(td);
						taskInfoAllList.add(tia);
					}
				}

			}
		}

		int firstDailyTask = -1;
		for (RoleTask rt : roleTask) {
			if (firstDailyTask != -1) {
				break;
			}
			for (BaseTask bt : baseTaskList) {
				if (rt.getTaskId() == bt.getTaskId()
						&& bt.getTaskCategory() == TASK_CATEGORY.DAILY_TASK
								.getValue()) {
					firstDailyTask = bt.getTaskId();
					break;
				}
			}
		}

		int firstDailyActivity = -1;
		for (RoleTask rt : roleTask) {
			if (firstDailyActivity != -1) {
				break;
			}
			for (BaseTask bt : baseTaskList) {
				if (rt.getTaskId() == bt.getTaskId()
						&& bt.getTaskCategory() == TASK_CATEGORY.ACTIVITY
								.getValue()) {
					firstDailyActivity = bt.getTaskId();

					break;
				}
			}
		}

		// 每日任务
		List<TaskInfoAll> taskInfoAllListDailyTask = new ArrayList<TaskInfoAll>();
		for (TaskInfoAll tia : taskInfoAllList) {
			if (tia.getBaseTask().getTaskCategory() == TASK_CATEGORY.DAILY_TASK
					.getValue()) {
				taskInfoAllListDailyTask.add(tia);
			}
		}
		// System.out.println("###" + taskInfoAllListDailyTask.size());

		for (TaskInfoAll tia : taskInfoAllListDailyTask) {
			if (firstDailyTask == tia.getBaseTask().getTaskId()) {
				int taskProgess = tia.getRoleTask().getTaskProgess();
				tia.getRoleTask().setTaskProgess(
						taskProgess + tia.getTaskDetail().getCount());
				baseTaskMapper.updateRoleTask(tia.getRoleTask());
				// System.out.println(1);

			}
			break;
		}

		// 每日活动
		List<TaskInfoAll> taskInfoAllListDailyActivity = new ArrayList<TaskInfoAll>();
		for (TaskInfoAll tia : taskInfoAllList) {
			if (tia.getBaseTask().getTaskCategory() == TASK_CATEGORY.ACTIVITY
					.getValue()) {
				taskInfoAllListDailyActivity.add(tia);
			}
		}

		for (TaskInfoAll tia : taskInfoAllListDailyActivity) {
			if (firstDailyActivity == tia.getBaseTask().getTaskId()) {
				int taskProgess = tia.getRoleTask().getTaskProgess();
				tia.getRoleTask().setTaskProgess(
						taskProgess + tia.getTaskDetail().getCount());
				baseTaskMapper.updateRoleTask(tia.getRoleTask());
				// System.out.println(2);
			}
			break;
		}

		// 成就
		List<TaskInfoAll> taskInfoAllListACHIEVE = new ArrayList<TaskInfoAll>();
		for (TaskInfoAll tia : taskInfoAllList) {
			if (tia.getBaseTask().getTaskCategory() == TASK_CATEGORY.ACHIEVE
					.getValue()) {
				taskInfoAllListACHIEVE.add(tia);
			}
		}

		for (TaskInfoAll tia : taskInfoAllListACHIEVE) {
			int taskProgess = tia.getRoleTask().getTaskProgess();
			tia.getRoleTask().setTaskProgess(
					taskProgess + tia.getTaskDetail().getCount());
			baseTaskMapper.updateRoleTask(tia.getRoleTask());
			// System.out.println(3);
		}

		// 活动
		List<TaskInfoAll> taskInfoAllListACTIVITY = new ArrayList<TaskInfoAll>();
		for (TaskInfoAll tia : taskInfoAllList) {
			if (tia.getBaseTask().getTaskCategory() == TASK_CATEGORY.ACTIVITY
					.getValue()) {
				taskInfoAllListACTIVITY.add(tia);
			}
		}
		for (TaskInfoAll tia : taskInfoAllListACTIVITY) {
			int taskProgess = tia.getRoleTask().getTaskProgess();

			TASK_TYPE tk = TASK_TYPE.valueOf(tia.getBaseTask().getTaskType());
			// 累计
			if (tk != TASK_TYPE.UNUSE_GO_THROUGH_SCORE
					&& tk != TASK_TYPE.UNUSE_GO_THROUGH_SCORE_FIGHT
					&& tk != TASK_TYPE.SINGLE_RUN_MAX

			) {
				tia.getRoleTask().setTaskProgess(
						taskProgess + tia.getTaskDetail().getCount());
			}
			// 不累计
			if (tk == TASK_TYPE.UNUSE_GO_THROUGH_SCORE
					|| tk == TASK_TYPE.UNUSE_GO_THROUGH_SCORE_FIGHT
					|| tk == TASK_TYPE.SINGLE_RUN_MAX
					|| tk == TASK_TYPE.UNLIMIT_NOT_ALIVE_MAX_POINT
					|| tk == TASK_TYPE.MAX_UNLIMIT_POINT) {
				RoleTask rt = tia.getRoleTask();
				rt.setTaskProgess(tia.getTaskDetail().getCount());
				baseTaskMapper.updateMorethanRoleTask(rt);
				if (tk == TASK_TYPE.UNUSE_GO_THROUGH_SCORE_FIGHT) {
					baseTaskMapper.insertOrUpdateFightActivityRank(appId, tia
							.getTaskDetail().getCount());
				}

				if (tk == TASK_TYPE.MAX_UNLIMIT_POINT) {
					baseTaskMapper.insertOrUpdateFightActivityRank_1(appId, tia
							.getTaskDetail().getCount());
				}

			} else if (tk == TASK_TYPE.ONE_RECHARGE)// 单笔充值
			{
				int count = tia.getTaskDetail().getCount();
				int arr[] = new int[] { 30, 60, 100, 300, 1000 };
				for (int i = 0; i < arr.length - 1; i++) {
					int target = tia.getBaseTask().getTaskTarget();
					if (arr[i] <= count && count < arr[i + 1]) {
						RoleTask rt = tia.getRoleTask();
						rt.setTaskProgess(arr[i]);
						baseTaskMapper.updateMorethanRoleTask(rt);
					}
				}
			} else {
				baseTaskMapper.updateRoleTask(tia.getRoleTask());
			}
		}
		return true;
	}

	@Transactional
	public void deleteTask(TASK_CATEGORY tc) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		if (tc == TASK_CATEGORY.ACTIVITY || tc == TASK_CATEGORY.DAILY_TASK) {
			boolean deleteDailyResetTask = isDeleteDailyResetTask();
			if (deleteDailyResetTask) {
				return;
			}
			DeleteDailyResetTask dwr = new DeleteDailyResetTask(dateUtil);
			loggingService.insertGameLog(dwr);
		} else if (tc == TASK_CATEGORY.ACHIEVE) {
			boolean deleteWeeklyResetTask = isDeleteWeeklyResetTask();
			if (deleteWeeklyResetTask) {
				return;
			}
			DeleteWeeklyResetTask ddrt = new DeleteWeeklyResetTask(dateUtil);

			loggingService.insertGameLog(ddrt);
		}
		List<BaseTask> selectBaseTask = resourceService.getBaseTaskList();// baseTaskMapper.selectBaseTask();
		List<BaseTask> selectBaseTask1 = new ArrayList<BaseTask>();
		for (BaseTask baseTask : selectBaseTask) {
			if (baseTask.getTaskCategory() == tc.getValue()) {
				selectBaseTask1.add(baseTask);
			}
		}
		taskService.deleteTask(tc);
	}

	public List<TaskDetail> newTaskDetailList(TASK_TYPE taskType, int count) {
		List<TaskDetail> taskDetailList = new ArrayList<TaskDetail>();
		TaskDetail taskDetail = new TaskDetail();
		taskDetail.setTaskType(taskType.getValue());
		taskDetail.setCount(count);
		taskDetailList.add(taskDetail);
		return taskDetailList;
	}

	@Transactional
	public void configDailyResetTask(final String appId,
			final TASK_CATEGORY taskCategory) {
		List<RoleTask> roleTaskList = baseTaskMapper
				.selectRoleTaskByAppId1(appId);
		List<BaseTask> baseTaskList = resourceService.getBaseTaskList();// baseTaskMapper.selectBaseTask();

		List<BaseTask> baseTaskAchieveList = new ArrayList<BaseTask>();
		List<BaseTask> baseTaskTaskList = new ArrayList<BaseTask>();
		List<BaseTask> baseTaskActivityList = new ArrayList<BaseTask>();
		for (BaseTask bt : baseTaskList) {
			if (bt.getTaskCategory() == TASK_CATEGORY.ACHIEVE.getValue()) {
				baseTaskAchieveList.add(bt);
			} else if (bt.getTaskCategory() == TASK_CATEGORY.DAILY_TASK
					.getValue()) {
				baseTaskTaskList.add(bt);
			} else if (bt.getTaskCategory() == TASK_CATEGORY.ACTIVITY
					.getValue()) {
				baseTaskActivityList.add(bt);
			}
		}

		List<Integer> achieveList = new ArrayList<Integer>();
		List<Integer> taskList = new ArrayList<Integer>();
		List<Integer> acitvityList = new ArrayList<Integer>();
		for (RoleTask rt : roleTaskList) {
			if (rt.getTaskId() / 100 == 0) {
				achieveList.add(rt.getTaskId());
			} else if (rt.getTaskId() / 100 == 1) {
				taskList.add(rt.getTaskId());
			} else if (rt.getTaskId() / 1000 == 1) {
				acitvityList.add(rt.getTaskId());
			}
		}
		List<BaseTask> result = new ArrayList<BaseTask>();
		if (taskCategory == TASK_CATEGORY.ACHIEVE) {
			if (achieveList.size() == 0) {
				Collections.shuffle(baseTaskAchieveList);
				List<BaseTask> btList = distinctTaskType(baseTaskAchieveList);
				baseTaskAchieveList = btList;

				if (baseTaskAchieveList.size() >= 5) {
					result = baseTaskAchieveList.subList(0, 5);
				}
			}
		} else if (taskCategory == TASK_CATEGORY.DAILY_TASK) {
			if (taskList.size() == 0) {
				Collections.shuffle(baseTaskTaskList);
				List<BaseTask> btList = distinctTaskType(baseTaskTaskList);
				baseTaskTaskList = btList;

				if (baseTaskTaskList.size() >= 5) {
					result = baseTaskTaskList.subList(0, 5);
				}
			}
		} else {

			for (BaseTask btal : baseTaskActivityList) {
				int taskId = btal.getTaskId();
				if (!acitvityList.contains(taskId)) {
					result.add(btal);
				}
			}

		}

		//System.out.println(taskCategory.name() + ":" + result.size());
		result.forEach(new Consumer<BaseTask>() {
			@Override
			public void accept(BaseTask t) {
				RoleTask roleTask = new RoleTask();
				roleTask.setAppId(appId);
				roleTask.setTaskId(t.getTaskId());
				roleTask.setTaskProgess(0);
				roleTask.setTaskProgess1(0);
				roleTask.setTaskState(TASK_STATE.NOT_OVER.getValue());
				baseTaskMapper.insertRoleTask(roleTask);
			}
		});

	}

	public List<BaseTask> distinctTaskType(List<BaseTask> baseTaskAchieveList) {
		List<BaseTask> btList = new ArrayList<BaseTask>();
		for (BaseTask baseTask : baseTaskAchieveList) {
			boolean isContain = false;
			for (BaseTask _bt : btList) {
				if (_bt.getTaskType() == baseTask.getTaskType()) {
					isContain = true;
					break;
				}
			}
			if (!isContain) {
				btList.add(baseTask);
			}
		}
		return btList;
	}

	public void configActivityTask(String appId) {
		// List<RoleTask> selectRoleTaskByAppId = baseTaskMapper
		// .selectAllRoleTaskByAppId(appId);
		List<BaseTask> selectActivityBaseTask = baseTaskMapper
				.selectActivityBaseTask(appId);
		for (final BaseTask baseTask : selectActivityBaseTask) {
			RoleTask roleTask = new RoleTask();
			roleTask.setAppId(appId);
			roleTask.setTaskId(baseTask.getTaskId());
			roleTask.setTaskProgess(0);
			roleTask.setTaskProgess1(0);
			roleTask.setTaskState(TASK_STATE.NOT_OVER.getValue());
			baseTaskMapper.insertRoleTask(roleTask);
		}
	}

	public boolean isDeleteDailyResetTask() {
		// GameLog selectGameLog = loggingService.selectGameLog(
		// DebugOption.CONFIG_TASK_APPID,
		// GAMELOG_TYPE.DELETE_DAILY_RESET_TASK.getValue());
		// if (selectGameLog != null) {
		// int epoch = selectGameLog.getEpoch();
		// long sysMil = epoch * 1000L;
		// DateUtil dateUtil = new DateUtil(sysMil);
		// long dayMil = dateUtil.getTodayStart();
		// DateUtil dateUtil1 = new DateUtil(System.currentTimeMillis());
		// long todayStart = dateUtil1.getTodayStart();
		// if (dayMil == todayStart) {
		// return true;
		// }
		// }
		// return false;

		int count = loggingMapper.countOfTodayResetDaiyTask(
				DebugOption.CONFIG_TASK_APPID,
				GAMELOG_TYPE.DELETE_DAILY_RESET_TASK.getValue());
		if (count > 0) {
			return true;
		}

		return false;

	}

	public boolean isDeleteWeeklyResetTask() {
		// GameLog selectGameLog = loggingService.selectGameLog(
		// DebugOption.CONFIG_TASK_APPID,
		// GAMELOG_TYPE.DELETE_WEEKLY_RESET_TASK.getValue());
		// if (selectGameLog != null) {
		// int epoch = selectGameLog.getEpoch();// second
		// long sysMil = epoch * 1000L;
		// DateUtil dateUtil = new DateUtil(sysMil);
		// int week = dateUtil.getThisWeek();
		// DateUtil dateUtil1 = new DateUtil(System.currentTimeMillis());
		// int week1 = dateUtil1.getThisWeek();
		// if (week == week1) {
		// return true;
		// }
		// }
		// return false;

		int count = loggingMapper.countOfThisWeekResetWeekAchivev(
				DebugOption.CONFIG_TASK_APPID,
				GAMELOG_TYPE.DELETE_WEEKLY_RESET_TASK.getValue());
		//logger.debug("count:" + count);
		if (count > 0) {
			return true;
		}
		return false;
	}

}
