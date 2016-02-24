package com.gy.app;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.gy.mapper.gyp_app.Mapper;
import com.nfl.kfb.util.DebugOption.CAMP_STATUS;

public class StartUp {
	
	private static final Logger logger = LoggerFactory
			.getLogger(StartUp.class);
	@Autowired
	private Mapper mapper;
	
	@PostConstruct
	public void startUp(){
		logger.info("#############startup");
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
