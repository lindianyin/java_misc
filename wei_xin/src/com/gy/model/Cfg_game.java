package com.gy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Cfg_game {
	private int id;
	private String name;
	private int repute;
	private String memo;
	private String url;
	private String logo;
	//@JsonIgnore
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRepute() {
		return repute;
	}
	public void setRepute(int repute) {
		this.repute = repute;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	
	
	

}
