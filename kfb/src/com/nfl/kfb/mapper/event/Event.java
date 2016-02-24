/**
 * 
 */
package com.nfl.kfb.mapper.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfl.kfb.event.EventService.EVENT_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 10. 14.
 *
 */
public class Event {
	
	private int event_key;
	private int start_dt;
	private int end_dt;
	private int event_type;
	private int item_id;
	private int item_cnt;
	private String mail_msg;
	private int mail_keep_days;
	private String condition;
	private String img;
	private String  content;
	private String  title;
	private String reward;
	private String items;//basereward 中的id区间
	
	
	public String getItems() {
		return items;
	}
	public void setItems(String items) {
		this.items = items;
	}
	public String getReward() {
		return reward;
	}
	public void setReward(String reward) {
		this.reward = reward;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public int getEvent_key() {
		return event_key;
	}
	public void setEvent_key(int event_key) {
		this.event_key = event_key;
	}
	public int getStart_dt() {
		return start_dt;
	}
	public void setStart_dt(int start_dt) {
		this.start_dt = start_dt;
	}
	public int getEnd_dt() {
		return end_dt;
	}
	public void setEnd_dt(int end_dt) {
		this.end_dt = end_dt;
	}
	public int getEvent_type() {
		return event_type;
	}
	public void setEvent_type(int event_type) {
		this.event_type = event_type;
	}
	public int getItem_id() {
		return item_id;
	}
	public void setItem_id(int item_id) {
		this.item_id = item_id;
	}
	public int getItem_cnt() {
		return item_cnt;
	}
	public void setItem_cnt(int item_cnt) {
		this.item_cnt = item_cnt;
	}
	public String getMail_msg() {
		return mail_msg;
	}
	public void setMail_msg(String mail_msg) {
		this.mail_msg = mail_msg;
	}
	public int getMail_keep_days() {
		return mail_keep_days;
	}
	public void setMail_keep_days(int mail_keep_days) {
		this.mail_keep_days = mail_keep_days;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	
	


}
