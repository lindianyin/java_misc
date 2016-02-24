package com.nfl.kfb.mapper.account;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ActivityBoard {
	private int id;
	private String title;
	private String content;
	private Date start_time;
	private Date end_time;
	private String img;
	private String reward;
	private String subtitle;
	private int is_rank;
	
	
	
	
	public int getIs_rank() {
		return is_rank;
	}
	public void setIs_rank(int is_rank) {
		this.is_rank = is_rank;
	}
	public String getSubtitle() {
		return subtitle;
	}
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	public String getReward() {
		return reward;
	}
	public void setReward(String reward) {
		this.reward = reward;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@JsonFormat(pattern = "MM月dd日")
	public Date getStart_time() {
		return start_time;
	}
	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}
	@JsonFormat(pattern = "MM月dd日")
	public Date getEnd_time() {
		return end_time;
	}
	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	
}
