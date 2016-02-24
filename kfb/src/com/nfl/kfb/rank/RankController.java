/**
 * 
 */
package com.nfl.kfb.rank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
import com.nfl.kfb.account.AccountController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.friend.FriendInfoResponse;
import com.nfl.kfb.friend.FriendService;
import com.nfl.kfb.friend.FriendServiceImpl;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.account.BaseReward;
import com.nfl.kfb.mapper.account.RoleReward;
import com.nfl.kfb.mapper.inv.InvList;
import com.nfl.kfb.mapper.inv.InvMapper;
import com.nfl.kfb.mapper.logging.logs.LastRankLog;
import com.nfl.kfb.mapper.rank.Grank;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.REWARD_TYPE;
import com.nfl.kfb.util.RedisUtil;
import com.sun.org.apache.regexp.internal.recompile;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 * 
 */
@Controller
@RequestMapping(value = "/rank", method = { RequestMethod.POST,
		RequestMethod.GET })
public class RankController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory
			.getLogger(RankController.class);
	//获得上周好友排行奖励
	private static final String URL_LAST_WEEK_REWARD = "/weekRw";
	//获得全服排行
	private static final String URL_GLOBAL_RANK = "/global";
	//获得无尽排行
	private static final String URL_ULIMIT_GLOBAL_RANK = "/unlimitglobal";

	@Autowired
	@Qualifier("RankServiceImpl")
	private RankService rankService;

	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	@Qualifier("FriendServiceImpl")
	private FriendService friendService;

	@Autowired
	private RankMapper rankMapper;

	@Autowired
	private AccountController accountController;

	@Autowired
	private InvMapper invMapper;

	//获得上周好友排行奖励
	@RequestMapping(value = URL_LAST_WEEK_REWARD)
	@ResponseBody
	public JsonResponse getLastWeekReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "WK", required = true) int WK,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "FID", required = false) String[] FID,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			int thisWeek = dateUtil.getThisWeek();
			if (WK != thisWeek) {
				LastWeekRewardResponse lastWeekRewardResponse = new LastWeekRewardResponse(
						ReturnCode.WRONG_WEEK);
				lastWeekRewardResponse.setWK(thisWeek);
				lastWeekRewardResponse.setGOT(true);
				lastWeekRewardResponse.setITID(0);
				lastWeekRewardResponse.setITCNT(0);
				lastWeekRewardResponse.setGD(account.getGold());
				lastWeekRewardResponse.setBL(account.getBall());
				lastWeekRewardResponse.setPN(account.getPunch());
				lastWeekRewardResponse.setPNDT(account
						.getPunchRemainDt(dateUtil.getNowEpoch()));
				lastWeekRewardResponse.setRW(DebugOption.LAST_WEEK_RANK_REWARD);
				return lastWeekRewardResponse;
			}
			LastWeekRewardResponse lastWeekRewardResponse = new LastWeekRewardResponse(
					ReturnCode.SUCCESS);
			lastWeekRewardResponse.setWK(WK);
			int lastWeek = thisWeek - 1;
			// 전체 랭킹을 가져옴
			List<Rank> rewardWeekRank = rankService.getTotalRank(lastWeek, ID,
					FID);

			Rank myRank = null;
			// 보낼 데이터 만들기
			for (Rank rank : rewardWeekRank) {
				if (rank.getAppId().equals(ID)) {
					myRank = rank;
				}
				lastWeekRewardResponse.addF(rank.getAppId(),
						rank.getRank() + 1, rank.getPoint(), rank.getGate());
			}
			// 이전에 보상을 이미 받았었음(지난주 랭킹이 없는경우도 포함)
			boolean gotRankReward = myRank == null
					|| rankService.hasRankReward(ID, lastWeek);
			lastWeekRewardResponse.setGOT(gotRankReward);

			if (!gotRankReward) { // 보상을 지급
				LastRankLog lastRankLog = new LastRankLog(dateUtil,
						account.getAppId());
				rankService.rewardLastWeekRank(dateUtil, account, lastWeek,
						myRank.getRank(), CHID, lastRankLog,
						lastWeekRewardResponse, SHOPVER);
				lastRankLog.setRank(myRank.getRank() + 1);
				loggingService.insertGameLog(lastRankLog);
			} else {
				lastWeekRewardResponse.setITID(0);
				lastWeekRewardResponse.setITCNT(0);
			}
			lastWeekRewardResponse.setGD(account.getGold());
			lastWeekRewardResponse.setBL(account.getBall());
			lastWeekRewardResponse.setPN(account.getPunch());
			lastWeekRewardResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			lastWeekRewardResponse.setRW(DebugOption.LAST_WEEK_RANK_REWARD);

			return lastWeekRewardResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	// 获得全服排行
	@RequestMapping(value = URL_GLOBAL_RANK)
	@ResponseBody
	public JsonResponse getGlobalRank(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			String rankName = DebugOption.REDIS_RANK_KEY;
			return getRankByRankName(ID, rankName, null, null);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	// 获得无尽排行
	@RequestMapping(value = URL_ULIMIT_GLOBAL_RANK)
	@ResponseBody
	public JsonResponse getUnlimitGlobalRank(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			String rankName = DebugOption.REDIS_UNLIMIT_RNANK_KEY;
			List<String> friendList = friendService.getFriendList(ID);
			List<String> friendListReq = new ArrayList<String>();
			List<InvList> selectInvList = invMapper.selectInvList(ID, 0);
			for (InvList il : selectInvList) {
				String fid = il.getfAppId();
				friendListReq.add(fid);
			}
			JsonResponse rankByRankName = getRankByRankName(ID, rankName,
					friendList, friendListReq);
			int rewardType = REWARD_TYPE.UNLIMIT_RANK_REWARD.getValue();
			Jedis redis = DebugOption.getJedisPool().getResource();
			Long zrank = redis
					.zrevrank(DebugOption.REDIS_UNLIMIT_RNANK_KEY, ID);
			DebugOption.getJedisPool().returnResource(redis);
			accountController.AddRankRewardToResponse(ID, rewardType,
					zrank + 1, rankByRankName);
			return rankByRankName;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			e.printStackTrace();
			return errorResponse;
		}
	}

	//根据排行的名字获得排行 rank  unlimitrank
	public JsonResponse getRankByRankName(String ID, String rankName,
			List<String> friendList, List<String> reqFriendList) {
		FriendInfoResponse totalRankResponse = new FriendInfoResponse(
				ReturnCode.SUCCESS);
		long rank = -1;
		Set<Tuple> globalRank = null;
		Jedis redis = DebugOption.getJedisPool().getResource();
		rank = redis.zrevrank(rankName, ID);
		if (rank < 5) {
			globalRank = redis.zrevrangeWithScores(rankName, 0, 9);
		} else {
			globalRank = redis
					.zrevrangeWithScores(rankName, rank - 4, rank + 5);
		}
		DebugOption.getJedisPool().returnResource(redis);
		List<String> rankList = new ArrayList<String>();
		for (Tuple tu : globalRank) {
			rankList.add(tu.getElement());
		}
		int indexOf = rankList.indexOf(ID);
		long delta = rank - indexOf;
		int id = 0;
		for (Tuple tu : globalRank) {
			Account ac = accountService.getAccount(tu.getElement());
			boolean isContain = false;
			if (friendList != null) {
				isContain = friendList.contains(tu.getElement());
			}
			boolean isReq = false;
			if (reqFriendList != null) {
				isReq = reqFriendList.contains(tu.getElement());
			}
			long sc = new Double(tu.getScore()).longValue();
			int pk = new Long(id + delta + 1).intValue();
			totalRankResponse.addF(tu.getElement(), ac.getChId(), ac.getChLv(),
					ac.getPetId(), ac.getPush() > 0 ? true : false, 10000,
					ac.getNickname(), ac.getImg(), pk, sc, isContain, isReq);
			id++;
		}
		return totalRankResponse;
	}

	public long submitUnlimitRankPoint(String ID, long point) {
		String key = DebugOption.REDIS_UNLIMIT_RNANK_KEY;
		Jedis redis = DebugOption.getJedisPool().getResource();
		Double zscore = redis.zscore(key, ID);
		long longValue = zscore.longValue();
		if (longValue < point) {
			redis.zadd(key, point, ID);
			longValue = point;
			rankMapper.insertOrUpdateUnlimitRank(ID, point);
		}
		DebugOption.getJedisPool().returnResource(redis);
		return longValue;
	}

}
