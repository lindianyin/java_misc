/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 9. 30.
 *
 */
public class RandomInvResurrectionLog extends GameLog {
	
	public RandomInvResurrectionLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.RANDOM_INV_RESURRECTION);
	}
	
	/**
	 * 랜덤 친구초대
	 * @param inv
	 */
	public void setInv(int inv) {
		setReserved0(inv);
	}
	
}
