/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * 신규유저
 * @author KimSeongsu
 * @since 2013. 9. 10.
 *
 */
public class NewRegisterUserLog extends GameLog {
	
	public NewRegisterUserLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.NEW_REGISTER_USER);
	}
	
	public void setLoginType(int valueForLog) {
		setReserved1(valueForLog);
	}
	
}
