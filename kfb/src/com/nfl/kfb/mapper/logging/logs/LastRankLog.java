/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 8. 13.
 *
 */
public class LastRankLog extends GameLog {
	
	public LastRankLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.LAST_RANK);
	}

	public void setRank(int rank) {
		setReserved0(rank);			// reserv0에 등수를 기록
	}

}
