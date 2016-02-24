package com.gy.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gy.mapper.gyp_app.Mapper;
public class MapperMgr {
	@Autowired
	private Mapper mapper;
	
	private static MapperMgr  single = null;
	private MapperMgr(){
		
	}
	
	public void setMapper(Mapper mapper){
		this.mapper = mapper;
	}
	
	public synchronized static MapperMgr getInstance(){
		if(single == null){
			single = new MapperMgr();
		}
		return single;
	}
	
	public Mapper getMapper(){
		return mapper;
	}
	
	
}
