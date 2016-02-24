package com.nfl.kfb.friend;

import java.util.List;

public class FriendList {
	private int status;
	private int friends_count;
	private List<Friend> friends_info;
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getFriends_count() {
		return friends_count;
	}
	public void setFriends_count(int friends_count) {
		this.friends_count = friends_count;
	}
	public List<Friend> getFriends_info() {
		return friends_info;
	}
	public void setFriends_info(List<Friend> friends_info) {
		this.friends_info = friends_info;
	}
	
}
