package com.gy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nfl.kfb.util.DebugOption.CAMP_STATUS;

public class RmMatch {
	private int rm_camp_id;
	private int rm_base_match_id;
	private int rm_base_round_id;
	private int score;
	//private Date start_time;
	//private Date end_time;
	private int count;
	private int status = CAMP_STATUS.READY.getValue();
	
	private long cost_time = 0;
	
	private int rank;
	
	public int reward;
	public int penalty;
	
	public double total;
	
	public long cost_time1 = 0;
	
	@JsonIgnore
	public int submit_count = 0;
	
	
	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getCost_time() {
		return cost_time;
	}

	public void setCost_time(long cost_time) {
		this.cost_time = cost_time;
	}

	@JsonIgnore
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	//@JsonIgnore
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getRm_camp_id() {
		return rm_camp_id;
	}

	public void setRm_camp_id(int rm_camp_id) {
		this.rm_camp_id = rm_camp_id;
	}

	@JsonIgnore
	public int getRm_base_match_id() {
		return rm_base_match_id;
	}

	public void setRm_base_match_id(int rm_base_match_id) {
		this.rm_base_match_id = rm_base_match_id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@JsonIgnore
	public int getRm_base_round_id() {
		return rm_base_round_id;
	}

	public void setRm_base_round_id(int rm_base_round_id) {
		this.rm_base_round_id = rm_base_round_id;
	}

	@Override
	public String toString() {
		return "RmMatch [rm_camp_id=" + rm_camp_id + ", rm_base_match_id="
				+ rm_base_match_id + ", rm_base_round_id=" + rm_base_round_id
				+ ", score=" + score + ", count=" + count + ", status="
				+ status + ", cost_time=" + cost_time + ", rank=" + rank
				+ ", reward=" + reward + ", penalty=" + penalty + ", total="
				+ total + "]";
	}

//	@JsonIgnore
//	public Date getStart_time() {
//		return start_time;
//	}
//
//	public void setStart_time(Date start_time) {
//		this.start_time = start_time;
//	}

//	@JsonIgnore
//	public Date getEnd_time() {
//		return end_time;
//	}
//
//	public void setEnd_time(Date end_time) {
//		this.end_time = end_time;
//	}
	
	
	
}
