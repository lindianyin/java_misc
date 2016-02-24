/**
 * 
 */
package com.nfl.kfb.maintenance.mapper;

/**
 * @author KimSeongsu
 * @since 2013. 10. 22.
 *
 */
public class GameLog {
	
	private int month;				// 201308, 201309, ...
	private int epoch;				// created time
	private String appId;
	private int logType;
	private float currency = 0.0f;
	private int itemId = 0;
	private int itemCnt = 0;
	private int addGold = 0;
	private int nowGold = 0;
	private int addBall = 0;
	private int nowBall = 0;
	private int addPunch = 0;
	private int nowPunch = 0;
	private long reserved0 = 0;
	private long reserved1 = 0;
	
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getEpoch() {
		return epoch;
	}
	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public int getLogType() {
		return logType;
	}
	public void setLogType(int logType) {
		this.logType = logType;
	}
	public float getCurrency() {
		return currency;
	}
	public void setCurrency(float currency) {
		this.currency = currency;
	}
	public int getItemId() {
		return itemId;
	}
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	public int getItemCnt() {
		return itemCnt;
	}
	public void setItemCnt(int itemCnt) {
		this.itemCnt = itemCnt;
	}
	public int getAddGold() {
		return addGold;
	}
	public void setAddGold(int addGold) {
		this.addGold = addGold;
	}
	public int getNowGold() {
		return nowGold;
	}
	public void setNowGold(int nowGold) {
		this.nowGold = nowGold;
	}
	public int getAddBall() {
		return addBall;
	}
	public void setAddBall(int addBall) {
		this.addBall = addBall;
	}
	public int getNowBall() {
		return nowBall;
	}
	public void setNowBall(int nowBall) {
		this.nowBall = nowBall;
	}
	public int getAddPunch() {
		return addPunch;
	}
	public void setAddPunch(int addPunch) {
		this.addPunch = addPunch;
	}
	public int getNowPunch() {
		return nowPunch;
	}
	public void setNowPunch(int nowPunch) {
		this.nowPunch = nowPunch;
	}
	public long getReserved0() {
		return reserved0;
	}
	public void setReserved0(long reserved0) {
		this.reserved0 = reserved0;
	}
	public long getReserved1() {
		return reserved1;
	}
	public void setReserved1(long reserved1) {
		this.reserved1 = reserved1;
	}


}
