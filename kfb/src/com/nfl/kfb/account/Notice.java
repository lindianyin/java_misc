package com.nfl.kfb.account;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Notice {
	private int id;
	private String content;
	private Date startDate;
	private Date endDate;
	private int interval;
	private int isMultiple;
	@JsonIgnore
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	@JsonIgnore
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	@JsonIgnore
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	//@JsonIgnore
	public int getIsMultiple() {
		return isMultiple;
	}
	public void setIsMultiple(int isMultiple) {
		this.isMultiple = isMultiple;
	}

	
	
}
