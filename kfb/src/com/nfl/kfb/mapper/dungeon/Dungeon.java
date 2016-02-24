/**
 * 
 */
package com.nfl.kfb.mapper.dungeon;

/**
 * @author KimSeongsu
 * @since 2013. 11. 13.
 *
 */
public class Dungeon {
	
	private String appId;
	private int playDt;
	private int playLimit;
	private int playCnt;
	private int punch;
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getPlayDt() {
		return playDt;
	}

	public void setPlayDt(int playDt) {
		this.playDt = playDt;
	}

	public int getPlayLimit() {
		return playLimit;
	}

	public void setPlayLimit(int playLimit) {
		this.playLimit = playLimit;
	}

	public int getPlayCnt() {
		return playCnt;
	}

	public void setPlayCnt(int playCnt) {
		this.playCnt = playCnt;
	}

	public int getPunch() {
		return punch;
	}

	public void setPunch(int punch) {
		this.punch = punch;
	}

}
