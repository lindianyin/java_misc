package com.gy.app;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.gy.mapper.gyp_app.Mapper;
import com.nfl.kfb.util.DebugOption.CAMP_STATUS;

public class StartUp {
	
	@Autowired
	private Mapper mapper;
	
	@PostConstruct
	public void startUp(){
		System.out.println("#############startup");
		MapperMgr.getInstance().setMapper(mapper);
		
//		RunningMgr.getInstance().newRound();
//		RunningMgr.getInstance().status = CAMP_STATUS.READY;
		
//		List<User_rank_vo> rankList = RankMgr.getInstance().getRankList();
//		
//		System.out.println(rankList.size());
//		for(User_rank_vo vo : rankList){
//			System.out.println(vo);
//		}
//		System.out.println();
//		List<User_rank_vo> rankList2 = RankMgr.getInstance().getRankList(1);
//		for(User_rank_vo vo : rankList2){
//			System.out.println(vo);
//		}
		
		
		
	}
}
