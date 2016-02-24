/**
 * 
 */
package com.nfl.kfb.friend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.apache.jasper.tagplugins.jstl.core.ForEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.account.FriendAccount;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.inv.Friendly;
import com.nfl.kfb.mapper.inv.FriendlyMapper;
import com.nfl.kfb.mapper.inv.Inv;
import com.nfl.kfb.mapper.inv.InvList;
import com.nfl.kfb.mapper.inv.InvMapper;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.logs.InvFriendLog;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.mapper.mail.Punch;
import com.nfl.kfb.mapper.rank.GateRank;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 * 
 */
@Service(value = "FriendServiceImpl")
public class FriendServiceImpl extends AbstractKfbService implements
		FriendService {

	private static final Logger logger = LoggerFactory
			.getLogger(FriendServiceImpl.class);

	private static final int ISFRIEND = 1;
	private static final int ISNOTFRIEND = 0;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private InvenMapper invenMapper;

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	private InvMapper invMapper;

	@Autowired
	private MailMapper mailMapper;

	@Autowired
	private GambleMapper gambleMapper;

	@Autowired
	private FriendlyMapper friendlyMapper;

	@Autowired
	private AccountService accountService;

	@Autowired
	private RankMapper rankMapper;

	@Autowired
	private TaskController taskController;

	@Transactional
	@Override
	public FriendInvResponse invFriend(DateUtil dateUtil, Account account,
			int invCnt, String friendId, InvFriendLog invFriendLog,
			boolean availableGP, int shopVer) throws JsonParseException,
			JsonMappingException, IOException {
		Inv inv = invMapper.selectInv(account.getAppId());

		// 클라에서 받은 현재 초대수 값이 올바르지 않음
		if (invCnt != (inv == null ? 0 : inv.getCnt())) {
			FriendInvResponse friendInvResponse = new FriendInvResponse(
					ReturnCode.UNKNOWN_ERR);
			friendInvResponse.setNOW(0);
			friendInvResponse.setINVCNT(inv == null ? 0 : inv.getCnt());
			friendInvResponse.setINVRIDX(-1);
			friendInvResponse.setITID(0);
			friendInvResponse.setITCNT(0);
			friendInvResponse.setGD(account.getGold());
			friendInvResponse.setBL(account.getBall());
			friendInvResponse.setPN(account.getPunch());
			friendInvResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			friendInvResponse.setADDGP(0);
			return friendInvResponse;
		}

		int newInvCnt = inv == null ? 1 : (inv.getCnt() + 1);

		FriendInvResponse friendInvResponse = new FriendInvResponse(
				ReturnCode.SUCCESS);
		friendInvResponse.setNOW(DebugOption.INV_COOL_TIME_EPOCH);
		friendInvResponse.setINVCNT(newInvCnt);

		// 초대 기본보상. 초대할때마다 주먹 1개씩 줌. 주먹 1개로 고정되어 있음. 수정 불가
		// invFriendLog에 주먹이 들어가고, 만약 밑에서 초대이벤트에 해당되면 추가보상아이템 이름이 덮어씌여짐
		final int defaultRewardItemId = DebugOption.INV_DEFAULT_REWARD[0];
		final int defaultRewardItemCnt = DebugOption.INV_DEFAULT_REWARD[1];
		Shop defaultRewardItem = resourceService.getShop(shopVer,
				defaultRewardItemId);
		if (isAccountItem(defaultRewardItemId)) {
			addAccountItem(dateUtil, account, defaultRewardItem,
					defaultRewardItemCnt, invFriendLog);
		}

		int RIDX = -1;

		int[] invReward = DebugOption.getInvReward(newInvCnt);
		// 보상 줌
		if (invReward == null) { // 보상이 있는 초대수가 아님
			friendInvResponse.setINVRIDX(-1);
			friendInvResponse.setITID(0);
			friendInvResponse.setITCNT(0);
		} else {
			RIDX = invReward[0];
			// final int reqCnt = invReward[1];
			final int rewardItemId = invReward[2];
			final int rewardItemCnt = invReward[3];

			// 전에 이미 받았었음
			if (inv != null && inv.gotReward(RIDX)) {
				logger.info("INV_FRIEND : Try reward RIDX=" + RIDX
						+ ", but You've got RIDX already. how?");
				friendInvResponse.setINVRIDX(-1);
				friendInvResponse.setITID(0);
				friendInvResponse.setITCNT(0);
			} else {
				invFriendLog.setHasReward();
				invFriendLog.setRewardIdx(newInvCnt);

				logger.debug("INV_FRIEND : InvCnt=" + newInvCnt + ", RIDX="
						+ RIDX);
				friendInvResponse.setINVRIDX(RIDX);

				Shop rewardItem = resourceService
						.getShop(shopVer, rewardItemId);
				if (isAccountItem(rewardItem.getItemId())) {
					addAccountItem(dateUtil, account, rewardItem,
							rewardItemCnt, invFriendLog);
					friendInvResponse.setITID(rewardItem.getItemId());
					friendInvResponse.setITCNT(rewardItemCnt);
					int itemId = rewardItem.getItemId();
					ITEM_TYPE itemType = DebugOption.getItemType(itemId);
					if (itemType == ITEM_TYPE.PUNCH) {
						// tasktask
						List<TaskDetail> newTaskDetailList = taskController
								.newTaskDetailList(TASK_TYPE.GET_REWARD_PUNCH,
										1);
						taskController.submitTaskDetailInfo(account.getAppId(),
								newTaskDetailList);
					}

				} else if (isInvenItem(rewardItem.getItemId())) {
					Inven newItem = createInvenItemFromShop(account.getAppId(),
							Inven.CHARACTER_ID_FOR_COMMON_ITEM, rewardItem,
							rewardItemCnt, false);
					Inven existItem = invenMapper.selectItem(
							account.getAppId(), newItem.getChId(),
							newItem.getItemId());

					friendInvResponse.setITID(newItem.getItemId());
					friendInvResponse.setITCNT(rewardItemCnt);

					invFriendLog.setItemId(rewardItem.getItemId());
					invFriendLog.setItemCnt(invFriendLog.getItemCnt()
							+ rewardItemCnt);

					final ITEM_TYPE itemType = DebugOption.getItemType(newItem
							.getItemId());
					switch (itemType) {

					case CHARACTER:
						if (existItem == null) {
							invenMapper.insertItem(newItem);
						} else {
							existItem
									.setItemLv(Math.min(
											DebugOption.MAX_CHARACTER_LV,
											existItem.getItemLv()
													+ newItem.getItemLv()));
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

					case PET:
						if (existItem == null) {
							invenMapper.insertItem(newItem);
						} else {
							existItem
									.setItemLv(Math.min(
											DebugOption.MAX_PET_LV,
											existItem.getItemLv()
													+ newItem.getItemLv()));
							int affectedRow = invenMapper.updateItem(existItem);
							if (affectedRow != 1)
								throw new RuntimeException(
										"updateItem affectedRow is not 1. something wrong.");
						}
						break;

					default:
						// 캐릭터 종속 아이템을 보상으로 추가할 경우 RequestParamter에 CHID를
						// 받아야하므로, API 수정이 필요함
						// 지금은 소환수 뿐이므로, CHARACTER_ID_FOR_COMMON_ITEM으로 아이템을 지급
						throw new RuntimeException(
								"InvReward item must be item character/skill/consume/pet");
					}
				}
			}
		}

		if (inv == null) {
			// insert
			inv = new Inv();
			inv.setAppId(account.getAppId());
			inv.setCnt(newInvCnt);
			if (RIDX != -1) {
				inv.setRwDt(RIDX, dateUtil.getNowEpoch());
			}
			invMapper.insertInv(inv);
		} else {
			// update
			inv.setCnt(newInvCnt);
			if (RIDX != -1) {
				inv.setRwDt(RIDX, dateUtil.getNowEpoch());
			}
			invMapper.updateInv(inv);
		}

		accountMapper.updateAccountItem(account);

		invMapper.insertOrUpdateInvList(account.getAppId(), friendId,
				dateUtil.getNowEpoch());

		// 겜블 포인트 지급
		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_INV;
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}
		friendInvResponse.setADDGP(gamblePoint);

		friendInvResponse.setGD(account.getGold());
		friendInvResponse.setBL(account.getBall());
		friendInvResponse.setPN(account.getPunch());
		friendInvResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));

		return friendInvResponse;
	}

	@Override
	public FriendInvListResponse invFriendList(DateUtil dateUtil,
			Account account) {
		FriendInvListResponse friendInvListResponse = new FriendInvListResponse(
				ReturnCode.SUCCESS);

		final int invDtLimit = dateUtil.getNowEpoch()
				- DebugOption.INV_COOL_TIME_EPOCH;
		List<InvList> invList = invMapper.selectInvList(account.getAppId(),
				invDtLimit);

		for (InvList coolInv : invList) {
			final int nextInvDt = coolInv.getInvDt()
					+ DebugOption.INV_COOL_TIME_EPOCH;
			friendInvListResponse.addF(coolInv.getfAppId(), nextInvDt
					- dateUtil.getNowEpoch());
		}
		return friendInvListResponse;
	}

	@Override
	public FriendInfoResponse getFriendInfo(DateUtil dateUtil, Account account,
			String[] appIds, boolean ISGATE) {
		FriendInfoResponse friendInfoResponse = new FriendInfoResponse(
				ReturnCode.SUCCESS);

		Collection<String> appIdSet = asFidSet(appIds);
		if (appIdSet.isEmpty())
			return friendInfoResponse;

		List<FriendAccount> friendAccountList = accountMapper
				.selectFriendAccount(appIdSet);

		final int nowEpoch = dateUtil.getNowEpoch();
		final int punchCoolTimeLimit = nowEpoch
				- DebugOption.SEND_PUNCH_COOL_TIME_EPOCH;
		Map<String, Punch> punchRegDtMap = new HashMap<String, Punch>();
		List<Punch> punchList = mailMapper.selectPunchList(account.getAppId(),
				punchCoolTimeLimit);
		for (Punch punch : punchList) {
			punchRegDtMap.put(punch.getfAppId(), punch);
		}
		List<Rank> selectUnsordedRank = rankMapper.selectUnsordedRank(
				dateUtil.getThisWeek(), appIdSet);

		if (selectUnsordedRank.size() == 0) {
			rankMapper.removeRankByAppId(account.getAppId());
			rankMapper.insertOrUpdateRankPoint(account.getAppId(),
					dateUtil.getThisWeek(), 0, 0, 0);
			selectUnsordedRank = rankMapper.selectUnsordedRank(
					dateUtil.getThisWeek(), appIdSet);
		}
		if (ISGATE) {

			String[] appIdList = new String[40];
			String[] appIdIMGList = new String[40];
			// for(int i=0;i<=DebugOption.LAST_GATE;i++){
			// appIdIMGList[i] = "";
			// }
			// appIdIMGList[0] =
			// "http://q.qlogo.cn/qqapp/1102962464/012B8DFAC6D7F56EC969654C7BD4A8DD/40,http://q.qlogo.cn/qqapp/1102962464/012B8DFAC6D7F56EC969654C7BD4A8DD/100";
			// appIdIMGList[1] =
			// "http://q.qlogo.cn/qqapp/1102962464/024D4711A0FEE552CE7FFB200D6D8016/40,http://q.qlogo.cn/qqapp/1102962464/024D4711A0FEE552CE7FFB200D6D8016/100";
			// appIdIMGList[2] =
			// "http://q.qlogo.cn/qqapp/1102962464/0691F728A8A9F6CB892D7D158DA1F4B1/40,http://q.qlogo.cn/qqapp/1102962464/0691F728A8A9F6CB892D7D158DA1F4B1/100";

			for (int i = 0; i <= DebugOption.LAST_GATE; i++) {
				List<GateRank> selectUnsordedGateRank = rankMapper
						.selectUnsordedGateRank(dateUtil.getThisWeek(), i,
								appIdSet);
				selectUnsordedGateRank.sort(new Comparator<GateRank>() {
					@Override
					public int compare(GateRank o1, GateRank o2) {
						return -(o1.getPoint() - o2.getPoint());
					}

				});
				if (selectUnsordedGateRank.size() > 0) {
					GateRank gateRank = selectUnsordedGateRank.get(0);
					String appId = gateRank.getAppId();
					appIdList[i] = appId;
				}

			}

			for (int i = 0; i <= DebugOption.LAST_GATE; i++) {
				if (appIdList[i] != null) {
					Account account2 = accountService.getAccount(appIdList[i]);
					if (account2 != null) {
						String img = account2.getImg();
						if (img != null && !img.equals("")) {
							int indexOf = img.indexOf(",");
							if (indexOf != -1) {
								img = img.substring(0, indexOf);
								appIdIMGList[i] = img;
							}
						}

					}
				}

			}

			friendInfoResponse.put("gatelistimg", appIdIMGList);

		}

		selectUnsordedRank.sort(new Comparator<Rank>() {
			@Override
			public int compare(Rank o2, Rank o1) {
				return (int)(o1.getPoint() - o2.getPoint());
			}
		});

		// 把这周没有玩过的好友也加到好友排行中
		List<String> list = new ArrayList<String>();
		for (Rank rank : selectUnsordedRank) {
			list.add(rank.getAppId());
		}
		for (int i = 0; i < appIds.length; i++) {
			if (!list.contains(appIds[i])) {
				Rank r = new Rank();
				r.setAppId(appIds[i]);
				r.setPoint(0);
				r.setGate(0);
				selectUnsordedRank.add(r);
			}
		}

		for (int i = 0; i < selectUnsordedRank.size(); i++) {
			selectUnsordedRank.get(i).setRank(i + 1);
		}
		HashMap<String, Rank> map = new HashMap<String, Rank>();
		for (int i = 0; i < selectUnsordedRank.size(); i++) {
			Rank rank = selectUnsordedRank.get(i);
			map.put(rank.getAppId(), rank);
		}

		for (FriendAccount friendAccount : friendAccountList) {
			Punch coolPunch = punchRegDtMap.get(friendAccount.getAppId());
			// Account acc =
			// accountService.getAccount(friendAccount.getAppId());
			final int punchCoolTime = coolPunch == null ? 0
					: (coolPunch.getRegDt()
							+ DebugOption.SEND_PUNCH_COOL_TIME_EPOCH - nowEpoch);

			// Account ac = accountService.getAccount(friendAccount.getAppId());
			Rank rank = map.get(friendAccount.getAppId());
			if (rank != null)
				friendInfoResponse.addF(friendAccount.getAppId(),
						friendAccount.getChId(), friendAccount.getChLv(),
						friendAccount.getPetId(), friendAccount.getPush() != 0,
						punchCoolTime, friendAccount.getNickname(),
						friendAccount.getImg(), rank.getRank(),
						rank.getPoint(), false, false);
		}

		return friendInfoResponse;
	}

	@Override
	public List<String> getFriendList(String id) {
		return friendlyMapper.selectFriendlyList(id, ISFRIEND);
	}

	@Override
	public List<String> getFriendListReq(String id) {
		return friendlyMapper.selectFriendlyListReq(id, ISNOTFRIEND);
	}

	@Transactional
	@Override
	public boolean addFriend(String fri_self, String fri_op) {
		Friendly selectFriendly = friendlyMapper.selectFriendly(fri_self,
				fri_op);
		if (null != selectFriendly) {
			return false;
		}
		Friendly friendly = new Friendly();
		friendly.setFri_self(fri_self);
		friendly.setFri_op(fri_op);
		friendly.setStatus(ISNOTFRIEND);
		friendlyMapper.insertFriendly(friendly);
		return true;
	}

	@Override
	public List<String> getRequestFriendList(String id) {
		return friendlyMapper.selectFriendlyList(id, ISNOTFRIEND);
	}

	@Override
	public boolean acceptFriendReq(String fri_self, String fri_op) {
		friendlyMapper.updateFriendly(fri_self, fri_op, ISFRIEND);
		return true;
	}

	@Override
	public void deleteFriend(String fri_self, String fri_op, int status) {
		friendlyMapper.deleteFriendly(fri_self, fri_op, status);
	}

}
