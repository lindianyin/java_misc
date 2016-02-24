/**
 * 
 */
package com.nfl.kfb.mapper.logging;

import java.util.Calendar;

import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 8. 14.
 *
 */
public class CurrencyLog {
	
	private int logKey;		// auto_crease
	private int month;
	private int epoch;
	private String appId;
	private int store;
	private float currency;
	private String productId;
	private String currencyType;
	private String ccode;
	
	public CurrencyLog(DateUtil dateUtil, String appId) {
		this.epoch = dateUtil.getNowEpoch();
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateUtil.getNow());
		this.month = cal.get(Calendar.YEAR) * 100 + (cal.get(Calendar.MONTH) + 1);
		
		this.appId = appId;
	}
	
	public int getMonth() {
		return month;
	}
	
	public void setMonth(int month) {
		this.month = month;
	}

	public int getEpoch() {
		return epoch;
	}

	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public int getStore() {
		return store;
	}

	public void setStore(STORE_TYPE store) {
		this.store = store.getValue();
	}

	public float getCurrency() {
		return currency;
	}

	public void setCurrency(float currency) {
		this.currency = currency;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public String getCcode() {
		return ccode;
	}

	public void setCcode(String ccode) {
		this.ccode = ccode;
	}

	public int getLogKey() {
		return logKey;
	}

	public void setLogKey(int logKey) {
		this.logKey = logKey;
	}

}
