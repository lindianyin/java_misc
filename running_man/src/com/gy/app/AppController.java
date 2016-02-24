/**
 * 
 */
package com.gy.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.codehaus.jackson.map.util.Comparators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gy.model.JsonResponse;
import com.gy.model.RmMatch;
import com.nfl.kfb.util.Command;
import com.nfl.kfb.util.DebugOption.CAMP_STATUS;
import com.nfl.kfb.util.DebugOption.GAME_STATUS;

@Controller
@RequestMapping(value = "/app", method = { RequestMethod.POST,
		RequestMethod.GET })
public class AppController {
	private static final Logger logger = LoggerFactory
			.getLogger(AppController.class);
	
	public static int[] base_score = {150,130,120};
	
	// 总体结算
	@RequestMapping(value = Command.TOTAL)
	@ResponseBody
	public JsonResponse total() {
		try {
			logger.info("total");
			boolean isSuccess = CopyOfRunningMgr.getInstance().total();
			JsonResponse jr = new JsonResponse();
			jr.put("msg", isSuccess ? "操作成功" : "操作失败");
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// 给回答问题的人奖励
	@RequestMapping(value = Command.ANSWER_QUEST_REWARD)
	@ResponseBody
	public JsonResponse answerQuestionReward(
			@RequestParam("campid") int campid, @RequestParam("time") int time) {
		try {
			campid = campid - 1;
			logger.info("answerQuestionReward:campid:{},time:{}", campid, time);

			JsonResponse jr = new JsonResponse();
			boolean isSuccess = CopyOfRunningMgr.getInstance()
					.giveAnswerQuestionRewardTime(campid, time);
			jr.put("msg", isSuccess ? "操作成功" : "操作失败");
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// 新的一局
	@RequestMapping(value = Command.NEW_GAME)
	@ResponseBody
	public JsonResponse new_game() {
		try {
			JsonResponse jr = new JsonResponse();
			CopyOfRunningMgr.getInstance().newGame();
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// 开始游戏
	@RequestMapping(value = Command.START_GAME)
	@ResponseBody
	public JsonResponse start_game() {
		try {
			JsonResponse jr = new JsonResponse();
			CopyOfRunningMgr.getInstance().start_first_run();
			jr.put("msg", CopyOfRunningMgr.getInstance().game_status);
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	// 开始冲刺
	@RequestMapping(value = Command.START_SPRINT)
	@ResponseBody
	public JsonResponse start_sprint() {
		try {
			JsonResponse jr = new JsonResponse();
			CopyOfRunningMgr.getInstance().start_second_run();
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.SUBMIT_TIMES)
	@ResponseBody
	public JsonResponse submit_times(
			@RequestParam("rm_camp_id") int rm_camp_id,
			@RequestParam("score") int score,
			@RequestParam("game_type") int game_type
			) {
		try {
			JsonResponse jr = new JsonResponse();
			int status = CopyOfRunningMgr.getInstance().submit_score(
					rm_camp_id, score,game_type);
			jr.put("status", status);
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.GET_RM_MATCH)
	@ResponseBody
	public JsonResponse get_rm_match() {
		try {
			JsonResponse jr = new JsonResponse();
			RmMatch[] get_list = CopyOfRunningMgr.getInstance().allRmMatch
					.values().toArray(new RmMatch[0]);
			// 修正末尾的值
			// int sum = 0;
			// for (int i = 0; i < get_list.size(); i++) {
			// sum += get_list.get(i).getScore();
			// }
			// int average = sum / get_list.size();
			// int delta = average - get_list.get(0).getScore();
			// if (delta > 0) {
			// submit_times(get_list.get(0).getRm_camp_id(), delta);
			// }
			// Arrays.sort(get_list, new Comparator<RmMatch>() {
			// @Override
			// public int compare(RmMatch o1, RmMatch o2) {
			// return (int) (o1.getCost_time() - o2.getCost_time());
			// }
			// });

			// ArrayList<RmMatch> list1 = new ArrayList<RmMatch>();
			// ArrayList<RmMatch> list2 = new ArrayList<RmMatch>();
			// for (int i = 0; i < get_list.length; i++) {
			// RmMatch m = get_list[i];
			// if (m.getStatus() != CAMP_STATUS.ELIMINATE.getValue()) {
			// list1.add(m);
			// } else {
			// list2.add(m);
			// }
			// }
			//
			// Collections.sort(list1, new Comparator<RmMatch>() {
			// @Override
			// public int compare(RmMatch o1, RmMatch o2) {
			// return (int) (o1.getCost_time() - o2.getCost_time());
			// }
			// });
			//
			// boolean isSecond = false;
			// if(CopyOfRunningMgr.getInstance().topFive.size() > 0){
			// is
			// }
			//
			// if (isSecond) {
			// Collections.sort(list1, new Comparator<RmMatch>() {
			// @Override
			// public int compare(RmMatch o1, RmMatch o2) {
			// return (int) (o1.getCost_time() - o2.getCost_time());
			// }
			// });
			// }
			//
			// Collections.sort(list2, new Comparator<RmMatch>() {
			// @Override
			// public int compare(RmMatch o1, RmMatch o2) {
			// return (int) (o1.getCost_time() - o2.getCost_time());
			// }
			// });
			//
			// boolean isTotaled = false;
			// for (int i = 0; i < get_list.length; i++) {
			// if (get_list[i].total > 0) {
			// isTotaled = true;
			// break;
			// }
			// }
			//
			// if (isTotaled) {
			//
			// Collections.sort(list1, new Comparator<RmMatch>() {
			// @Override
			// public int compare(RmMatch o1, RmMatch o2) {
			// return (int) (o1.total - o2.total);
			// }
			// });
			// Collections.sort(list2, new Comparator<RmMatch>() {
			// @Override
			// public int compare(RmMatch o1, RmMatch o2) {
			// return (int) (o1.total - o2.total);
			// }
			// });
			// }
			//
			// list1.addAll(list2);
			// get_list = list1.toArray(new RmMatch[0]);

			List<RmMatch> get_list_bak = new ArrayList<RmMatch>();
			for (RmMatch r : get_list) {
				RmMatch _rm = new RmMatch();
				_rm.setCount(r.getCount());
				_rm.setRm_base_match_id(r.getRm_base_match_id());
				_rm.setRm_base_round_id(r.getRm_base_round_id());
				_rm.setRm_camp_id(r.getRm_camp_id() + 1);
				_rm.setScore(r.getScore());
				_rm.setStatus(r.getStatus());
				_rm.reward = r.reward;
				_rm.penalty = r.penalty;
				_rm.total = r.total;

				if (CopyOfRunningMgr.getInstance().game_status == GAME_STATUS.RUNNING
						|| CopyOfRunningMgr.getInstance().game_status == GAME_STATUS.ANSWER
						|| CopyOfRunningMgr.getInstance().game_status == GAME_STATUS.OVER

				) {
					long cost = CopyOfRunningMgr.getInstance().costTime();
					if (r.getCost_time() == 0
							&& r.getStatus() == CAMP_STATUS.FIRST_RUNNING
									.getValue()) {
						_rm.setCost_time(cost);
					} else {
						_rm.setCost_time(r.getCost_time());
					}

					if (r.cost_time1 == 0
							&& r.getStatus() == CAMP_STATUS.SECOND_RUNNING
									.getValue()) {
						_rm.cost_time1 = cost;
					} else {
						_rm.cost_time1 = r.cost_time1;
					}

				}

				// if (CopyOfRunningMgr.getInstance().game_status ==
				// GAME_STATUS.RUNNING) {
				// long cost = CopyOfRunningMgr.getInstance().costTime();
				// if (!CopyOfRunningMgr.getInstance().topFive.containsKey(r
				// .getRm_camp_id())
				// && r.getStatus() == CAMP_STATUS.ELIMINATE
				// .getValue()) {
				// cost = 0;
				// }
				// _rm.setCost_time(r.getCost_time() + cost);
				// } else {
				// _rm.setCost_time(r.getCost_time());
				// }

				get_list_bak.add(_rm);
			}

			ArrayList<RmMatch> list1 = new ArrayList<RmMatch>();
			ArrayList<RmMatch> list2 = new ArrayList<RmMatch>();
			for (int i = 0; i < get_list_bak.size(); i++) {
				RmMatch m = get_list_bak.get(i);
				if (m.getStatus() != CAMP_STATUS.ELIMINATE.getValue()) {
					list1.add(m);
				} else {
					list2.add(m);
				}
			}

			boolean isSecond = false;

			for (int i = 0; i < get_list_bak.size(); i++) {
				if (get_list[i].getStatus() == CAMP_STATUS.SECOND_RUNNING
						.getValue()
						|| get_list[i].getStatus() == CAMP_STATUS.OVER
								.getValue()) {
					isSecond = true;
					break;
				}
			}

			if (isSecond) {// second
				Collections.sort(list1, new Comparator<RmMatch>() {
					@Override
					public int compare(RmMatch o1, RmMatch o2) {
						if ((int) (o1.cost_time1 - o2.cost_time1) == 0) {
							return o1.getStatus() - o2.getStatus();
						}
						return (int) (o1.cost_time1 - o2.cost_time1);
					}
				});

			} else {// first
				Collections.sort(list1, new Comparator<RmMatch>() {
					@Override
					public int compare(RmMatch o1, RmMatch o2) {
						if ((int) (o1.getCost_time() - o2.getCost_time()) == 0) {
							return o1.getStatus() - o2.getStatus();
						}

						return (int) (o1.getCost_time() - o2.getCost_time());
					}
				});

			}

			boolean isTotaled = false;
			for (int i = 0; i < list1.size(); i++) {
				if (list1.get(i).total > 0) {
					isTotaled = true;
					break;
				}
			}
			
			if(isTotaled){
				Collections.sort(list1, new Comparator<RmMatch>() {
					@Override
					public int compare(RmMatch o1, RmMatch o2) {
						return (int) (o1.total - o2.total);
					}
				});
			}
			
			
			Collections.sort(list2, new Comparator<RmMatch>() {
				@Override
				public int compare(RmMatch o1, RmMatch o2) {
					if ((int) (o1.getCost_time() - o2.getCost_time()) == 0) {
						return o1.getStatus() - o2.getStatus();
					}
					return (int) (o1.getCost_time() - o2.getCost_time());
				}
			});
			list1.addAll(list2);
			get_list_bak = list1;
			for (int i = 0; i < get_list_bak.size(); i++) {
				get_list_bak.get(i).setRank(i + 1);
			}

			jr.put("list", get_list_bak);
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.INCREMENT_CAMP_COUNT)
	@ResponseBody
	public JsonResponse increment_camp_count(
			@RequestParam("rm_camp_id") int rm_camp_id) {
		try {
			JsonResponse jr = new JsonResponse();
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.CHANGE_BASE_SCORE)
	@ResponseBody
	public JsonResponse change_base_score(
			@RequestParam("game_type") int game_type,
			@RequestParam("value") int value
			) {
		try {
			logger.info("{},{},{}",AppController.base_score[0],AppController.base_score[1],AppController.base_score[2]);
			JsonResponse jr = new JsonResponse();
			AppController.base_score[game_type] += value;
			String strVal = String.format("摇:%d,划:%d,点:%d",AppController.base_score[0],AppController.base_score[1],AppController.base_score[2]);
			jr.put("msg",strVal);
			//jr.put("base_score",AppController.base_score);
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	
	
	
}
