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
public class AttendanceLog extends GameLog {
	
	public AttendanceLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.ATTENDANCE);
	}

	public void setAttCnt(int attCnt) {
		setReserved0(attCnt);		// reserv0를 출석일차로 사용
	}

}
