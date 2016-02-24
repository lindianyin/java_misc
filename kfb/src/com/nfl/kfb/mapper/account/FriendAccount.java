/**
 * 
 */
package com.nfl.kfb.mapper.account;

/**
 * 친구에게 보여질 계정정보<br>
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
public class FriendAccount {
	
	private String appId;
	private int push;
	private int chId;
	private int chLv;
	private int petId;
	private String nickname;
	private String img;
	
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getPush() {
		return push;
	}

	public void setPush(int push) {
		this.push = push;
	}

	public int getChId() {
		return chId;
	}

	public void setChId(int chId) {
		this.chId = chId;
	}

	public int getChLv() {
		return chLv;
	}

	public void setChLv(int chLv) {
		this.chLv = chLv;
	}

	public int getPetId() {
		return petId;
	}

	public void setPetId(int petId) {
		this.petId = petId;
	}

	
}
