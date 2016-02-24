/**
 * 
 */
package com.nfl.kfb.push.collector;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.nfl.kfb.push.mapper.PushDevice;
import com.nfl.kfb.push.mapper.PushDevice.DEVICE_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 10. 1.
 *
 */
public class KfbPushMessage {
	
	private String pushUniqueKey;
	
	private String title;
	private String msg;

	// 푸시 노티 대상자
	private final List<PushDevice> pushDevices = new LinkedList<PushDevice>();
	
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public void addPushDevice(PushDevice pushDevice) {
		if (pushDevice == null)
			return;
		pushDevices.add(pushDevice);
	}
	
	public void addPushDevice(Collection<PushDevice> pushDevice) {
		if (pushDevice == null)
			return;
		pushDevices.addAll(pushDevice);
	}
	
	public List<PushDevice> getPushDevices(DEVICE_TYPE deviceType) {
		List<PushDevice> tmpPushDevices = new LinkedList<PushDevice>();
		
		for (PushDevice pushDevice : pushDevices) {
			if (pushDevice.getDeviceType() == deviceType) {
				tmpPushDevices.add(pushDevice);
			}
		}
		
		return tmpPushDevices;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPushUniqueKey() {
		return pushUniqueKey;
	}

	public void setPushUniqueKey(String pushUniqueKey) {
		this.pushUniqueKey = pushUniqueKey;
	}

}

