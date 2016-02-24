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
public class CurrencyGameLog extends GameLog {
	
	public CurrencyGameLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.CURRENCY);
	}

	public void setStore(STORE_TYPE storeType) {
		setReserved0(storeType.getValue());
	}
	
}
