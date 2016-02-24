//package com.gy.app;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.gy.model.RmMatch;
//import com.nfl.kfb.util.DebugOption;
//import com.nfl.kfb.util.DebugOption.CAMP_STATUS;
//
///*
// * 在轮中可以开比赛
// * */
//
//public class RunningMgr {
//
//	public static final int SCREEN_SIZE = 1024;
//
//	private static final Logger logger = LoggerFactory
//			.getLogger(AppController.class);
//
//	private static RunningMgr single = null;
//
//	private ArrayList<ArrayList<RmMatch>> running_road = new ArrayList<ArrayList<RmMatch>>();
//
//	private boolean isRunning = false;
//
//	private int current_match = 1;
//
//	private Object _lock = new Object();
//
//	// 第一关头三名奖励
//	private HashMap<Integer, Integer> topThreeRewardTime = new HashMap<Integer, Integer>();
//	// 回答问题奖励的时间
//	private HashMap<Integer, Integer> answerQuestionRewardTime = new HashMap<Integer, Integer>();
//
//	// 到达答题区的前五个
//	private List<Integer> topFive = new ArrayList<Integer>();
//
//	private List<Integer> notTopFive = new ArrayList<Integer>();
//
//	
//	
//	public Date clock = null;
//	
//	public long fist_run_time = 0;
//	
//	public List<Integer> getNotTopFive() {
//		return notTopFive;
//	}
//
//	public void setNotTopFive(List<Integer> notTopFive) {
//		this.notTopFive = notTopFive;
//	}
//
//	public HashMap<Integer, Integer> getTopThreeRewardTime() {
//		synchronized (_lock) {
//			return topThreeRewardTime;
//		}
//	}
//
//	public void setTopThreeRewardTime(
//			HashMap<Integer, Integer> topThreeRewardTime) {
//		this.topThreeRewardTime = topThreeRewardTime;
//	}
//
//	public HashMap<Integer, Integer> getAnswerQuestionRewardTime() {
//		synchronized (_lock) {
//			return answerQuestionRewardTime;
//		}
//	}
//
//	public void setAnswerQuestionRewardTime(
//			HashMap<Integer, Integer> answerQuestionRewardTime) {
//		this.answerQuestionRewardTime = answerQuestionRewardTime;
//	}
//
//	public List<Integer> getNotEliminateList() {
//		synchronized (_lock) {
//			return topFive;
//		}
//	}
//
//	public void setNotEliminateList(List<Integer> notEliminateList) {
//		this.topFive = notEliminateList;
//	}
//
//	public boolean isRunning() {
//		return isRunning;
//	}
//
//	public void setRunning(boolean isRunning) {
//		this.isRunning = isRunning;
//	}
//
//	public int getCurrent_match() {
//		return current_match;
//	}
//
//	public void setCurrent_match(int current_match) {
//		this.current_match = current_match;
//	}
//
//	public synchronized static RunningMgr getInstance() {
//		if (single == null) {
//			single = new RunningMgr();
//		}
//		return single;
//	}
//
//	public ArrayList<ArrayList<RmMatch>> getRunning_road() {
//		return running_road;
//	}
//
//	public void setRunning_road(ArrayList<ArrayList<RmMatch>> running_road) {
//		this.running_road = running_road;
//	}
//
//	private RunningMgr() {
//		logger.info("runnging man start up!");
//		for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//			ArrayList<RmMatch> list = new ArrayList<RmMatch>();
//			for (int j = 0; j < 2; j++) {
//				RmMatch rm = new RmMatch();
//				rm.setRm_camp_id(i + 1);
//				if (j == 1) {
//					rm.setScore(1024 * 2);
//				}
//
//				list.add(rm);
//			}
//			running_road.add(list);
//		}
//	}
//
//	// 提交分数
//	public void submit_score(int camp_id, int score) {
//		// if(camp_id > 6){
//		// return;
//		// }
//
//		// logger.info("submit_score:camp_id:{},score:{}", camp_id, score);
//
//		synchronized (_lock) {
//			if (notTopFive.contains(camp_id)) {
//				return;
//			}
//
//			ArrayList<RmMatch> arrayList = running_road.get(camp_id - 1);
//			RmMatch currentMatch = arrayList.get(current_match - 1);
//			if (currentMatch.getStatus() == CAMP_STATUS.STOP.getValue()) {
//				return;
//			}
//
//			if (current_match == 2 && !topFive.contains(camp_id)) {
//				return;
//			}
//			
//			if(currentMatch.getStatus() == CAMP_STATUS.ELIMINATE.getValue()){
//				logger.info("campId:{} is ELIMINATE and score is not submit",currentMatch.getRm_camp_id());
//				return ;
//			}
//			
//			
//			
//			
//			// giveTopThreeRewardTime(new int[] { camp_id });
//
//			//currentMatch.setStatus(CAMP_STATUS.RUNNING.getValue());
//			currentMatch.setScore(currentMatch.getScore() + score);
//
//			int _score = currentMatch.getScore();
//			if (_score >= 1024) {
//				if (topThreeRewardTime.size() < 3
//						&& !topThreeRewardTime.containsKey(camp_id)) {
//					topThreeRewardTime.put(camp_id,
//							topThreeRewardTime.size() - 4);
//					logger.info("campid:{} score more than 1024", camp_id);
//				}
//			}
//
////			if (currentMatch.getStart_time() == null) {
////				currentMatch.setStart_time(new Date());
////			}
//
//			Date dateNow = new Date();
//			
//			if (currentMatch.getScore() > SCREEN_SIZE * 2 * current_match) {
//				currentMatch.setStatus(CAMP_STATUS.STOP.getValue());
//				currentMatch.setScore(SCREEN_SIZE * 2 * current_match);
//				
//				//currentMatch.setCost_time();
//				
//				//currentMatch.setEnd_time(new Date());
////				currentMatch.setCost_time(currentMatch.getEnd_time().getTime()
////						- currentMatch.getStart_time().getTime());
//				// System.out.println(currentMatch.getRm_camp_id() + "到达答题区");
//
//				if (topFive.size() < 5 && !topFive.contains(camp_id)) {
//					logger.info("campid:{} arrive at answer area!", camp_id);
//					topFive.add(camp_id);
//					currentMatch.setCost_time(dateNow.getTime() - clock.getTime());
//					if (topFive.size() == 5) {
//						List<Integer> list = new ArrayList<Integer>();
//						for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//							list.add(i + 1);
//						}
//						
//						
//						list.removeAll(topFive);
//
//						for (int i = 0; i < list.size(); i++) {
//							int campid = list.get(i);
//							
//							RmMatch match = running_road.get(campid-1).get(current_match-1);
//							match.setStatus(CAMP_STATUS.ELIMINATE
//											.getValue());
//							match.setCost_time(dateNow.getTime() - clock.getTime());
//							fist_run_time = dateNow.getTime() - clock.getTime();
//						}
//						notTopFive = list;
//						logger.info("list.size()={}", list.size());
//						isRunning = false;
//						
//					}
//
//				}
//				
//				if(current_match == 2){
//					int count = 0;
//					for(int i=0;i<DebugOption.ROAD_SIZE;i++){
//						RmMatch m = running_road.get(i).get(1);
//						if(m.getStatus() == CAMP_STATUS.STOP.getValue()){
//							count++;
//						}
//					}
//					if(count == 5){
//						isRunning = false;
//					}
//				}
//				
//				
//				
//
//			}
////			int count = 0;
////			int count1 = 0;
////			for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
////				RmMatch rmMatch = running_road.get(i).get(current_match - 1);
////				if (rmMatch.getStatus() == CAMP_STATUS.STOP.getValue()) {
////					count++;
////				}
////				if (rmMatch.getStatus() == CAMP_STATUS.ELIMINATE.getValue()) {
////					count1++;
////				}
////			}
////			logger.info("count:{},count1:{}", count, count1);
////			if (count == DebugOption.ROAD_SIZE
////					|| (count + count1) == DebugOption.ROAD_SIZE) {
////				isRunning = false;
////				logger.info("stop the game isRunning:{}", isRunning);
////			} else {
////				isRunning = true;
////			}
//		}
//	}
//
//	public List<RmMatch> get_list() {
//		// logger.info("get_list");
//		synchronized (_lock) {
//			List<RmMatch> rmMatchList = new ArrayList<>();
//			for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//				rmMatchList.add(running_road.get(i).get(current_match - 1));
//			}
//			return rmMatchList;
//		}
//	}
//
//	// 开始奔跑
//	public void start_run( boolean isSprit) {
//		logger.debug("start_run");
//		synchronized (_lock) {
//			if (isRunning) {
//				logger.info("isRunning={}", isRunning);
//				return;
//			}
//			int stopCount = 0;
//			int taotaiCount = 0;
//			for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//				RmMatch rmMatch = running_road.get(i).get(current_match - 1);
//				if (rmMatch.getStatus() == CAMP_STATUS.STOP.getValue()) {
//					stopCount++;
//				}
//				if (rmMatch.getStatus() == CAMP_STATUS.ELIMINATE.getValue()) {
//					taotaiCount++;
//				}
//				//rmMatch.setStart_time(new Date());
//				
////				if(rmMatch.getStatus() == CAMP_STATUS.ELIMINATE.getValue()){
////					rmMatch.setEnd_time(new Date());
////				}
////				if(notTopFive.contains(rmMatch.getRm_camp_id())){
////					logger.info("set the second road's status to ELIMINATE");
////					rmMatch.setEnd_time(new Date());
////					rmMatch.setStatus(CAMP_STATUS.ELIMINATE.getValue());
////				}
//				
//				
//				
//				
//			}
//			if (stopCount == 5 && taotaiCount == 8 || stopCount == 0 &&  taotaiCount == 0) {
//				for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//					RmMatch rmMatch = running_road.get(i)
//							.get(current_match - 1);
//					
//					if(!isSprit){
//						rmMatch.setStatus(CAMP_STATUS.RUNNING.getValue());
//					}
//					
//					
//				}
//				logger.info("start the server and the isRunning is {}",
//						isRunning);
//				isRunning = true;
//			}
//			
//			clock = new Date();
//			
//			
//			
//			
//			
//		}
//	}
//
//	// 开始冲刺
//	public void start_sprint() {
//		logger.info("start_sprint");
//		synchronized (_lock) {
//			if (current_match == 1) {
//				current_match = 2;
//			}
//
//			for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//				RmMatch rmMatch = running_road.get(i).get(current_match - 1);
//				long cost_time = running_road.get(i).get(0).getCost_time();
//				int status = running_road.get(i).get(0).getStatus();
//				rmMatch.setCost_time(rmMatch.getCost_time() + cost_time);// add
//				//rmMatch.setStatus(status);															// the
//																			// fisrt
//																			// match
//																			// cost
//																			// time
//
//				if (notTopFive.contains(rmMatch.getRm_camp_id())) {
//					rmMatch.setScore(running_road.get(i).get(0).getScore());
//				}
//
//			}
//			start_run(true);
//		}
//	}
//
//	// 开始新游戏
//	public void newGame() {
//		logger.info("newGame");
//		synchronized (_lock) {
//			single = new RunningMgr();
//		}
//	}
//
//	// 1.过1024<第一关>前三名奖励 奖励内容待定... 3 2 1
//	public HashMap<Integer, Integer> giveTopThreeRewardTime(int[] campIds) {
//		logger.info("giveTopThreeRewardTime");
//		synchronized (_lock) {
//			if (topThreeRewardTime.size() > 2) {
//				return null;
//			}
//			int idx = topThreeRewardTime.size();
//			int time[] = new int[] { -1, -2, -3 };
//			for (int i = 0; i < campIds.length; i++) {
//				topThreeRewardTime.put(campIds[i], time[idx]);
//			}
//			return topThreeRewardTime;
//		}
//	}
//
//	// 答题奖励功能
//	public boolean giveAnswerQuestionRewardTime(int campId, int time) {
//		synchronized (_lock) {
//			if (notTopFive.contains(campId)) {
//				return false;
//			}
//			if (!isRunning) {
//				return false;
//			}
//			if (answerQuestionRewardTime.containsKey(campId)) {
//				int val = answerQuestionRewardTime.get(campId);
//				val += time;
//				answerQuestionRewardTime.remove(campId);
//				answerQuestionRewardTime.put(campId, val);
//			} else {
//				answerQuestionRewardTime.put(campId, time);
//			}
//			return true;
//		}
//	}
//
//	public boolean total() {
//		synchronized (_lock) {
//			if(!isRunning){
//				return false;
//			}
//			for (int i = 0; i < DebugOption.ROAD_SIZE; i++) {
//				RmMatch rmMatch = running_road.get(i).get(current_match - 1);
//				if(notTopFive.contains(rmMatch.getRm_camp_id())){
//					continue;
//				}
//				long val = rmMatch.getCost_time();
//				if(topThreeRewardTime.containsKey(rmMatch.getRm_camp_id())){
//					val += topThreeRewardTime.get(rmMatch.getRm_camp_id());
//				
//				}
//				
//				if(answerQuestionRewardTime.containsKey(rmMatch.getRm_camp_id())){
//					val += answerQuestionRewardTime.get(rmMatch.getRm_camp_id());
//				}
//				
//				rmMatch.total = val;
//				
//			}
//			
//			return true;
//		}
//	}
//
//}
