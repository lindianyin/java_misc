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
public class PlayRewardClearLog extends GameLog {
	
	public PlayRewardClearLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.PLAY_ALL_CLEAR);
	}

	public void setRank(int gateAllClearRank) {
		setReserved0(gateAllClearRank);		// 관문 올클리어 등수
	}

}
