package com.nfl.kfb.friend;

public class Friend {
	
	private String nickname;
	private String userid;
	private String profile_image_url;
	private boolean message_blocked;
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getProfile_image_url() {
		return profile_image_url;
	}
	public void setProfile_image_url(String profile_image_url) {
		this.profile_image_url = profile_image_url;
	}
	public boolean isMessage_blocked() {
		return message_blocked;
	}
	public void setMessage_blocked(boolean message_blocked) {
		this.message_blocked = message_blocked;
	}

	
}

