/**
 * 
 */
package com.nfl.kfb.maintenance.mapper;

/**
 * @author KimSeongsu
 * @since 2013. 10. 22.
 *
 */
public class Mail {
	
	private int mailKey;
	private String owner;
	private String sender;
	private int item;
	private int cnt;
	private int delDt;
	private String msg;
	
	public int getMailKey() {
		return mailKey;
	}
	
	public void setMailKey(int mailKey) {
		this.mailKey = mailKey;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public int getItem() {
		return item;
	}

	public void setItem(int item) {
		this.item = item;
	}

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public int getDelDt() {
		return delDt;
	}

	public void setDelDt(int delDt) {
		this.delDt = delDt;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
