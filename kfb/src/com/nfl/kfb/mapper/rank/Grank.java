package com.nfl.kfb.mapper.rank;

public class Grank {
	private int id;
	private String appId;
	private int week;
	private int gate;
	private long point;
	private int reward_dt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getWeek() {
		return week;
	}

	public void setWeek(int week) {
		this.week = week;
	}

	public int getGate() {
		return gate;
	}

	public void setGate(int gate) {
		this.gate = gate;
	}

	public long getPoint() {
		return point;
	}

	public void setPoint(long point) {
		this.point = point;
	}

	public int getReward_dt() {
		return reward_dt;
	}

	public void setReward_dt(int reward_dt) {
		this.reward_dt = reward_dt;
	}

}
