/**
 * 
 */
package com.nfl.kfb.mapper.appver;

/**
 * @author KimSeongsu
 * @since 2013. 7. 30.
 *
 */
public class Appver {
	
	private String app;
	private String ver;
	private int needUpdate;
	private int ableSendPunch;
	
	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}
	
	public String getVer() {
		return ver;
	}
	
	public void setVer(String ver) {
		this.ver = ver;
	}
	
	public int getNeedUpdate() {
		return needUpdate;
	}
	
	public void setNeedUpdate(int needUpdate) {
		this.needUpdate = needUpdate;
	}
	
	public int getAbleSendPunch() {
		return ableSendPunch;
	}
	
	public void setAbleSendPunch(int ableSendPunch) {
		this.ableSendPunch = ableSendPunch;
	}

	public boolean isNeedUpdate() {
		return needUpdate != 0;
	}
	
	public boolean isAbleSendPunch() {
		return ableSendPunch != 0;
	}

}
