/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 11. 8.
 *
 */
public class GambleLog extends GameLog {
	
	public GambleLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.GAMBLE);
	}

	public void setGamblePoint(int gamblePoint) {
		setReserved0(gamblePoint);
	}

}
