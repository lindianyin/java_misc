/**
 * 
 */
package com.nfl.kfb.mapper.rank;



/**
 * 
 * @author KimSeongsu
 * @since 2013. 7. 24.
 *
 */
public class Rank {
	
	public static final int DEFAULT_GATE = -1;
	public static final int DEFAULT_POINT = 0;
	public static final int DEFAULT_REWARD_DT = 0;
	
	private String appId;
	private long point = DEFAULT_POINT;
	private int gate = DEFAULT_GATE;
	private int rank = -1;
	
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public long getPoint() {
		return point;
	}

	public void setPoint(long point) {
		this.point = point;
	}

	public int getGate() {
		return gate;
	}

	public void setGate(int gate) {
		this.gate = gate;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
