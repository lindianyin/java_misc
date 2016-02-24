package com.nfl.kfb.mapper.account;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FightActivityRankVo {
	public int rank;
	private String appId;
	private String nickname;
	private String img;
	private int point;
	
	
	@JsonIgnore
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public int getPoint() {
		return point;
	}
	public void setPoint(int point) {
		this.point = point;
	}
	
	
}
