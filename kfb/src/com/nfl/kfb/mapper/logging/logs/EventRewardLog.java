/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 10. 15.
 *
 */
public class EventRewardLog extends GameLog {
	
	public EventRewardLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.EVENT_REWARD);
	}

	public void setMailKey(int mailKey) {
		setReserved0(mailKey);
	}

	public void setEventKey(int eventKey) {
		setReserved1(eventKey);
	}
	
}
