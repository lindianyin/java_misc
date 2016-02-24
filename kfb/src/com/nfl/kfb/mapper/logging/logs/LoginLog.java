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
public class LoginLog extends GameLog {
	
	public LoginLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.LOGIN);
	}

	public void setLoginType(int valueForLog) {
		setReserved1(valueForLog);
	}
	
}
