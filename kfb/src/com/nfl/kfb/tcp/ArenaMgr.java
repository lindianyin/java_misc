package com.nfl.kfb.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.mina.core.session.IoSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.GameLog.GAMELOG_TYPE;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.task.ArenaResult;
import com.nfl.kfb.mapper.task.BaseTaskMapper;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

//竞技大厅管理器
@Controller
public class ArenaMgr {

	private static ArenaMgr arenaMgr = null;

	private PkArea[] pkArea = new PkArea[4];

	//private Timer timer = null;

	private static final int TIME_OUT = 60 * 10;

	private static Object lock = new Object();

	private BaseTaskMapper baseTaskMapper;

	private AccountMapper accountMapper;

	private ResourceService resourceService;

	private LoggingService loggingService;

	public BaseTaskMapper getBaseTaskMapper() {
		return baseTaskMapper;
	}

	public void setBaseTaskMapper(BaseTaskMapper baseTaskMapper) {
		this.baseTaskMapper = baseTaskMapper;
	}

	public AccountMapper getAccountMapper() {
		return accountMapper;
	}

	public void setAccountMapper(AccountMapper accountMapper) {
		this.accountMapper = accountMapper;
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public LoggingService getLoggingService() {
		return loggingService;
	}

	public void setLoggingService(LoggingService loggingService) {
		this.loggingService = loggingService;
	}

//	public Timer getTimer() {
//		return timer;
//	}
//
//	public void setTimer(Timer timer) {
//		this.timer = timer;
//	}

	private ArenaMgr() throws IOException {
		for (int i = 0; i < pkArea.length; i++) {
			pkArea[i] = new PkArea();
		}
//		timer = new Timer();
//		timer.schedule(new TimerTask() {
//			@Override
//			public void run() {
//				synchronized (lock) {
//					loop();
//				}
//			}
//		}, 1000, 1000);

	}

	public static ArenaMgr getInstance() {
		synchronized (lock) {
			if (arenaMgr == null) {
				try {
					arenaMgr = new ArenaMgr();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return arenaMgr;
		}
	}

	// 移除玩家
	public void removePlayer(String appId) {
		synchronized (lock) {

			for (int i = 0; i < pkArea.length; i++) {
				PkArea pkArean = pkArea[i];
				Map<String, Player> preMatch = pkArean.getPreMatch();
				Player player = preMatch.get(appId);
				if (player != null) {
					preMatch.remove(appId);
				}
			}
		}
	}

	// 获取在场中的待匹配人数
	public int[] getCountOfPlayer() {
		int[] countArr = new int[4];
		for (int i = 0; i < pkArea.length; i++) {
			int count = pkArea[i].getPreMatch().size();
			countArr[i] = count;
		}
		return countArr;
	}

	// 进入某一个场
	public void enterSomeArea(String appId, int idx) {
		synchronized (lock) {
			removePlayer(appId);
			Map<String, Player> preMatch = pkArea[idx].getPreMatch();
			Account account = ArenaMgr.getInstance().getAccountMapper()
					.selectAccount(appId);

			String nickname = account.getNickname();
			String img = account.getImg();

			Player player = new Player();
			player.setAppId(appId);
			player.setNickName(nickname);
			player.setImg(img);
			player.setMatchDate(new Date());
			preMatch.put(appId, player);
			IoSession session = SessionMgr.getInstance().getSession(appId);
			if (session != null) {
				session.setAttribute("areaIdx", idx);
			}
		}
		// Player p = new Player();
		// p.setAppId("CC6126F75E78522C60FCF0E032AF50EA");
		// p.setDate(new Date());
		// p.setImg("http://q.qlogo.cn/qqapp/1102962464/A3EC5F429AF57126EF869FB1CD730825/40,http://q.qlogo.cn/qqapp/1102962464/A3EC5F429AF57126EF869FB1CD730825/100");
		// p.setNickName("Vanilla♪Sky");
		// p.setOver(true);
		// p.setPoint(2000);
		// pkArea[0].getPreMatch().put("CC6126F75E78522C60FCF0E032AF50EA", p);

	}

	// 系统匹配玩家
	public void match() {
		for (int i = 0; i < pkArea.length; i++) {
			// System.out.println(i);
			PkArea area = pkArea[i];
			Map<String, Player> preMatch = area.getPreMatch();
			Set<String> keySet = preMatch.keySet();
			List<String> list = new ArrayList<String>();
			Set<Entry<String, Player>> preMatchEntrySet = preMatch.entrySet();
			long nowSecond = new Date().toInstant().getEpochSecond();
			ArrayList<String> timeOutPreMatchList = new ArrayList<String>();
			for (Entry<String, Player> entry : preMatchEntrySet) {
				long matchEpochSecond = entry.getValue().getMatchDate()
						.toInstant().getEpochSecond();
				if (nowSecond - matchEpochSecond > 20) {
					timeOutPreMatchList.add(entry.getKey());
				}
			}
			for (String appId : timeOutPreMatchList) {
				// 发送退出匹配
				SessionMgr.getInstance().sendMsg(appId, CMD.EXIT_MATCH,
						new Object[] {});
				preMatch.remove(appId);
				keySet.remove(appId);
			}

			for (String str : keySet) {
				list.add(str);
			}
			Collections.shuffle(list);
			if (list.size() < 2) {
				continue;
				// return;
			}
			if (list.size() % 2 != 0) {
				list.remove(0);
			}

			Map<Long, Map<String, Player>> matched = area.getMatched();
			for (int j = 0; j < list.size(); j += 2) {
				long maxArenaId = DebugOption.getMaxArenaId();
				Map<String, Player> players = new HashMap<String, Player>();
				String appId = list.get(j);
				String appId1 = list.get(j + 1);
				Player p = preMatch.get(appId);
				Player p1 = preMatch.get(appId1);
				p.setDate(new Date());
				p1.setDate(new Date());

				players.put(p.getAppId(), p);
				players.put(p1.getAppId(), p1);
				matched.put(maxArenaId, players);
			}
			for (String str : list) {
				preMatch.remove(str);
			}

			Set<Entry<Long, Map<String, Player>>> entrySet = matched.entrySet();

			for (Entry<Long, Map<String, Player>> entry : entrySet) {
				String[] strArr = entry.getValue().keySet()
						.toArray(new String[0]);
				for (String appId : strArr) {
					SessionMgr.getInstance().sendMsg(appId, CMD.MATCH_SUCCESS,
							new Object[] { entry.getKey(), entry.getValue() });
					int idx = i;
					int ret = ArenaMgr.getInstance().costGoldAndBall(idx,
							appId, true);
					if (ret != 0) {
						try {
							throw new Exception("gold or ball is not enough!!!");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
			Map<Long, Map<String, Player>> playing = area.getPlaying();
			for (Entry<Long, Map<String, Player>> entry : entrySet) {
				playing.put(entry.getKey(), entry.getValue());
			}
			matched.clear();
		}
	}

	// 提交分数(每隔10秒左右提交并同步一次各自的分数)
	public Map<String, Boolean> submitPoint(String appId, long id,
			boolean isOver, int point, int idx) {
		synchronized (lock) {
			System.out.println("--------------------------------" + "appId:"
					+ appId + "submit_point:" + point + "isOver" + isOver
					+ "idx " + idx);
			// int idx = (int) SessionMgr.getInstance().getSession(appId)
			// .getAttribute(Arena.AREA_IDX);
			Map<Long, Map<String, Player>> playing = pkArea[idx].getPlaying();
			Map<String, Player> map = playing.get(id);
			if (map == null) {
				return null;
			}
			Player player = map.get(appId);
			if (player == null) {
				return null;
			}
			player.setOver(isOver);
			player.setPoint(point);

			String[] appIdList = map.keySet().toArray(new String[0]);

			// 发送两个人的分数到对方
			Player p1 = map.get(appIdList[0]);
			Player p2 = map.get(appIdList[1]);

			if (p1 == null || p2 == null) {
				return null;
			}
			// p1.setOver(isOver);
			// p2.setOver(isOver);
			HashMap<String, Boolean> _map = new HashMap<>();
			_map.put(p1.getAppId(), p1.isOver());
			_map.put(p2.getAppId(), p2.isOver());
			return _map;
		}

	}

	private void judge() {
		for (int i = 0; i < pkArea.length; i++) {
			// System.out.println(i);
			PkArea area = pkArea[i];
			Map<Long, Map<String, Player>> playing = area.getPlaying();
			Set<Entry<Long, Map<String, Player>>> entrySet = playing.entrySet();
			Iterator<Entry<Long, Map<String, Player>>> iterator = entrySet
					.iterator();
			while (iterator.hasNext()) {
				Entry<Long, Map<String, Player>> next = iterator.next();
				Map<String, Player> value = next.getValue();
				Set<Entry<String, Player>> entrySet2 = value.entrySet();
				Iterator<Entry<String, Player>> iterator2 = entrySet2
						.iterator();
				List<Date> allTime = new ArrayList<Date>();
				List<Boolean> allOver = new ArrayList<Boolean>();
				while (iterator2.hasNext()) {
					Entry<String, Player> next2 = iterator2.next();
					Date date = next2.getValue().getDate();
					if (date == null) {
						continue;
					}
					allTime.add(date);
					allOver.add(next2.getValue().isOver());
				}
				int count = 0;
				for (Date d : allTime) {
					long second = d.toInstant().getEpochSecond();
					long now = new Date().toInstant().getEpochSecond();
					if (now - second > TIME_OUT) {
						count++;
					}
				}
				int count1 = 0;
				for (Boolean b : allOver) {
					if (b) {
						count1++;
					}
				}

				if (count == 2 || count1 == 2) {
					Iterator<Entry<String, Player>> iterator3 = entrySet2
							.iterator();
					List<Player> playerList = new ArrayList<Player>();
					while (iterator3.hasNext()) {
						Entry<String, Player> next3 = iterator3.next();
						Player player = next3.getValue();
						playerList.add(player);
					}
					playerList.sort(new Comparator<Player>() {
						@Override
						public int compare(Player o1, Player o2) {
							return -(int) (o1.getPoint() - o2.getPoint());
						}
					});
					// 比赛结束，给奖励，发送数据给双方
					if (playerList.size() == 2) {
						Player winer = playerList.get(0);
						Player looser = playerList.get(1);

						int idx = i;
						// 给奖励
						int[] countOf = countOfThreeFightSuccss(winer
								.getAppId());
						double rate = 1d;
						if (countOf[idx] == 3) {
							rate = 1.5;
						}
						Account account = accountMapper.selectAccount(winer
								.getAppId());
						Account accountLooser = accountMapper
								.selectAccount(looser.getAppId());

						int[] rewardList = DebugOption.arenaReward[idx];
						ITEM_TYPE itemType = DebugOption
								.getItemType(rewardList[0]);
						int countOfItem = rewardList[1];
						countOfItem = (int) (countOfItem * rate);
						DateUtil dateUtil = new DateUtil(
								System.currentTimeMillis());
						GameLog gameLog = new GameLog(dateUtil,
								account.getAppId(), GAMELOG_TYPE.ARENA_GET);
						if (itemType == ITEM_TYPE.GOLD) {
							ArenaMgr.getInstance()
									.addGold(account, countOfItem);
							gameLog.setAddGold(countOfItem);
							gameLog.setNowGold(account.getGold());
						} else if (itemType == ITEM_TYPE.BALL) {
							ArenaMgr.getInstance()
									.addBall(account, countOfItem);
							gameLog.setAddBall(countOfItem);
							gameLog.setNowBall(account.getBall());
						}
						ArenaMgr.getInstance().accountMapper
								.updateAccountItem(account);
						ArenaMgr.getInstance().getLoggingService()
								.insertGameLog(gameLog);

						if (countOf[idx] == 3) {
							SessionMgr.getInstance().SendMsgToAll(
									CMD.THREE_WIN,
									new Object[] { account.getNickname() });
						}

						ArenaResult arenaResult = new ArenaResult();
						arenaResult.setId(next.getKey());
						arenaResult.setAppId(winer.getAppId());
						arenaResult.setAppId1(looser.getAppId());
						arenaResult.setDate(winer.getDate());
						arenaResult.setDate1(looser.getDate());
						arenaResult.setPoint((int) winer.getPoint());
						arenaResult.setPoint1((int) looser.getPoint());
						arenaResult.setIdx(idx);
						arenaResult.setWinAppId(winer.getAppId());

						int[] reward = new int[] { rewardList[0], countOfItem };
						winer.setItemType(reward[0]);
						winer.setItemValue(reward[1]);
						String rewardStr = null;
						try {
							rewardStr = DebugOption.toJson(reward);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						arenaResult.setReward(rewardStr);

						// 删除战斗
						playing.remove(next.getKey());

						baseTaskMapper.insertAreanResult(arenaResult);

						System.out.println("#########" + winer.getAppId()
								+ "   " + looser.getAppId());
						// 发送输赢
						SessionMgr.getInstance().sendMsg(
								winer.getAppId(),
								CMD.MATCH_RESULT,
								new Object[] { true, playerList,
										account.getGold(), account.getBall() });

						SessionMgr.getInstance().sendMsg(
								looser.getAppId(),
								CMD.MATCH_RESULT,
								new Object[] { false, playerList,
										accountLooser.getGold(),
										accountLooser.getBall() });
						System.out.println("发送双发");

					}

				}

			}
		}
	}

	public void loop() {
		//System.out.println("loop");
		synchronized (lock) {
		
			try {
				match();
				judge();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}

	}

	public void addGold(Account account, int gold) {
		int newGold = account.getGold() + gold;
		account.setGold(newGold);
	}

	public void addBall(Account account, int ball) {
		int newBall = account.getBall() + ball;
		account.setBall(newBall);
	}

	public boolean addAccountItem(DateUtil dateUtil, Account account,
			Shop item, int itemCnt, GameLog gameLog) {
		if (account.getBall() < 0 || account.getGold() < 0
				|| account.getPunch() < 0)
			throw new RuntimeException("Wrong now accountItem. NOW_GOLD="
					+ account.getGold() + ", NOW_BALL=" + account.getBall()
					+ ", NOW_PUNCH=" + account.getPunch());

		final int addCnt = item.getCnt() * itemCnt;
		final ITEM_TYPE itemType = DebugOption.getItemType(item.getItemId());

		switch (itemType) {
		case BALL:
			addBall(account, addCnt);
			if (account.getBall() < 0)
				throw new RuntimeException("Couldnot add ACCOUNT.BL=" + addCnt
						+ ", ACCOUNT.BL < 0");
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
				throw new RuntimeException("Couldnot add ACCOUNT.GD=" + addCnt
						+ ", ACCOUNT.BL < 0");
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
				throw new RuntimeException("Couldnot add ACCOUNT.PUNCH="
						+ addCnt + ", ACCOUNT.PUNCH < 0");
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
			throw new RuntimeException(
					"Cannot add item to account. this is not account item");
		}
	}

	public int[] countOfThreeFightSuccss(String appId) {
		synchronized (lock) {
			int[] list = new int[4];
			for (int i = 0; i < 4; i++) {
				List<ArenaResult> selectThreeArenaResult = baseTaskMapper
						.selectThreeArenaResult(appId, i);
				int count = 0;
				for (ArenaResult ar : selectThreeArenaResult) {
					// if (ar.getAppId().equals(appId)) {
					// if (ar.getPoint() >= ar.getPoint1()) {
					// count++;
					// }
					// } else if (ar.getAppId1().equals(appId)) {
					// if (ar.getPoint1() >= ar.getPoint()) {
					// count++;
					// }
					// }
					if (ar.getWinAppId().equals(appId)) {
						count++;
					}
				}
				list[i] = count;
			}

			return list;
		}
	}

	public ArrayList<Player> getWorldFightList(String appId) {
		synchronized (lock) {
			ArrayList<Player> _list = new ArrayList<Player>();
			for (int i = 0; i < pkArea.length; i++) {
				PkArea pkArean = pkArea[i];
				Map<Long, Map<String, Player>> playing = pkArean.getPlaying();
				Set<Entry<Long, Map<String, Player>>> entrySet = playing
						.entrySet();
				for (Entry<Long, Map<String, Player>> entry : entrySet) {
					Map<String, Player> value = entry.getValue();
					if (value.containsKey(appId)) {
						value.remove(appId);
						Player[] array = value.values().toArray(new Player[0]);
						if(array.length == 1){
							Player player = value.values().toArray(new Player[0])[0];
							_list.add(player);
						}
					
					}

				}
			}

			List<ArenaResult> arenaList = baseTaskMapper
					.selectWorldFightArenaList(appId);
			for(ArenaResult ar : arenaList){
				String state = ar.getAppIdState() +"######"+ar.getAppId1State();
				if(state.contains(appId)){
					continue;
				}
				Account selectAccountp1 = ArenaMgr.getInstance()
						.getAccountMapper().selectAccount(ar.getAppId());
				Account selectAccountp2 = ArenaMgr.getInstance()
						.getAccountMapper().selectAccount(ar.getAppId1());
				Player p1 = new Player(ar.getAppId(), selectAccountp1.getNickname(),null, null, ar.getPoint(), true, 0, 0, 0, 0, false, null);
				Player p2 = new Player(ar.getAppId1(), selectAccountp2.getNickname(),null, null, ar.getPoint1(), true, 0, 0, 0, 0, false, null);
				
				if(p1.getAppId().equals(ar.getWinAppId()))
				{
					p1.setWin(true);
					int[] ret = DebugOption.parseJson(int[].class, ar.getReward());
					if (ret != null && ret.length == 2) {
						p1.setItemType(ret[0]);
						p1.setItemValue(ret[1]);
					}
					
				}else if(p2.getAppId().equals(ar.getWinAppId())){
					p2.setWin(true);
					int[] ret = DebugOption.parseJson(int[].class, ar.getReward());
					if (ret != null && ret.length == 2) {
						p2.setItemType(ret[0]);
						p2.setItemValue(ret[1]);
					}
				}
				
				if(p1.getAppId().equals(appId)&&!p2.getAppId().equals(appId)){
					_list.add(p2);
				}
				else if(!p1.getAppId().equals(appId) && p2.getAppId().equals(appId)){
					_list.add(p1);
				}
				
				
				
				
				
			}
						
			for (ArenaResult ar : arenaList) {
				if (ar.getAppId().equals(appId) && ar.getAppIdState() == null) {
					baseTaskMapper.updateArenaList(appId, ar.getId());
				} else if (ar.getAppId1().equals(appId))
					baseTaskMapper.updateArenaList1(appId, ar.getId());
			}

			return _list;
		}
	}

	public int costGoldAndBall(int idx, String appId, boolean isSave) {
		if (appId == null) {
			return -1;
		}
		Account account = ArenaMgr.getInstance().getAccountMapper()
				.selectAccount(appId);
		if (account == null) {
			return -2;
		}
		int[][] reward = DebugOption.arenaReward;
		int[] rd = reward[idx];
		ITEM_TYPE itemType = DebugOption.getItemType(rd[0]);
		int cost = rd[2];
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		GameLog gameLog = new GameLog(dateUtil, appId, GAMELOG_TYPE.ARENA_COST);
		if (itemType == ITEM_TYPE.GOLD) {
			if (account.getGold() < cost) {
				// returnCode = 1;
				// returnVal.put("RC", returnCode);
				return -3;
			}
			ArenaMgr.getInstance().addGold(account, -cost);
			gameLog.setNowGold(account.getGold());
			gameLog.setAddGold(-cost);
		} else if (itemType == ITEM_TYPE.BALL) {
			if (account.getBall() < cost) {
				// returnCode = 2;
				// returnVal.put("RC", returnCode);
				// return returnVal;
				return -4;
			}
			ArenaMgr.getInstance().addBall(account, -cost);
			gameLog.setNowBall(account.getBall());
			gameLog.setAddBall(-cost);
		}
		if (isSave) {
			ArenaMgr.getInstance().getLoggingService().insertGameLog(gameLog);
			ArenaMgr.getInstance().getAccountMapper()
					.updateAccountItem(account);
		}

		// returnVal.put("RC", returnCode);
		return 0;
	}

}
