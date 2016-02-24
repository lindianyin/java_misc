/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 8. 13.
 *
 */
public class BuyItemLog extends GameLog {
	
	private boolean success = false;
	
	public BuyItemLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.BUY_ITEM);
	}

	public void setDiscount(int discount) {
		setReserved0(discount);			// reserv0에 할인율을 기록
	}
	
	public void setStore(STORE_TYPE store) {
		setReserved1(store.getValue());
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
}
