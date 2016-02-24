/**
 * 
 */
package com.nfl.kfb.mapper.rank;

/**
 * @author KimSeongsu
 * @since 2013. 7. 24.
 *
 */
public class GateRank {
	
	private String appId;
	private int gate;
//	private int week;
	private int point;
	private int rwDt;
	private int star;
	
	private int chid;
	
	
	
	
	
	
	

	public int getChid() {
		return chid;
	}

	public void setChid(int chid) {
		this.chid = chid;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	private String nickname;
	
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getGate() {
		return gate;
	}
	
	public void setGate(int gate) {
		this.gate = gate;
	}

	public int getPoint() {
		return point;
	}

	public void setPoint(int point) {
		this.point = point;
	}

	public int getRwDt() {
		return rwDt;
	}

	public void setRwDt(int rwDt) {
		this.rwDt = rwDt;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

}
