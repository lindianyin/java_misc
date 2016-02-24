package com.gy.app;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.gy.mapper.gyp_app.Mapper;

public class StartUp {
	
	@Autowired
	private Mapper mapper;
	
	@PostConstruct
	public void startUp(){
		System.out.println("#############startup");
		MapperMgr.getInstance().setMapper(mapper);
	}
}
