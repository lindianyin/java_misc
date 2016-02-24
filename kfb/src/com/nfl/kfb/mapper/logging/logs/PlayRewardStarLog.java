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
public class PlayRewardStarLog extends GameLog {
	
	public PlayRewardStarLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.PLAY_STAR);
	}

	public void setStar(int star) {
		setReserved0(star);			// reserv0을 별 개수로 기록
	}

}
