/**
 * 
 */
package com.nfl.kfb.push.mapper;

/**
 * @author KimSeongsu
 * @since 2013. 10. 17.
 *
 */
public class PushScheduler {
	
	private int pushSchedulerKey;
	private String pushTitle;
	private String pushMsg;
	private String collectorClass;
	private int pushDt;
	
	public int getPushSchedulerKey() {
		return pushSchedulerKey;
	}
	
	public void setPushSchedulerKey(int pushSchedulerKey) {
		this.pushSchedulerKey = pushSchedulerKey;
	}

	public String getPushTitle() {
		return pushTitle;
	}

	public void setPushTitle(String pushTitle) {
		this.pushTitle = pushTitle;
	}

	public String getPushMsg() {
		return pushMsg;
	}

	public void setPushMsg(String pushMsg) {
		this.pushMsg = pushMsg;
	}

	public String getCollectorClass() {
		return collectorClass;
	}

	public void setCollectorClass(String collectorClass) {
		this.collectorClass = collectorClass;
	}

	public int getPushDt() {
		return pushDt;
	}

	public void setPushDt(int pushDt) {
		this.pushDt = pushDt;
	}

}
