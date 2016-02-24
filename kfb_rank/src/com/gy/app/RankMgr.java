package com.gy.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.gy.model.User_rank_vo;

public class RankMgr {
	private List<User_rank_vo> user_rank_set = new LinkedList<User_rank_vo>();

	private List<String> first_name_list = new ArrayList<String>();
	private List<String> second_name_list = new ArrayList<String>();

	Random random = new Random();
	
	private static RankMgr singleTon = null;
	private static final int RANK_SIZE = 10;

	private RankMgr() {
		init();
	}

	public static synchronized RankMgr getInstance() {
		if (singleTon == null) {
			singleTon = new RankMgr();
		}
		return singleTon;
	}

	private void init() {
		List<User_rank_vo> user_rank_vo_list = MapperMgr.getInstance()
				.getMapper().select_user_rank_vo_list(RANK_SIZE);
		user_rank_set.clear();
		for (User_rank_vo vo : user_rank_vo_list) {
			user_rank_set.add(vo);
		}

		first_name_list.addAll(MapperMgr.getInstance().getMapper()
				.select_base_first_name_female());
		first_name_list.addAll(MapperMgr.getInstance().getMapper()
				.select_base_first_name_male());
		
		second_name_list.addAll(MapperMgr.getInstance().getMapper()
				.select_base_second_name_female());
		second_name_list.addAll(MapperMgr.getInstance().getMapper()
				.select_base_second_name_male());

	}

	public synchronized List<User_rank_vo> getRankList() {
		ArrayList<User_rank_vo> _list = new ArrayList<User_rank_vo>();
		Iterator<User_rank_vo> iterator = user_rank_set.iterator();
		while (iterator.hasNext()) {
			User_rank_vo next = iterator.next();
			_list.add(next);
		}
		return _list;
	}

	public synchronized List<User_rank_vo> getRankList(int user_account_id) {
		List<User_rank_vo> rankList = getRankList();
		User_rank_vo self = MapperMgr.getInstance().getMapper()
				.select_user_rank_vo(user_account_id);
		if (!rankList.contains(self)) {
			rankList.add(self);
		}
		return rankList;
	}

	// 更新排行（包括内存和数据库）
	public synchronized List<User_rank_vo> updateRankScore(int user_account_id,
			int score) {
		MapperMgr.getInstance().getMapper()
				.update_user_rank_score(user_account_id, score);
		User_rank_vo user_rank_vo = MapperMgr.getInstance().getMapper()
				.select_user_rank_vo(user_account_id);
		boolean isContain = false;
		for (int i = 0; i < user_rank_set.size(); i++) {
			User_rank_vo vo = user_rank_set.get(i);
			if (vo.getUser_account_id() == user_rank_vo.getUser_account_id()) {
				isContain = true;
				if (score > vo.getScore()) {
					vo.setScore(score);
					break;
				}
			}
		}
		if (!isContain) {
			user_rank_set.add(user_rank_vo);
		}
		Collections.sort(user_rank_set);
		if (user_rank_set.size() > RANK_SIZE) {
			user_rank_set = user_rank_set.subList(0, RANK_SIZE);
		}
		return user_rank_set;
	}
	
	
	public String getRandomName(){
		int index1 = random.nextInt(first_name_list.size());
		int index2 = random.nextInt(second_name_list.size());
		return first_name_list.get(index1) +  second_name_list.get(index2);
	}
	
	

}
