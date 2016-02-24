/**
 * 
 */
package com.nfl.kfb.push.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KimSeongsu
 * @since 2013. 10. 2.
 *
 */
public class PushDevice {
	
	private static final Logger logger = LoggerFactory.getLogger(PushDevice.class);
	
	public enum DEVICE_TYPE {
		AND
		, IOS
	}
	
	private String appId;
	private String token;
	private DEVICE_TYPE deviceType;
	
	// sentDt, sentResult, unregistered
	
	public String getAppId() {
		return appId;
	}
	
	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DEVICE_TYPE getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		try {
			this.deviceType = DEVICE_TYPE.valueOf(deviceType);
		} catch (Exception e) {
			logger.error("Unknown DEVICE_TYPE=" + deviceType, e);
		}
	}
	
	
}
