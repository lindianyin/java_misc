/**
 * 
 */
package com.nfl.kfb.play;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.rank.GateRank;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.rank.RankController;
import com.nfl.kfb.rank.RankService;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.ResourceService.GAME_OPTION;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 6. 27.
 * 
 */
@Controller
@RequestMapping(value = "/play", method = { RequestMethod.POST,
		RequestMethod.GET })
public class PlayController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory
			.getLogger(PlayController.class);

	private static final String URL_START_GAME = "/start";
	private static final String URL_FINISH_GAME = "/finish";
	private static final String URL_BOAST = "/boast";
	private static final String URL_START_DUNGEON = "/startDungeon";
	private static final String URL_FINISH_DUNGEON = "/finishDungeon";

	// private static final String URL_START_UNLIMIT = "/startUnlimit";
	//
	// private static final String URL_FINISH_UNLIMIT = "/finishUnlimit";

	// private static final String URL_GET_GATE_RANK = "/gaterank";

	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;

	@Autowired
	@Qualifier("PlayServiceImpl")
	private PlayService playService;

	@Autowired
	@Qualifier("RankServiceImpl")
	private RankService rankService;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	private TaskController taskController;

	@Autowired
	private RankController rankController;

	@Autowired
	private AccountMapper accountMapper;

	@RequestMapping(value = URL_START_GAME)
	@ResponseBody
	public JsonResponse startGame(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "WK", required = true) int WK,
			@RequestParam(value = "GN", required = true) int GN,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "CLV", required = true) int CLV,
			@RequestParam(value = "PID", required = true) int PID,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "APPIDS", required = false) String[] APPIDS) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			if (APPIDS == null) {
				APPIDS = new String[] { "yidengdashiiiiiiiiiii" };
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			int thisWeek = dateUtil.getThisWeek();
			if (WK != thisWeek) {
				// 주가 바꼈음
				StartGameResponse startGameResponse = new StartGameResponse(
						ReturnCode.WRONG_WEEK);
				startGameResponse.setWK(thisWeek);
				return startGameResponse;
			}

			// playGate validation
			if (GN < DebugOption.UNLIMIT_GATE) {

				if (GN < 0 || GN > DebugOption.LAST_GATE) {
					StartGameResponse startGameResponse = new StartGameResponse(
							ReturnCode.UNKNOWN_ERR);
					startGameResponse.setException(new Exception(
							"Wrong Gate Number"));
					startGameResponse.setWK(thisWeek);
					startGameResponse.setPK(account.getPlayKey());
					startGameResponse.setPN(account.getPunch());
					startGameResponse.setPNDT(account.getPunchRemainDt(dateUtil
							.getNowEpoch()));
					startGameResponse.setADDGP(0);
					return startGameResponse;
				}
			}

			int reqPunch = 1;
			if (GN < DebugOption.UNLIMIT_GATE) {
				reqPunch = resourceService.getWeekResource(thisWeek)
						.getWeekMission(GN).getPunch();
			}

			StartGameResponse startGameResponse = playService.startPlay(
					dateUtil, account, thisWeek, GN, CHID, CLV, PID, GP,
					reqPunch);

			List<GateRank> gateRankOfweek = null;
			if (GN < DebugOption.UNLIMIT_GATE) {
				gateRankOfweek = rankService.getGateRankOfweek(APPIDS, WK, GN);
				Collections.sort(gateRankOfweek, new Comparator<GateRank>() {
					@Override
					public int compare(GateRank o1, GateRank o2) {

						return -(o1.getPoint() - o2.getPoint());
					}
				});

				for (GateRank gr : gateRankOfweek) {
					String appId = gr.getAppId();
					Account account2 = accountService.getAccount(appId);
					String nickname = account2.getNickname();
					gr.setChid(account2.getChId());
					gr.setNickname(nickname);
				}

			} else if (GN > DebugOption.UNLIMIT_GATE) {
				// 竞技模式

			} else {
				Set<Tuple> globalRank = null;
				String rankName = DebugOption.REDIS_UNLIMIT_RNANK_KEY;
				Jedis redis = DebugOption.getJedisPool().getResource();
				Long rank = redis.zrevrank(rankName, ID);
				globalRank = redis.zrevrangeWithScores(rankName, rank + 1,
						rank + 10);
				DebugOption.getJedisPool().returnResource(redis);
				List<String> rankList = new ArrayList<String>();
				List<Long> rankScore = new ArrayList<Long>();
				for (Tuple tu : globalRank) {
					rankList.add(tu.getElement());
					long val = new Double(tu.getScore()).longValue();
					rankScore.add(val);
				}
				gateRankOfweek = new ArrayList<GateRank>();
				for (int i = 0; i < rankList.size(); i++) {
					String appId = rankList.get(i);
					Long score = rankScore.get(i);
					Account acc = accountService.getAccount(appId);
					if (acc != null) {
						GateRank gr = new GateRank();
						gr.setPoint(new Long(score).intValue());
						gr.setNickname(acc.getNickname());
						gateRankOfweek.add(gr);
					}
				}
				// System.out.println("######" + gateRankOfweek.size());

			}
			startGameResponse.put("gaterank", gateRankOfweek);
			return startGameResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_FINISH_GAME)
	@ResponseBody
	public JsonResponse finishGame(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "WK", required = true) int WK,
			@RequestParam(value = "PK", required = true) int PK,
			@RequestParam(value = "GN", required = true) int GN,
			@RequestParam(value = "MD5", required = true) String MD5,
			@RequestParam(value = "PT", required = true) int PT,
			@RequestParam(value = "GD", required = true) int GD,
			@RequestParam(value = "BL", required = true) int BL,
			@RequestParam(value = "RW", required = true) boolean RW,
			@RequestParam(value = "USEINV", required = false, defaultValue = "0") int USEINV,
			@RequestParam(value = "ITID", required = false) Integer[] ITID,
			@RequestParam(value = "ITCNT", required = false) Integer[] ITCNT,
			@RequestParam(value = "FID", required = false) String[] FID,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			// Play Key 가 맞지 않는다면 다른 게임의 결과를 요청한 것이므로 거절
			if (account.getPlayKey() != PK) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_GAME);
				return response;
			}

			// PK = 0은 게임이 시작된 적이 없다는 뜻임(혹은 이미 FINISH_GAME이 되어서 PlayKey값이 리셋이
			// 되었거나)
			if (PK == Account.DEFAULT_PLAY_KEY) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_GAME);
				return response;
			}

			// Gate 가 맞지 않는다면 다른 게임의 결과를 요청한 것이므로 거절
			if (GN < DebugOption.UNLIMIT_GATE) {
				if (account.getPlayGate() != GN) {
					JsonResponse response = new JsonResponse(
							ReturnCode.WRONG_GAME);
					return response;
				}
			}

			// PKM MD5 확인, MD5값이 잘못되었다면 정상적인 요청이 아니다
			if (!DebugOption
					.isValidFinishGameMD5(MD5, ID, account.getPlayKey())) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_MD5);
				return response;
			}

			// 획득한 엽전/여의주가 0보다 작을수는 없다
			if (GD < 0 || BL < 0) {
				throw new RuntimeException("Wrong FinishGame Gold, Ball : GD="
						+ GD + ", BL=" + BL);
			}

			Collection<GameLog> gameLogs = new ArrayList<GameLog>();

			// 게임 결과를 계산
			FinishGameResponse finishGameResponse = playService.finishPlay(
					dateUtil, account, GD, BL, PT, RW, FID, ITID, ITCNT,
					USEINV, gameLogs, GP, SHOPVER, GN);

			// //task
			int starByPoint = DebugOption.getStarByPoint(PT, GN);
			if (starByPoint == 3) {
				// tasktask
				{
					List<TaskDetail> newTaskDetailList = taskController
							.newTaskDetailList(TASK_TYPE.STAR_PASS, 1);
					taskController.submitTaskDetailInfo(account.getAppId(),
							newTaskDetailList);
				}
			}

			{
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.PLAY_TIMES, 1);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}

			// 随机获得一个未购买或者拥有的物品的ID（正在使用的角色）
			Integer itemId = accountMapper.getUnBuyItemId(ID);
			//Shop _shop = resourceService.getShop(0, itemId);
			
			
			if (itemId == null) {
				itemId = -1;
				int[] arr = new int[] { 400, 401, 402, 403,510, 511, 512, 513, 514, 515};
				Random r = new Random();
				int idx = r.nextInt(arr.length);
				int val = arr[idx];
				itemId = val;
			}
			//itemId = 401;

			finishGameResponse.put("itemId", itemId);
			
			if (starByPoint > 0) {
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.GET_START, starByPoint);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}

			if (!gameLogs.isEmpty()) {
				loggingService.insertGameLog(gameLogs);
			}

			return finishGameResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_BOAST)
	@ResponseBody
	public JsonResponse boast(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "FID", required = true) String FID) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			if (ID.equalsIgnoreCase(FID)) {
				JsonResponse errorResponse = new JsonResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Couldn't boast to yourself"));
				return errorResponse;
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			BoastResponse boastResponse = playService.boast(dateUtil, account,
					FID);

			return boastResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_START_DUNGEON)
	@ResponseBody
	public JsonResponse startDungeon(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "CLV", required = true) int CLV,
			@RequestParam(value = "PID", required = true) int PID) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			StartDungeonResponse startDungeonResponse = playService
					.startDungeon(dateUtil, account, CHID, CLV, PID);
			// tasktask
			{
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.GO_MO_KU, 1);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}
			return startDungeonResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_FINISH_DUNGEON)
	@ResponseBody
	public JsonResponse finishDungeon(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "PK", required = true) int PK,
			@RequestParam(value = "MD5", required = true) String MD5,
			@RequestParam(value = "GD", required = true) int GD,
			@RequestParam(value = "BL", required = true) int BL,
			@RequestParam(value = "ITID", required = false) Integer[] ITID,
			@RequestParam(value = "ITCNT", required = false) Integer[] ITCNT,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			// Play Key 가 맞지 않는다면 다른 게임의 결과를 요청한 것이므로 거절
			if (account.getPlayKey() != PK) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_GAME);
				return response;
			}

			// PK = 0은 게임이 시작된 적이 없다는 뜻임(혹은 이미 FINISH_GAME이 되어서 PlayKey값이 리셋이
			// 되었거나)
			if (PK == Account.DEFAULT_PLAY_KEY) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_GAME);
				return response;
			}

			// Gate 가 맞지 않는다면 다른 게임의 결과를 요청한 것이므로 거절
			if (account.getPlayGate() != DebugOption.DUNGEON_GATE_NUMBER) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_GAME);
				return response;
			}

			// PKM MD5 확인, MD5값이 잘못되었다면 정상적인 요청이 아니다
			if (!DebugOption
					.isValidFinishGameMD5(MD5, ID, account.getPlayKey())) {
				JsonResponse response = new JsonResponse(ReturnCode.WRONG_MD5);
				return response;
			}

			// 획득한 엽전/여의주가 0보다 작을수는 없다
			if (GD < 0 || BL < 0) {
				throw new RuntimeException("Wrong FinishGame Gold, Ball : GD="
						+ GD + ", BL=" + BL);
			}

			Collection<GameLog> gameLogs = new ArrayList<GameLog>();

			int dungeonLimit = Integer.parseInt(resourceService
					.getGameOption(GAME_OPTION.DUNGEON_DAILY_LIMIT));
			int dungeonPunch = Integer.parseInt(resourceService
					.getGameOption(GAME_OPTION.DUNGEON_REQ_PUNCH));

			// 게임 결과를 계산
			FinishDungeonResponse finishDungeonResponse = playService
					.finishDungeon(dateUtil, account, GD, BL, ITID, ITCNT,
							gameLogs, dungeonLimit, dungeonPunch, SHOPVER);

			if (!gameLogs.isEmpty()) {
				loggingService.insertGameLog(gameLogs);
			}
			
			// 随机获得一个未购买或者拥有的物品的ID（正在使用的角色）
			Integer itemId = accountMapper.getUnBuyItemId(ID);
			//Shop _shop = resourceService.getShop(0, itemId);
			
			
			if (itemId == null) {
				itemId = -1;
				int[] arr = new int[] { 400, 401, 402, 403,510, 511, 512, 513, 514, 515};
				Random r = new Random();
				int idx = r.nextInt(arr.length);
				int val = arr[idx];
				itemId = val;
			}
			finishDungeonResponse.put("itemId", itemId);

			return finishDungeonResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}
}
