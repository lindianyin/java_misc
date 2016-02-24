package com.gy.model;

public class User_statistics {
	private int id;
	private int user_account_id;
	private int base_statistics_id;
	private java.util.Date time;
	private int number;
	private String ext;
	private String 	ip_port;
	
	
	
	public int getUser_account_id() {
		return user_account_id;
	}
	public void setUser_account_id(int user_account_id) {
		this.user_account_id = user_account_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getBase_statistics_id() {
		return base_statistics_id;
	}
	public void setBase_statistics_id(int base_statistics_id) {
		this.base_statistics_id = base_statistics_id;
	}
	public java.util.Date getTime() {
		return time;
	}
	public void setTime(java.util.Date time) {
		this.time = time;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	public String getIp_port() {
		return ip_port;
	}
	public void setIp_port(String ip_port) {
		this.ip_port = ip_port;
	}
	
	
}
