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
public class AchieveLog extends GameLog {
	
	public AchieveLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.ACHIEVE);
	}

	public void setAchIdx(int achIdx) {
		setReserved0(achIdx);		// 보상받은 주간업적 idx
	}
	
}
