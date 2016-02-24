/**
 * 
 */
package com.nfl.kfb.play;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import redis.clients.jedis.Jedis;

import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.dungeon.Dungeon;
import com.nfl.kfb.mapper.dungeon.DungeonMapper;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.logs.FinishPlayLog;
import com.nfl.kfb.mapper.logging.logs.PlayRewardClearLog;
import com.nfl.kfb.mapper.logging.logs.PlayRewardMissionLog;
import com.nfl.kfb.mapper.logging.logs.PlayRewardStarLog;
import com.nfl.kfb.mapper.logging.logs.RandomInvResurrectionLog;
import com.nfl.kfb.mapper.logging.logs.UseItemPlayLog;
import com.nfl.kfb.mapper.rank.GateRank;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.week.WeekMission;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.rank.RankController;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.WeekResource;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.RedisUtil;

/**
 * @author KimSeongsu
 * @since 2013. 6. 27.
 * 
 */
@Service(value = "PlayServiceImpl")
public class PlayServiceImpl extends AbstractKfbService implements PlayService {

	private static final Logger logger = LoggerFactory
			.getLogger(PlayServiceImpl.class);

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private RankMapper rankMapper;

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	private InvenMapper invenMapper;

	@Autowired
	private GambleMapper gambleMapper;

	@Autowired
	private DungeonMapper dungeonMapper;

	@Autowired
	private RankController rankController;

	@Transactional
	@Override
	public StartGameResponse startPlay(DateUtil dateUtil, Account account,
			int playWeek, int playGate, int chId, int chLv, int petId,
			boolean availableGP, int reqPunch) {
		account.regenPunch(dateUtil.getNowEpoch());

		if (account.getPunch() < reqPunch) {
			StartGameResponse startGameResponse = new StartGameResponse(
					ReturnCode.CANNOT_START_NO_PUNCH);
			startGameResponse.setWK(playWeek);
			startGameResponse.setPK(account.getPlayKey());
			startGameResponse.setPN(account.getPunch());
			startGameResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			startGameResponse.setADDGP(0);
			return startGameResponse;
		}

		int playKey = generatePlayKey(dateUtil);
		account.setPlayWeek(playWeek);
		if (playGate < DebugOption.UNLIMIT_GATE) {
			account.setPlayGate(playGate);
		}

		account.setPlayKey(playKey);

		account.setChId(chId);
		account.setChLv(chLv);
		account.setPetId(petId);

		// 주먹 개수 변경
		account.usePunch(dateUtil.getNowEpoch(), reqPunch);

		accountMapper.updateStartPlay(account);

		// 겜블 포인트 지급
		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_START_GAME;
			gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
		}

		StartGameResponse startGameResponse = new StartGameResponse(
				ReturnCode.SUCCESS);
		startGameResponse.setWK(playWeek);
		startGameResponse.setPK(account.getPlayKey());
		startGameResponse.setPN(account.getPunch());
		startGameResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));
		startGameResponse.setADDGP(gamblePoint);

		return startGameResponse;
	}

	/**
	 * //Play Key 생성 - 0은 나오지 않게 -> 삭제<br>
	 * 현재 시각을 기록하는것으로 수정<br>
	 * 
	 * @return
	 */
	private int generatePlayKey(DateUtil dateUtil) {
		// Random random = new Random();
		// int randomValue = random.nextInt();
		// while (randomValue == Account.DEFAULT_PLAY_KEY) {
		// randomValue = random.nextInt();
		// }
		// return randomValue;
		return dateUtil.getNowEpoch();
	}

	@Transactional
	@Override
	public FinishGameResponse finishPlay(DateUtil dateUtil, Account account,
			int gold, int ball, int point, boolean missionRw, String[] fAppIds,
			Integer[] useItemIds, Integer[] useItemCnts, int randomInvCnt,
			Collection<GameLog> gameLogs, boolean availableGP, int shopVer,
			int playGate) {
		logger.debug(
				"FinishGame : APPID={}, WK={}, GATE={}, GOLD={}, BALL={}, POINT={}, RW={}, FID.LEN={}",
				account.getAppId(), account.getPlayWeek(),
				account.getPlayGate(), gold, ball, point, missionRw,
				fAppIds == null ? 0 : fAppIds.length);

		if (account.getBall() < 0 || account.getGold() < 0
				|| account.getPunch() < 0)
			throw new RuntimeException("Wrong now accountItem. NOW_GOLD="
					+ account.getGold() + ", NOW_BALL=" + account.getBall()
					+ ", NOW_PUNCH=" + account.getPunch());

		FinishGameResponse finishGameResponse = new FinishGameResponse(
				ReturnCode.SUCCESS);
		boolean isUnlimit = DebugOption.UNLIMIT_GATE == playGate;
		// 게임중 사용한 아이템
		Set<Inven> useItems = new HashSet<Inven>(); // 게임중 사용한 아이템들
		Set<Inven> addItems = new HashSet<Inven>(); // 보상으로 받을 아이템들

		// 사용한 아이템 계산
		if (useItemIds != null && useItemCnts != null) {
			for (int i = 0; i < useItemIds.length; i++) {
				if (useItemIds[i] == null || useItemCnts[i] == null)
					continue;

				final int useItemId = useItemIds[i].intValue();
				final int useItemCnt = useItemCnts[i].intValue();
				if (useItemId <= 0 || useItemCnt <= 0) {
					continue;
				}

				Shop shop = resourceService.getShop(shopVer, useItemId);
				if (shop == null) {
					continue;
				}

				final ITEM_TYPE itemType = DebugOption.getItemType(useItemId);

				switch (itemType) {
				case RESURRECT:
					if (shop.getPriceType() != DebugOption.SHOP_PRICE_GOLD)
						throw new RuntimeException(
								"Resurrection Item must spend GOLD. check this.");

					addGold(account, (int) -shop.getPrice() * useItemCnt);
					if (account.getGold() < 0)
						throw new RuntimeException(
								"Couldnot use ResurrectItem. You have not enough gold");
					UseItemPlayLog useResurrectionLog = new UseItemPlayLog(
							dateUtil, account.getAppId());
					useResurrectionLog.setItemId(shop.getItemId());
					useResurrectionLog.setItemCnt(useResurrectionLog
							.getItemCnt() - useItemCnt);
					useResurrectionLog.setAddGold(useResurrectionLog
							.getAddGold() - (int) shop.getPrice() * useItemCnt);
					useResurrectionLog.setNowGold(account.getGold());
					useResurrectionLog.setNowBall(account.getBall());
					useResurrectionLog.setNowPunch(account.getPunch());
					gameLogs.add(useResurrectionLog);
					break;
				case SKILL:
				case CONSUME:
					Inven newItem = createInvenItemFromShop(account.getAppId(),
							account.getChId(), shop, useItemCnt, false);
					useItems.add(newItem);
					UseItemPlayLog useItemPlayLog = new UseItemPlayLog(
							dateUtil, account.getAppId());
					useItemPlayLog.setItemId(shop.getItemId());
					useItemPlayLog.setItemCnt(-useItemCnt);
					useItemPlayLog.setNowGold(account.getGold());
					useItemPlayLog.setNowBall(account.getBall());
					useItemPlayLog.setNowPunch(account.getPunch());
					gameLogs.add(useItemPlayLog);
					break;
				default:
					throw new RuntimeException(
							"Could not use this item while playing");
				}
			}
		}
		long newRankPoint = 0;
		boolean notThisWeek = false;
		int gamblePoint = 0;
		if (!isUnlimit) {
			int thisWeek = dateUtil.getThisWeek();
			finishGameResponse.setWK(thisWeek);
			// Week가 안맞다
			// 점수/별/미션/주간업적은 적용하지 않는다. 입수한 엽전/여의주만 적용하자
			notThisWeek = account.getPlayWeek() != thisWeek;

			boolean gotMissionReward = false;

			// 미션보상, 별보상, 점수 갱신
			if (notThisWeek) {
				finishGameResponse.setRC(ReturnCode.WRONG_WEEK);
				finishGameResponse.setRWIT(0, 0, 0);
				finishGameResponse.setRWIT(1, 0, 0);
				finishGameResponse.setST(0, 0, 0);
				finishGameResponse.setLASTGATE(-1, 0, 0);
			} else {
				int starRewardGold = 0, starRewardBall = 0;

				// 현재 관문을 제외한 점수합
				final long sumGateRankPoint = rankMapper.sumGateRankPoint(
						account.getAppId(), account.getPlayWeek(),
						account.getPlayGate());
				// 새 점수합
				newRankPoint = sumGateRankPoint + point;
				// 이전 관문값
				GateRank gateRank = rankMapper.selectGateRank(
						account.getAppId(), account.getPlayWeek(),
						account.getPlayGate());
				int[][] gateStarReward = null;
				if (DebugOption.LAST_GATE >= playGate
						&& account.getPlayGate() <= DebugOption.LAST_GATE) {
					// 별개수 계산
					gateStarReward = DebugOption.GATE_STAR_REWARD[account
							.getPlayGate()]; // 별개수에따른 {기준점수, 보상아이템, 개수}
				} else {
					gateStarReward = DebugOption.GATE_STAR_REWARD[0]; // 별개수에따른
																		// {기준점수,
																		// 보상아이템,
																		// 개수}
				}

				int star = 0; // 지금 별개수
				int starBefore = gateRank == null ? 0 : gateRank.getStar(); // 이전
																			// 별개수

				// 미션을 클리어했을때만 별을 얻을 수 있다.
				if (missionRw) {
					for (int i = 0; i < gateStarReward.length; i++) {
						if (point >= gateStarReward[i][0])
							star = i + 1;
					}
				} else {
					// 미션을 클리어하지 못했으므로 현재 별개수는 점수에 상관없이 0개가 됨
					star = 0;
				}
				logger.debug(
						"FinishGame : APPID={}, point={}, missionClear={}, star={}",
						account.getAppId(), point, missionRw, star);

				// 별개수에 따른 보상 지급
				if (star > starBefore) {
					for (int i = starBefore; i < star; i++) {
						final int rewardItemId = gateStarReward[i][1];
						final int rewardCnt = gateStarReward[i][2];
						int gamblePointInThisStar = 0;
						if (availableGP) {
							gamblePointInThisStar = DebugOption.GAMBLE_POINT_MISSION_STAR[i];
							gamblePoint += gamblePointInThisStar;
						}
						logger.debug(
								"FinishGame : APPID={}, STAR_RW, ID={}, CNT={}, GP={}",
								account.getAppId(), rewardItemId, rewardCnt,
								gamblePointInThisStar);

						if (rewardItemId == 0) {
							continue;
						}

						if (rewardItemId != DebugOption.REWARD_ITEM_GOLD
								&& rewardItemId != DebugOption.REWARD_ITEM_BALL) {
							throw new RuntimeException(
									"Star Reward must be gold or ball");
						}

						Shop shop = resourceService.getShop(shopVer,
								rewardItemId);
						if (shop == null) {
							throw new RuntimeException("Unknown itemId="
									+ rewardItemId);
						}

						if (isAccountItem(shop.getItemId())) {
							PlayRewardStarLog playRewardStarLog = new PlayRewardStarLog(
									dateUtil, account.getAppId());
							addAccountItem(dateUtil, account, shop, rewardCnt,
									playRewardStarLog);

							starRewardGold += playRewardStarLog.getAddGold();
							starRewardBall += playRewardStarLog.getAddBall();

							gameLogs.add(playRewardStarLog);
							playRewardStarLog.setStar(i);
						} else {
							throw new RuntimeException(
									"Star Reward must be gold or ball");
						}
					}
				}
				finishGameResponse.setST(Math.max(star, starBefore),
						starRewardGold, starRewardBall);

				// 미션 보상 지급
				// 이미 받았거나, 별이 1개가 안되면 미션 완료보상은 없음
				if ((gateRank != null && gateRank.getRwDt() > 0) || star < 1) {
					finishGameResponse.setRWIT(0, 0, 0);
					finishGameResponse.setRWIT(1, 0, 0);
					finishGameResponse.setLASTGATE(-1, 0, 0);
				} else {
					gotMissionReward = true;
					WeekResource weekResource = resourceService
							.getWeekResource(account.getPlayWeek());
					WeekMission weekMission = weekResource
							.getWeekMission(account.getPlayGate()); // 해당 관문의
																	// 미션/보상 데이터
					final int[][] missionClearRewardItems = new int[][] {
							{ weekMission.getReward1ItemId(),
									weekMission.getReward1ItemCnt() },
							{ weekMission.getReward2ItemId(),
									weekMission.getReward2ItemCnt() } };

					for (int i = 0; i < missionClearRewardItems.length; i++) {
						final int itemId = missionClearRewardItems[i][0];
						final int itemCnt = missionClearRewardItems[i][1];

						logger.debug(
								"FinishGame : APPID={}, MISSION_RW, ID={}, CNT={}",
								account.getAppId(), itemId, itemCnt);

						if (itemId == 0 || itemCnt <= 0) { // 보상아이템이 없는것
							finishGameResponse.setRWIT(i, 0, 0);
							continue;
						}

						Shop shop = resourceService.getShop(shopVer, itemId);
						if (shop == null) {
							throw new RuntimeException("Unknown itemId="
									+ itemId);
						}

						if (isAccountItem(shop.getItemId())) {
							PlayRewardMissionLog playRewardMissionLog = new PlayRewardMissionLog(
									dateUtil, account.getAppId());
							addAccountItem(dateUtil, account, shop, itemCnt,
									playRewardMissionLog);
							finishGameResponse.setRWIT(i, shop.getItemId(),
									itemCnt);
							gameLogs.add(playRewardMissionLog);
						} else if (isInvenItem(shop.getItemId())) {
							Inven newItem = createInvenItemFromShop(
									account.getAppId(), account.getChId(),
									shop, itemCnt, false);
							addItems.add(newItem);
							finishGameResponse.setRWIT(i, newItem.getItemId(),
									itemCnt);
							PlayRewardMissionLog playRewardMissionLog = new PlayRewardMissionLog(
									dateUtil, account.getAppId());
							playRewardMissionLog.setItemId(shop.getItemId());
							playRewardMissionLog.setItemCnt(itemCnt);
							gameLogs.add(playRewardMissionLog);
						}
					}

					// 마지막 관문일 경우 마지막관문 추가 보상 지급 - 등수안에 들어야됨
					if (account.getPlayGate() != DebugOption.LAST_GATE) {
						finishGameResponse.setLASTGATE(-1, 0, 0);
					} else {
						// 전체랭크를 조회할 appId
						Set<String> appIdForRank = asFidSet(fAppIds);
						int gateAllClearRank;
						if (appIdForRank.isEmpty()) {
							gateAllClearRank = 0; // 친구없음. 내가 1등임
						} else {
							gateAllClearRank = rankMapper
									.countGateAllClearFriends(
											account.getAppId(),
											account.getPlayWeek(),
											newRankPoint, appIdForRank,
											DebugOption.LAST_GATE);
						}

						int[] gateClearReward = DebugOption
								.getGateAllClearReward(gateAllClearRank);
						final int gateClearRewardItemId = gateClearReward[0];
						final int gateClearRewardCnt = gateClearReward[1];
						logger.debug(
								"FinishGame : APPID={}, GATE_CLEAR_RW, RANK={}, ID={}, CNT={}",
								account.getAppId(), gateAllClearRank,
								gateClearRewardItemId, gateClearRewardCnt);

						if (gateClearRewardItemId != 0
								&& gateClearRewardCnt > 0) {
							Shop shop = resourceService.getShop(shopVer,
									gateClearRewardItemId);
							if (shop != null) {
								if (isAccountItem(shop.getItemId())) {
									PlayRewardClearLog playRewardClearLog = new PlayRewardClearLog(
											dateUtil, account.getAppId());
									playRewardClearLog
											.setRank(gateAllClearRank + 1);
									addAccountItem(dateUtil, account, shop,
											gateClearRewardCnt,
											playRewardClearLog);
									finishGameResponse.setLASTGATE(
											gateAllClearRank + 1,
											shop.getItemId(),
											gateClearRewardCnt);
									gameLogs.add(playRewardClearLog);
								}

								if (isInvenItem(shop.getItemId())) {
									Inven newItem = createInvenItemFromShop(
											account.getAppId(),
											account.getChId(), shop,
											gateClearRewardCnt, false);
									addItems.add(newItem);
									finishGameResponse.setLASTGATE(
											gateAllClearRank + 1,
											newItem.getItemId(),
											gateClearRewardCnt);
									PlayRewardClearLog playRewardClearLog = new PlayRewardClearLog(
											dateUtil, account.getAppId());
									playRewardClearLog.setItemId(shop
											.getItemId());
									playRewardClearLog
											.setItemCnt(gateClearRewardCnt);
									playRewardClearLog
											.setRank(gateAllClearRank + 1);
									gameLogs.add(playRewardClearLog);
								}
							}
						}
					}
				}

				// 점수 갱신
				if (gateRank == null) {
					rankMapper.insertGateRank(account.getAppId(), account
							.getPlayWeek(), account.getPlayGate(), point,
							gotMissionReward ? dateUtil.getNowEpoch() : 0, Math
									.max(star, starBefore));
				} else {
					rankMapper.updateGateRank(account.getAppId(), account
							.getPlayWeek(), account.getPlayGate(), Math.max(
							gateRank.getPoint(), point),
							gotMissionReward ? dateUtil.getNowEpoch()
									: gateRank.getRwDt(), Math.max(star,
									gateRank.getStar()));
				}
			}
			// 전체랭킹 점수 update

			rankMapper.insertOrUpdateRankPoint(account.getAppId(),
					account.getPlayWeek(), account.getPlayGate(), newRankPoint,
					Rank.DEFAULT_REWARD_DT);
			Jedis redis = DebugOption.getJedisPool().getResource();
			Double zscore = redis.zscore(DebugOption.REDIS_RANK_KEY,
					account.getAppId());
			int zs = -1;
			if (zscore != null) {
				zs = zscore.intValue();
			}
			if (zs < newRankPoint) {
				redis.zadd(DebugOption.REDIS_RANK_KEY, newRankPoint,
						account.getAppId());
			}
			DebugOption.getJedisPool().returnResource(redis);

		} else if (playGate > DebugOption.UNLIMIT_GATE) {
			// 竞技模式

		} else {
			long newPoint = rankController.submitUnlimitRankPoint(
					account.getAppId(), point);
			if (newPoint != point) {
				Jedis redis = DebugOption.getJedisPool().getResource();
				redis.zadd(DebugOption.REDIS_UNLIMIT_RNANK_KEY, newPoint,
						account.getAppId());
				DebugOption.getJedisPool().returnResource(redis);
			}

		}

		// 랜덤 친구초대 부활
		if (randomInvCnt > 0) {
			RandomInvResurrectionLog randomInvResurrectionLog = new RandomInvResurrectionLog(
					dateUtil, account.getAppId());
			randomInvResurrectionLog.setInv(randomInvCnt);
			gameLogs.add(randomInvResurrectionLog);
		}

		// 게임끝 로그
		FinishPlayLog finishPlayLog = new FinishPlayLog(dateUtil,
				account.getAppId());
		finishPlayLog.setGate(account.getPlayGate());
		finishPlayLog.setPoint(notThisWeek ? -point : point); // 이번주가 아니면 로그에
																// -점수로 남김
		finishPlayLog.setPlayTime(Math.max(1,
				dateUtil.getNowEpoch() - account.getPlayKey())); // 플레이시간 - 최소
																	// 1초

		account.setPlayKey(Account.DEFAULT_PLAY_KEY); // reset

		// +엽전, +여의주, PlayKey 리셋
		if (gold > 0) {
			addGold(account, gold);
			finishPlayLog.setAddGold(finishPlayLog.getAddGold() + gold);
		}
		if (ball > 0) {
			addBall(account, ball);
			finishPlayLog.setAddBall(finishPlayLog.getAddBall() + ball);
		}
		finishPlayLog.setNowGold(account.getGold());
		finishPlayLog.setNowBall(account.getBall());
		finishPlayLog.setNowPunch(account.getPunch());
		gameLogs.add(finishPlayLog);

		finishGameResponse.setGD(account.getGold());
		finishGameResponse.setBL(account.getBall());
		finishGameResponse.setPN(account.getPunch());
		finishGameResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));

		// 인벤아이템 지급/사용
		if (!addItems.isEmpty() || !useItems.isEmpty()) { // 업데이트할 아이템이 있을경우
			FinishGameItemManager finishGameItemManager = new FinishGameItemManager(
					invenMapper, account.getAppId());
			for (Inven rewardItem : addItems) {
				finishGameItemManager.addRewardItem(rewardItem);
			}
			for (Inven useItem : useItems) {
				finishGameItemManager.useItem(useItem);
			}
			finishGameItemManager.commit(invenMapper);
		}

		// account 는 무조건 update
		accountMapper.updateFinishPlay(account);
		if (!isUnlimit) {
			// 겜블 포인트 지급
			if (gamblePoint > 0) {
				gambleMapper.increaseGamblePoint(account.getAppId(),
						gamblePoint);
			}
		}
		finishGameResponse.setADDGP(gamblePoint);

		return finishGameResponse;
	}

	@Override
	public BoastResponse boast(DateUtil dateUtil, Account account, String fID) {
		gambleMapper.increaseGamblePoint(account.getAppId(),
				DebugOption.GAMBLE_POINT_BOAST);

		BoastResponse boastResponse = new BoastResponse(ReturnCode.SUCCESS);
		boastResponse.setADDGP(DebugOption.GAMBLE_POINT_BOAST);

		return boastResponse;
	}

	// @Transactional
	// @Override
	// public StartDungeonResponse startUnlimit(DateUtil dateUtil,
	// Account account, int chId, int chLv, int petId) {
	// account.regenPunch(dateUtil.getNowEpoch());
	//
	// Dungeon dungeon = dungeonMapper.selectDungeon(account.getAppId());
	// if (dungeon == null)
	// throw new RuntimeException("Dungeon is empty. APPID="
	// + account.getAppId());
	//
	// // 날짜가 넘어가서 던전횟수가 초기화 되었는지 확인
	// if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()) {
	// dungeon.setPlayDt(dateUtil.getNowEpoch());
	// dungeon.setPlayCnt(0);
	// }
	//
	// // 플레이 횟수가 제한에 걸리나
	// if (dungeon.getPlayCnt() >= dungeon.getPlayLimit()) {
	// StartDungeonResponse startDungeonResponse = new StartDungeonResponse(
	// ReturnCode.NOT_ENOUGH_DUNGEON_CNT);
	// startDungeonResponse.setPK(0);
	// startDungeonResponse.setPN(account.getPunch());
	// startDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
	// .getNowEpoch()));
	// startDungeonResponse.setADDGP(0);
	// startDungeonResponse.setDungeon(dungeon.getPlayLimit(),
	// dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
	// - dateUtil.getNowEpoch(), dungeon.getPunch());
	// return startDungeonResponse;
	// }
	//
	// // 주먹 부족
	// if (account.getPunch() < dungeon.getPunch()) {
	// StartDungeonResponse startDungeonResponse = new StartDungeonResponse(
	// ReturnCode.CANNOT_START_NO_PUNCH);
	// startDungeonResponse.setPK(0);
	// startDungeonResponse.setPN(account.getPunch());
	// startDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
	// .getNowEpoch()));
	// startDungeonResponse.setADDGP(0);
	// startDungeonResponse.setDungeon(dungeon.getPlayLimit(),
	// dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
	// - dateUtil.getNowEpoch(), dungeon.getPunch());
	// return startDungeonResponse;
	// }
	//
	// int playKey = generatePlayKey(dateUtil);
	// int playWeek = dateUtil.getThisWeek();
	// account.setPlayWeek(playWeek);
	// account.setPlayGate(DebugOption.DUNGEON_GATE_NUMBER);
	// account.setPlayKey(playKey);
	//
	// account.setChId(chId);
	// account.setChLv(chLv);
	// account.setPetId(petId);
	//
	// // 주먹 개수 변경
	// account.usePunch(dateUtil.getNowEpoch(), dungeon.getPunch());
	// accountMapper.updateStartPlay(account);
	//
	// // 겜블 포인트 지급
	// int gamblePoint = DebugOption.GAMBLE_POINT_START_DUNGEON;
	// gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);
	//
	// // 던전 정보 변경
	// dungeon.setPlayDt(dateUtil.getNowEpoch());
	// dungeon.setPlayCnt(dungeon.getPlayCnt() + 1);
	// dungeonMapper.updateDungeon(dungeon);
	//
	// StartDungeonResponse startDungeonResponse = new StartDungeonResponse(
	// ReturnCode.SUCCESS);
	// startDungeonResponse.setPK(account.getPlayKey());
	// startDungeonResponse.setPN(account.getPunch());
	// startDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
	// .getNowEpoch()));
	// startDungeonResponse.setADDGP(gamblePoint);
	// startDungeonResponse.setDungeon(dungeon.getPlayLimit(),
	// dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
	// - dateUtil.getNowEpoch(), dungeon.getPunch());
	// return startDungeonResponse;
	// }

	// @Override
	// public FinishDungeonResponse finishUnlimit(DateUtil dateUtil,
	// Account account, int gold, int ball, Integer[] useItemIds,
	// Integer[] useItemCnts, Collection<GameLog> gameLogs,
	// int dungeonLimit, int dungeonPunch, int shopVer) {
	// logger.debug("FinishDungeon : APPID={}, GOLD={}, BALL={}",
	// account.getAppId(), gold, ball);
	//
	// if (account.getBall() < 0 || account.getGold() < 0
	// || account.getPunch() < 0)
	// throw new RuntimeException("Wrong now accountItem. NOW_GOLD="
	// + account.getGold() + ", NOW_BALL=" + account.getBall()
	// + ", NOW_PUNCH=" + account.getPunch());
	//
	// Dungeon dungeon = dungeonMapper.selectDungeon(account.getAppId());
	// if (dungeon == null)
	// throw new RuntimeException("Dungeon is empty. APPID="
	// + account.getAppId());
	//
	// FinishDungeonResponse finishDungeonResponse = new FinishDungeonResponse(
	// ReturnCode.SUCCESS);
	//
	// // 게임중 사용한 아이템
	// Set<Inven> useItems = new HashSet<Inven>(); // 게임중 사용한 아이템들
	//
	// // 사용한 아이템 계산
	// if (useItemIds != null && useItemCnts != null) {
	// for (int i = 0; i < useItemIds.length; i++) {
	// if (useItemIds[i] == null || useItemCnts[i] == null)
	// continue;
	//
	// final int useItemId = useItemIds[i].intValue();
	// final int useItemCnt = useItemCnts[i].intValue();
	// if (useItemId <= 0 || useItemCnt <= 0) {
	// continue;
	// }
	//
	// Shop shop = resourceService.getShop(shopVer, useItemId);
	//
	// final ITEM_TYPE itemType = DebugOption.getItemType(useItemId);
	//
	// switch (itemType) {
	// case RESURRECT:
	// if (shop.getPriceType() != DebugOption.SHOP_PRICE_GOLD)
	// throw new RuntimeException(
	// "Resurrection Item must spend GOLD. check this.");
	//
	// addGold(account, (int) -shop.getPrice() * useItemCnt);
	// if (account.getGold() < 0)
	// throw new RuntimeException(
	// "Couldnot use ResurrectItem. You have not enough gold");
	// UseItemPlayLog useResurrectionLog = new UseItemPlayLog(
	// dateUtil, account.getAppId());
	// useResurrectionLog.setItemId(shop.getItemId());
	// useResurrectionLog.setItemCnt(useResurrectionLog
	// .getItemCnt() - useItemCnt);
	// useResurrectionLog.setAddGold(useResurrectionLog
	// .getAddGold() - (int) shop.getPrice() * useItemCnt);
	// useResurrectionLog.setNowGold(account.getGold());
	// useResurrectionLog.setNowBall(account.getBall());
	// useResurrectionLog.setNowPunch(account.getPunch());
	// gameLogs.add(useResurrectionLog);
	// break;
	// case SKILL:
	// case CONSUME:
	// Inven newItem = createInvenItemFromShop(account.getAppId(),
	// account.getChId(), shop, useItemCnt, false);
	// useItems.add(newItem);
	// UseItemPlayLog useItemPlayLog = new UseItemPlayLog(
	// dateUtil, account.getAppId());
	// useItemPlayLog.setItemId(shop.getItemId());
	// useItemPlayLog.setItemCnt(-useItemCnt);
	// useItemPlayLog.setNowGold(account.getGold());
	// useItemPlayLog.setNowBall(account.getBall());
	// useItemPlayLog.setNowPunch(account.getPunch());
	// gameLogs.add(useItemPlayLog);
	// break;
	// default:
	// throw new RuntimeException(
	// "Could not use this item while playing");
	// }
	// }
	// }
	//
	// // 게임끝 로그
	// FinishPlayLog finishPlayLog = new FinishPlayLog(dateUtil,
	// account.getAppId());
	// finishPlayLog.setGate(DebugOption.DUNGEON_GATE_NUMBER);
	// finishPlayLog.setPoint(0); // 던전은 점수가 없다
	// finishPlayLog.setPlayTime(Math.max(1,
	// dateUtil.getNowEpoch() - account.getPlayKey())); // 플레이시간 - 최소
	// // 1초
	//
	// account.setPlayKey(Account.DEFAULT_PLAY_KEY); // reset
	//
	// // +엽전, +여의주, PlayKey 리셋
	// if (gold > 0) {
	// addGold(account, gold);
	// finishPlayLog.setAddGold(finishPlayLog.getAddGold() + gold);
	// }
	// if (ball > 0) {
	// addBall(account, ball);
	// finishPlayLog.setAddBall(finishPlayLog.getAddBall() + ball);
	// }
	// finishPlayLog.setNowGold(account.getGold());
	// finishPlayLog.setNowBall(account.getBall());
	// finishPlayLog.setNowPunch(account.getPunch());
	// gameLogs.add(finishPlayLog);
	//
	// finishDungeonResponse.setGD(account.getGold());
	// finishDungeonResponse.setBL(account.getBall());
	// finishDungeonResponse.setPN(account.getPunch());
	// finishDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
	// .getNowEpoch()));
	//
	// // 인벤아이템 지급/사용
	// if (!useItems.isEmpty()) { // 업데이트할 아이템이 있을경우
	// FinishGameItemManager finishGameItemManager = new FinishGameItemManager(
	// invenMapper, account.getAppId());
	// for (Inven useItem : useItems) {
	// finishGameItemManager.useItem(useItem);
	// }
	// finishGameItemManager.commit(invenMapper);
	// }
	//
	// // account 는 무조건 update
	// accountMapper.updateFinishPlay(account);
	//
	// // 던전 정보 갱신
	// // if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()
	// // || dungeon.getPlayLimit() != dungeonLimit
	// // || dungeon.getPunch() != dungeonPunch) {
	// // if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()) {
	// // dungeon.setPlayDt(dateUtil.getNowEpoch());
	// // dungeon.setPlayCnt(0);
	// // }
	// // dungeon.setPlayLimit(dungeonLimit);
	// // dungeon.setPunch(dungeonPunch);
	// // dungeonMapper.updateDungeon(dungeon);
	// // }
	//
	// finishDungeonResponse.setDungeon(dungeon.getPlayLimit(),
	// dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
	// - dateUtil.getNowEpoch(), dungeon.getPunch());
	//
	// return finishDungeonResponse;
	// }

	@Transactional
	@Override
	public StartDungeonResponse startDungeon(DateUtil dateUtil,
			Account account, int chId, int chLv, int petId) {
		account.regenPunch(dateUtil.getNowEpoch());

		Dungeon dungeon = dungeonMapper.selectDungeon(account.getAppId());
		if (dungeon == null)
			throw new RuntimeException("Dungeon is empty. APPID="
					+ account.getAppId());

		// 날짜가 넘어가서 던전횟수가 초기화 되었는지 확인
		if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()) {
			dungeon.setPlayDt(dateUtil.getNowEpoch());
			dungeon.setPlayCnt(0);
		}

		// 플레이 횟수가 제한에 걸리나
		if (dungeon.getPlayCnt() >= dungeon.getPlayLimit()) {
			StartDungeonResponse startDungeonResponse = new StartDungeonResponse(
					ReturnCode.NOT_ENOUGH_DUNGEON_CNT);
			startDungeonResponse.setPK(0);
			startDungeonResponse.setPN(account.getPunch());
			startDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			startDungeonResponse.setADDGP(0);
			startDungeonResponse.setDungeon(dungeon.getPlayLimit(),
					dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
							- dateUtil.getNowEpoch(), dungeon.getPunch());
			return startDungeonResponse;
		}

		// 주먹 부족
		if (account.getPunch() < dungeon.getPunch()) {
			StartDungeonResponse startDungeonResponse = new StartDungeonResponse(
					ReturnCode.CANNOT_START_NO_PUNCH);
			startDungeonResponse.setPK(0);
			startDungeonResponse.setPN(account.getPunch());
			startDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			startDungeonResponse.setADDGP(0);
			startDungeonResponse.setDungeon(dungeon.getPlayLimit(),
					dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
							- dateUtil.getNowEpoch(), dungeon.getPunch());
			return startDungeonResponse;
		}

		int playKey = generatePlayKey(dateUtil);
		int playWeek = dateUtil.getThisWeek();
		account.setPlayWeek(playWeek);
		account.setPlayGate(DebugOption.DUNGEON_GATE_NUMBER);
		account.setPlayKey(playKey);

		account.setChId(chId);
		account.setChLv(chLv);
		account.setPetId(petId);

		// 주먹 개수 변경
		account.usePunch(dateUtil.getNowEpoch(), dungeon.getPunch());
		accountMapper.updateStartPlay(account);

		// 겜블 포인트 지급
		int gamblePoint = DebugOption.GAMBLE_POINT_START_DUNGEON;
		gambleMapper.increaseGamblePoint(account.getAppId(), gamblePoint);

		// 던전 정보 변경
		dungeon.setPlayDt(dateUtil.getNowEpoch());
		dungeon.setPlayCnt(dungeon.getPlayCnt() + 1);
		dungeonMapper.updateDungeon(dungeon);

		StartDungeonResponse startDungeonResponse = new StartDungeonResponse(
				ReturnCode.SUCCESS);
		startDungeonResponse.setPK(account.getPlayKey());
		startDungeonResponse.setPN(account.getPunch());
		startDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));
		startDungeonResponse.setADDGP(gamblePoint);
		startDungeonResponse.setDungeon(dungeon.getPlayLimit(),
				dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
						- dateUtil.getNowEpoch(), dungeon.getPunch());
		return startDungeonResponse;
	}

	@Override
	public FinishDungeonResponse finishDungeon(DateUtil dateUtil,
			Account account, int gold, int ball, Integer[] useItemIds,
			Integer[] useItemCnts, Collection<GameLog> gameLogs,
			int dungeonLimit, int dungeonPunch, int shopVer) {
		logger.debug("FinishDungeon : APPID={}, GOLD={}, BALL={}",
				account.getAppId(), gold, ball);

		if (account.getBall() < 0 || account.getGold() < 0
				|| account.getPunch() < 0)
			throw new RuntimeException("Wrong now accountItem. NOW_GOLD="
					+ account.getGold() + ", NOW_BALL=" + account.getBall()
					+ ", NOW_PUNCH=" + account.getPunch());

		Dungeon dungeon = dungeonMapper.selectDungeon(account.getAppId());
		if (dungeon == null)
			throw new RuntimeException("Dungeon is empty. APPID="
					+ account.getAppId());

		FinishDungeonResponse finishDungeonResponse = new FinishDungeonResponse(
				ReturnCode.SUCCESS);

		// 게임중 사용한 아이템
		Set<Inven> useItems = new HashSet<Inven>(); // 게임중 사용한 아이템들

		// 사용한 아이템 계산
		if (useItemIds != null && useItemCnts != null) {
			for (int i = 0; i < useItemIds.length; i++) {
				if (useItemIds[i] == null || useItemCnts[i] == null)
					continue;

				final int useItemId = useItemIds[i].intValue();
				final int useItemCnt = useItemCnts[i].intValue();
				if (useItemId <= 0 || useItemCnt <= 0) {
					continue;
				}

				Shop shop = resourceService.getShop(shopVer, useItemId);

				final ITEM_TYPE itemType = DebugOption.getItemType(useItemId);

				switch (itemType) {
				case RESURRECT:
					if (shop.getPriceType() != DebugOption.SHOP_PRICE_GOLD)
						throw new RuntimeException(
								"Resurrection Item must spend GOLD. check this.");

					addGold(account, (int) -shop.getPrice() * useItemCnt);
					if (account.getGold() < 0)
						throw new RuntimeException(
								"Couldnot use ResurrectItem. You have not enough gold");
					UseItemPlayLog useResurrectionLog = new UseItemPlayLog(
							dateUtil, account.getAppId());
					useResurrectionLog.setItemId(shop.getItemId());
					useResurrectionLog.setItemCnt(useResurrectionLog
							.getItemCnt() - useItemCnt);
					useResurrectionLog.setAddGold(useResurrectionLog
							.getAddGold() - (int) shop.getPrice() * useItemCnt);
					useResurrectionLog.setNowGold(account.getGold());
					useResurrectionLog.setNowBall(account.getBall());
					useResurrectionLog.setNowPunch(account.getPunch());
					gameLogs.add(useResurrectionLog);
					break;
				case SKILL:
				case CONSUME:
					Inven newItem = createInvenItemFromShop(account.getAppId(),
							account.getChId(), shop, useItemCnt, false);
					useItems.add(newItem);
					UseItemPlayLog useItemPlayLog = new UseItemPlayLog(
							dateUtil, account.getAppId());
					useItemPlayLog.setItemId(shop.getItemId());
					useItemPlayLog.setItemCnt(-useItemCnt);
					useItemPlayLog.setNowGold(account.getGold());
					useItemPlayLog.setNowBall(account.getBall());
					useItemPlayLog.setNowPunch(account.getPunch());
					gameLogs.add(useItemPlayLog);
					break;
				default:
					throw new RuntimeException(
							"Could not use this item while playing");
				}
			}
		}

		// 게임끝 로그
		FinishPlayLog finishPlayLog = new FinishPlayLog(dateUtil,
				account.getAppId());
		finishPlayLog.setGate(DebugOption.DUNGEON_GATE_NUMBER);
		finishPlayLog.setPoint(0); // 던전은 점수가 없다
		finishPlayLog.setPlayTime(Math.max(1,
				dateUtil.getNowEpoch() - account.getPlayKey())); // 플레이시간 - 최소
																	// 1초

		account.setPlayKey(Account.DEFAULT_PLAY_KEY); // reset

		// +엽전, +여의주, PlayKey 리셋
		if (gold > 0) {
			addGold(account, gold);
			finishPlayLog.setAddGold(finishPlayLog.getAddGold() + gold);
		}
		if (ball > 0) {
			addBall(account, ball);
			finishPlayLog.setAddBall(finishPlayLog.getAddBall() + ball);
		}
		finishPlayLog.setNowGold(account.getGold());
		finishPlayLog.setNowBall(account.getBall());
		finishPlayLog.setNowPunch(account.getPunch());
		gameLogs.add(finishPlayLog);

		finishDungeonResponse.setGD(account.getGold());
		finishDungeonResponse.setBL(account.getBall());
		finishDungeonResponse.setPN(account.getPunch());
		finishDungeonResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));

		// 인벤아이템 지급/사용
		if (!useItems.isEmpty()) { // 업데이트할 아이템이 있을경우
			FinishGameItemManager finishGameItemManager = new FinishGameItemManager(
					invenMapper, account.getAppId());
			for (Inven useItem : useItems) {
				finishGameItemManager.useItem(useItem);
			}
			finishGameItemManager.commit(invenMapper);
		}

		// account 는 무조건 update
		accountMapper.updateFinishPlay(account);

		// 던전 정보 갱신
		if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()
				|| dungeon.getPlayLimit() != dungeonLimit
				|| dungeon.getPunch() != dungeonPunch) {
			if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()) {
				dungeon.setPlayDt(dateUtil.getNowEpoch());
				dungeon.setPlayCnt(0);
			}
			dungeon.setPlayLimit(dungeonLimit);
			dungeon.setPunch(dungeonPunch);
			dungeonMapper.updateDungeon(dungeon);
		}

		finishDungeonResponse.setDungeon(dungeon.getPlayLimit(),
				dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
						- dateUtil.getNowEpoch(), dungeon.getPunch());

		return finishDungeonResponse;
	}

}
