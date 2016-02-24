package com.gy.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gy.model.RmMatch;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.CAMP_STATUS;
import com.nfl.kfb.util.DebugOption.GAME_STATUS;

/*
 * 在轮中可以开比赛
 * */

public class CopyOfRunningMgr {

	public static final int SCREEN_SIZE = 1024;

	private static final Logger logger = LoggerFactory
			.getLogger(AppController.class);

	private static CopyOfRunningMgr single = null;

	public HashMap<Integer, RmMatch> allRmMatch = new HashMap<Integer, RmMatch>();

	public GAME_STATUS game_status = GAME_STATUS.READY;

	private Object _lock = new Object();

	private HashMap<Integer, RmMatch> topThree = new HashMap<Integer, RmMatch>();
	// 回答问题奖励的时间
	private HashMap<Integer, Integer> answerQuestionRewardTime = new HashMap<Integer, Integer>();

	// 到达答题区的前五个
	public HashMap<Integer, RmMatch> topFive = new HashMap<Integer, RmMatch>();

	private List<Integer> notTopFive = new ArrayList<Integer>();

	public Date clock = null;

	public long fist_run_time = 0;

	public synchronized static CopyOfRunningMgr getInstance() {
		if (single == null) {
			single = new CopyOfRunningMgr();
		}
		return single;
	}

	private CopyOfRunningMgr() {
		logger.info("#runnging man start up!");
		for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
			RmMatch rmMatch = new RmMatch();
			rmMatch.setRm_camp_id(i);
			allRmMatch.put(i, rmMatch);
		}
	}

	// 提交分数
	public int submit_score(int campId, int score,int game_type) {
		synchronized (_lock) {
			campId = campId - 1;
			if (game_status != GAME_STATUS.RUNNING) {
				return -1;
			}
			RmMatch _m = allRmMatch.get(campId);
			_m.submit_count += 1;
			int sumScore = _m.getScore() + score;
			int sumCount = _m.submit_count;
			
			score = (int)((sumScore + AppController.base_score[game_type]) * 1.0 / sumCount); 
			
			CAMP_STATUS _s = CAMP_STATUS.valueOf(_m.getStatus());
			switch (_s) {
			case READY:// 准备
				break;
			case STOP:// 停止答题
				break;
			case OVER:// 比赛结束
				break;
			case FIRST_RUNNING:// 第一阶段奔跑
				_m.setScore(_m.getScore() + score);
				if (_m.getScore() > 1024) {
					boolean isContain = topThree
							.containsKey(_m.getRm_camp_id());
					if (!isContain && topThree.size() < 3) {
						_m.reward += -(5 - topThree.size());
						topThree.put(_m.getRm_camp_id(), _m);
					}
				}
				long cost = costTime();

				if (_m.getScore() >= 2048) {
					boolean isContain = topFive.containsKey(_m.getRm_camp_id());
					if (!isContain && topFive.size() < 5) {
						topFive.put(_m.getRm_camp_id(), _m);
						if (topFive.size() == 5) {
							RmMatch[] m = allRmMatch.values().toArray(
									new RmMatch[0]);
							for (int i = 0; i < m.length; i++) {
								boolean isHave = topFive.containsKey(m[i]
										.getRm_camp_id());
								if (!isHave) {
									m[i].setStatus(CAMP_STATUS.ELIMINATE
											.getValue());
									m[i].setCost_time(cost);
								}
							}
							game_status = GAME_STATUS.ANSWER;
						}

					}
				}

				if (_m.getScore() >= 2048) {
					_m.setScore(2048);
					_m.setStatus(CAMP_STATUS.STOP.getValue());
					_m.setCost_time(cost);
				}

				break;
			case SECOND_RUNNING:// 第二阶段奔跑
				_m.setScore(_m.getScore() + score);

				if (_m.getScore() >= 4096) {
					_m.setScore(4096);
					_m.setStatus(CAMP_STATUS.OVER.getValue());
					//_m.setCost_time(_m.getCost_time() + costTime());
					_m.cost_time1 = costTime();
				}

				RmMatch[] mList = topFive.values().toArray(new RmMatch[0]);
				int cnt = 0;
				for (int i = 0; i < mList.length; i++) {
					if (mList[i].getStatus() == CAMP_STATUS.OVER.getValue()) {
						cnt++;
					}
				}
				if (cnt == 5) {
					game_status = GAME_STATUS.OVER;
				}

				break;
			case ELIMINATE:// 淘汰
				break;
			default:
				break;
			}
			return _m.getStatus();
		}
		
	}

	// 开始第一阶段跑
	public void start_first_run() {
		logger.debug("start_first_run");
		synchronized (_lock) {
			clock = new Date();
			if (game_status != GAME_STATUS.READY) {
				return;
			}
			game_status = GAME_STATUS.READY.RUNNING;
			RmMatch[] list = allRmMatch.values().toArray(new RmMatch[0]);
			for (RmMatch _m : list) {
				CAMP_STATUS _s = CAMP_STATUS.valueOf(_m.getStatus());
				switch (_s) {
				case READY:// 准备
					_m.setStatus(CAMP_STATUS.FIRST_RUNNING.getValue());
					break;
				case STOP:// 停止答题
					_m.setStatus(CAMP_STATUS.SECOND_RUNNING.getValue());
					break;
				case OVER:// 比赛结束
					break;
				case FIRST_RUNNING:// 第一阶段奔跑
					break;
				case SECOND_RUNNING:// 第二阶段奔跑
					break;
				case ELIMINATE:// 淘汰
					break;
				default:
					break;
				}
			}
		}
	}

	// 开始第二阶段跑
	public void start_second_run() {
		logger.info("start_second_run");
		synchronized (_lock) {
			clock = new Date();
			if (game_status != GAME_STATUS.ANSWER) {
				return;
			}

			RmMatch[] list = allRmMatch.values().toArray(new RmMatch[0]);
			for (RmMatch _m : list) {
				CAMP_STATUS _s = CAMP_STATUS.valueOf(_m.getStatus());
				switch (_s) {
				case READY:// 准备
					break;
				case STOP:// 停止答题
					_m.setStatus(CAMP_STATUS.SECOND_RUNNING.getValue());
					break;
				case OVER:// 比赛结束
					break;
				case FIRST_RUNNING:// 第一阶段奔跑
					break;
				case SECOND_RUNNING:// 第二阶段奔跑
					break;
				case ELIMINATE:// 淘汰
					break;

				}
			}

			game_status = GAME_STATUS.RUNNING;

		}

	}

	// 开始新游戏
	public void newGame() {
		logger.info("newGame");
		synchronized (_lock) {
			single = new CopyOfRunningMgr();
		}
	}

	// 答题奖励功能
	public boolean giveAnswerQuestionRewardTime(int campId, int time) {
		synchronized (_lock) {
			if (!topFive.containsKey(campId)) {
				return false;
			}
			if (game_status != GAME_STATUS.ANSWER) {
				return false;
			}

			RmMatch m = topFive.get(campId);
			if (time < 0) {
				m.reward += time;
			} else {
				m.penalty += time;
			}
			return true;
		}
	}

	public boolean total() {
		logger.info("total");
		synchronized (_lock) {
			if(game_status != GAME_STATUS.OVER){
				logger.info("game is not over!");
				return false;
			}
			
			RmMatch[] list = allRmMatch.values().toArray(new RmMatch[0]);
			for (int i = 0; i < list.length; i++) {
				list[i].total = ((list[i].reward + list[i].penalty) * 1000  + (list[i].getCost_time() + list[i].cost_time1)) / 1000.0;
			}
			
			return true;
		}
	}

	public long costTime() {
		synchronized (_lock) {
			Date now = new Date();
			return now.getTime() - clock.getTime();
		}
	}

}
