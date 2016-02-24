/**
 * 
 */
package com.nfl.kfb.mapper.account;

import com.nfl.kfb.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 *
 */
public class Account {
	
	public enum LOGIN_TY {
		IOS(0)
		, AND(1)
		;
		
		private final int valueForLog;
		LOGIN_TY(int valueForLog) {
			this.valueForLog = valueForLog;
		}
		public int getValueForLog() {
			return valueForLog;
		}
	}
	
	private static final int DEFAULT_GOLD = DebugOption.DEFAULT_GOLD;
	private static final int DEFAULT_BALL = DebugOption.DEFAULT_BALL;
	public static final int DEFAULT_SESSION_KEY = 0;
	public static final int DEFAULT_PLAY_GATE = 0;
	public static final int DEFAULT_PLAY_WEEK = 0;
	public static final int DEFAULT_PLAY_KEY = 0;
	public static final int DEFAULT_ATT_CNT = 0;
	public static final int DEFAULT_ATT_DT = 0;
	public static final int DEFAULT_PUNCH = 5;
	public static final int DEFAULT_PUNCH_DT = 0;
	public static final int DEFAULT_CH_ID = 600;
	public static final int DEFAULT_CH_LV = 1;
	public static final int DEFAULT_PET_ID = 700;
	public static final int DEFAULT_INV_CNT = 700;
//	public static final String DEFAULT_INV = Inv.DEFAULT_JSON_STRING;
	public static final int DEFAULT_TUTORIAL = 0;		// 0=false
	public static final int DEFAULT_REVIEW = 0;		// 0=false
	public static final int DEFAULT_PUSH = 1;		// 1=true
	
	private String appId;
	private int gold = DEFAULT_GOLD;
	private int ball = DEFAULT_BALL;
	private int sessionKey = DEFAULT_SESSION_KEY;
	private int playGate = DEFAULT_PLAY_GATE;
	private int playWeek = DEFAULT_PLAY_WEEK;
	private int playKey = DEFAULT_PLAY_KEY;
	
	private int attCnt = DEFAULT_ATT_CNT;		// 출석 일수
	private int attDt = DEFAULT_ATT_DT;		// 마지막 출석 시각
	
	private int punch = DEFAULT_PUNCH;			// 주먹 개수
	private int punchDt = DEFAULT_PUNCH_DT;		// 주먹 사용한 시각
	
	private int chId = DEFAULT_CH_ID;
	private int chLv = DEFAULT_CH_LV;
	private int petId = DEFAULT_PET_ID;
	
//	private String inv = DEFAULT_INV;
	private int tuto = DEFAULT_TUTORIAL;
	private int rev = DEFAULT_REVIEW;
	
	private int push = DEFAULT_PUSH;
	
	private int creDt = 0;
	private int loginDt = 0;
	private String loginType = "UNKNOWN";
	private String loginVer = "UNKNOWN";
	private String ccode = "KR";
	
	private String nickname="游客";
	
	private String tags="";
	
	private String img;
	
	
	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
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

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public int getBall() {
		return ball;
	}

	public void setBall(int ball) {
		this.ball = ball;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}

	public int getPlayGate() {
		return playGate;
	}

	public void setPlayGate(int playGate) {
		this.playGate = playGate;
	}

	public int getPlayWeek() {
		return playWeek;
	}

	public void setPlayWeek(int playWeek) {
		this.playWeek = playWeek;
	}

	public int getPlayKey() {
		return playKey;
	}

	public void setPlayKey(int playKey) {
		this.playKey = playKey;
	}

	public int getAttCnt() {
		return attCnt;
	}

	public void setAttCnt(int attCnt) {
		this.attCnt = attCnt;
	}

	public int getAttDt() {
		return attDt;
	}

	public void setAttDt(int attDt) {
		this.attDt = attDt;
	}

	public int getPunch() {
		return punch;
	}

	public void setPunch(int punch) {
		this.punch = punch;
	}

	public int getPunchDt() {
		return punchDt;
	}
	
	public void usePunch(int nowEpoch, int cnt) {
		int newCnt = getPunch() - cnt;
		
		// 리젠중이었음. 리젠시간 변동은 없고, 갯수만 줄임
		if (getPunch() < DebugOption.PUNCH_REGEN_MAX) {
			setPunch(newCnt);
		}
		else {		// 리젠중이 아니었음. 리젠시간, 갯수가 새로운 값으로.
			setPunch(newCnt);
			setPunchDt(nowEpoch);
		}
		regenPunch(nowEpoch);
	}
	
	public void regenPunch(int nowEpoch) {
		// 리젠 최대값
		if (getPunch() >= DebugOption.PUNCH_REGEN_MAX) {
			setPunchDt(nowEpoch);
			return;
		}
		
		int punch = getPunch();
		int punchDt = getPunchDt();
		
		while (punch < DebugOption.PUNCH_REGEN_MAX
				&& (punchDt + DebugOption.PUNCH_REGEN_TIME_EPOCH[punch]) < nowEpoch
				) {
			punchDt += DebugOption.PUNCH_REGEN_TIME_EPOCH[punch];
			punch += 1;
		}
		
		// 다 찼으면 기준시각을 지금으로 바꿈
		if (punch >= DebugOption.PUNCH_REGEN_MAX) {
			punchDt = nowEpoch;
		}
		
		setPunch(punch);
		setPunchDt(punchDt);
	}
	
	public int getPunchRemainDt(int nowEpoch) {
		if (getPunch() >= DebugOption.PUNCH_REGEN_MAX) {
			return 0;
		}
		else {
			final int nextRegenTime = getPunchDt() + DebugOption.PUNCH_REGEN_TIME_EPOCH[getPunch()];
			return nextRegenTime - nowEpoch;
		}
	}
	
	public void setPunchDt(int punchDt) {
		this.punchDt = punchDt;
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

//	public String getInv() {
//		return inv;
//	}
//
//	public void setInv(String inv) {
//		this.inv = inv;
//	}

	public int getTuto() {
		return tuto;
	}

	public void setTuto(int tuto) {
		this.tuto = tuto;
	}

	public int getRev() {
		return rev;
	}

	public void setRev(int rev) {
		this.rev = rev;
	}

	public int getPush() {
		return push;
	}

	public void setPush(int push) {
		this.push = push;
	}

	public int getCreDt() {
		return creDt;
	}

	public void setCreDt(int creDt) {
		this.creDt = creDt;
	}

	public int getLoginDt() {
		return loginDt;
	}

	public void setLoginDt(int loginDt) {
		this.loginDt = loginDt;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}

	public String getLoginVer() {
		return loginVer;
	}

	public void setLoginVer(String loginVer) {
		this.loginVer = loginVer;
	}

	public String getCcode() {
		return ccode;
	}

	public void setCcode(String ccode) {
		this.ccode = ccode;
	}

}
