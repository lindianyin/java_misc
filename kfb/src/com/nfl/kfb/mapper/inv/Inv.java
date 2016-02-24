/**
 * 
 */
package com.nfl.kfb.mapper.inv;

import java.util.HashSet;
import java.util.Set;

/**
 * @author KimSeongsu
 * @since 2013. 8. 14.
 *
 */
public class Inv {
	
	private String appId;
	private int cnt = 0;
	private int rw0Dt = 0;
	private int rw1Dt = 0;
	private int rw2Dt = 0;
	private int rw3Dt = 0;
	private int rw4Dt = 0;
	private int rw5Dt = 0;
	
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public int getRw0Dt() {
		return rw0Dt;
	}

	public void setRw0Dt(int rw0Dt) {
		this.rw0Dt = rw0Dt;
	}

	public int getRw1Dt() {
		return rw1Dt;
	}

	public void setRw1Dt(int rw1Dt) {
		this.rw1Dt = rw1Dt;
	}

	public int getRw2Dt() {
		return rw2Dt;
	}

	public void setRw2Dt(int rw2Dt) {
		this.rw2Dt = rw2Dt;
	}

	public int getRw3Dt() {
		return rw3Dt;
	}

	public void setRw3Dt(int rw3Dt) {
		this.rw3Dt = rw3Dt;
	}
	
	public int getRw4Dt() {
		return rw4Dt;
	}

	public void setRw4Dt(int rw4Dt) {
		this.rw4Dt = rw4Dt;
	}

	public int getRw5Dt() {
		return rw5Dt;
	}

	public void setRw5Dt(int rw5Dt) {
		this.rw5Dt = rw5Dt;
	}

	public boolean gotReward(int ridx) {
		if (ridx == 0) 			return rw0Dt > 0;
		else if (ridx == 1) 			return rw1Dt > 0;
		else if (ridx == 2) 			return rw2Dt > 0;
		else if (ridx == 3) 			return rw3Dt > 0;
		else if (ridx == 4) 			return rw4Dt > 0;
		else if (ridx == 5) 			return rw5Dt > 0;
		else throw new RuntimeException("Unknown INV RIDX");
	}
	
	public void setRwDt(int ridx, int epoch) {
		if (ridx == 0) 			rw0Dt = epoch;
		else if (ridx == 1) 			rw1Dt = epoch;
		else if (ridx == 2) 			rw2Dt = epoch;
		else if (ridx == 3) 			rw3Dt = epoch;
		else if (ridx == 4) 			rw4Dt = epoch;
		else if (ridx == 5) 			rw5Dt = epoch;
		else throw new RuntimeException("Unknown INV RIDX");
	}

	public Set<Integer> getRidxSet() {
		Set<Integer> rewardIdxSet = new HashSet<Integer>();
		// 초대보상은 4개
		for (int i=0; i<4; i++) {
			if (gotReward(i))
				rewardIdxSet.add(i);
		}
		return rewardIdxSet;
	}

}
