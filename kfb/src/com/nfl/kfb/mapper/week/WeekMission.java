/**
 * 
 */
package com.nfl.kfb.mapper.week;

/**
 * @author KimSeongsu
 * @since 2013. 8. 12.
 *
 */
public class WeekMission {
	
	private int week;
	private int gate;
	private int mission1Id;
	private int mission1Value;
	private int mission2Id;
	private int mission2Value;
	private int reward1ItemId;
	private int reward1ItemCnt;
	private int reward2ItemId;
	private int reward2ItemCnt;
	private int punch;
	
	
	public int getMission2Value() {
		return mission2Value;
	}

	public void setMission2Value(int mission2Value) {
		this.mission2Value = mission2Value;
	}

	public int getReward1ItemId() {
		return reward1ItemId;
	}

	public void setReward1ItemId(int reward1ItemId) {
		this.reward1ItemId = reward1ItemId;
	}

	public int getReward1ItemCnt() {
		return reward1ItemCnt;
	}

	public void setReward1ItemCnt(int reward1ItemCnt) {
		this.reward1ItemCnt = reward1ItemCnt;
	}

	public int getReward2ItemId() {
		return reward2ItemId;
	}

	public void setReward2ItemId(int reward2ItemId) {
		this.reward2ItemId = reward2ItemId;
	}

	public int getReward2ItemCnt() {
		return reward2ItemCnt;
	}

	public void setReward2ItemCnt(int reward2ItemCnt) {
		this.reward2ItemCnt = reward2ItemCnt;
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

	public int getMission1Id() {
		return mission1Id;
	}

	public void setMission1Id(int mission1Id) {
		this.mission1Id = mission1Id;
	}

	public int getMission1Value() {
		return mission1Value;
	}

	public void setMission1Value(int mission1Value) {
		this.mission1Value = mission1Value;
	}

	public int getMission2Id() {
		return mission2Id;
	}

	public void setMission2Id(int mission2Id) {
		this.mission2Id = mission2Id;
	}

	public int getPunch() {
		return punch;
	}

	public void setPunch(int punch) {
		this.punch = punch;
	}

}
