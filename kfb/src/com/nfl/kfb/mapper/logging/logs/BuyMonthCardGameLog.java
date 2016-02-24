/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 8. 16.
 *
 */
public class BuyMonthCardGameLog extends GameLog {
	
	public BuyMonthCardGameLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.BUY_MONTH_CARD);
	}

	public void setStore(STORE_TYPE storeType) {
		setReserved0(storeType.getValue());
	}
	
}
