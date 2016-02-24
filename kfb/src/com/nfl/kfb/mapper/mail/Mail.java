/**
 * 
 */
package com.nfl.kfb.mapper.mail;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
public class Mail {
	
	public static final String SENDER_ID_ADMIN = "0";
	
	private int mailKey;
	private String owner;
	private String sender;
	private int item;
	private String msg;
	private int cnt;
	private int delDt;
	
	private String title;
	
	public int getMailKey() {
		return mailKey;
	}
	
	public void setMailKey(int mailKey) {
		this.mailKey = mailKey;
	}

	public int getType() {
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

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	
	
	
	
	

}
