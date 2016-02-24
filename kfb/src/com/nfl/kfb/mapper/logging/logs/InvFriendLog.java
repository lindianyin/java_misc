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
public class InvFriendLog extends GameLog {
	
	private boolean reward = false;
	
	public InvFriendLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.INV_FRIEND);
	}

	public void setHasReward() {
		reward = true;
	}
	
	public boolean hasReward() {
		return reward;
	}

	public void setRewardIdx(int invCnt) {
		setReserved0(invCnt);					// reserv0에 초대수를 기록
	}
	
}
