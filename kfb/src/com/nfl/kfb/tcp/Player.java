package com.nfl.kfb.tcp;

import java.util.Date;

public class Player {
	private String appId;
	private String nickName;
	private String img;
	private Date date;
	private long point;
	private boolean isOver;

	private int chId;
	private int chLv;
	
	
	private int itemType;
	private int itemValue;
	
	private boolean isWin;
	
	private Date matchDate;
	
	public Player(){}
	
	
	public Player(String appId, String nickName, String img, Date date,
			long point, boolean isOver, int chId, int chLv, int itemType,
			int itemValue, boolean isWin, Date matchDate) {
		super();
		this.appId = appId;
		this.nickName = nickName;
		this.img = img;
		this.date = date;
		this.point = point;
		this.isOver = isOver;
		this.chId = chId;
		this.chLv = chLv;
		this.itemType = itemType;
		this.itemValue = itemValue;
		this.isWin = isWin;
		this.matchDate = matchDate;
	}

	public Date getMatchDate() {
		return matchDate;
	}

	public void setMatchDate(Date matchDate) {
		this.matchDate = matchDate;
	}

	public boolean isWin() {
		return isWin;
	}

	public void setWin(boolean isWin) {
		this.isWin = isWin;
	}

	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public int getItemValue() {
		return itemValue;
	}

	public void setItemValue(int itemValue) {
		this.itemValue = itemValue;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getPoint() {
		return point;
	}

	public void setPoint(long point) {
		this.point = point;
	}

	public boolean isOver() {
		return isOver;
	}

	public void setOver(boolean isOver) {
		this.isOver = isOver;
	}

	public int getChId() {
		return chId;
	}

	public void setChId(int chId) {
		this.chId = chId;
	}

	public int getChLv() {
		return chLv;
	}

	public void setChLv(int chLv) {
		this.chLv = chLv;
	}

}
