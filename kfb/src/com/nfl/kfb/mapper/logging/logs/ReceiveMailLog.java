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
public class ReceiveMailLog extends GameLog {
	
	private boolean hasItem = false;
	
	public ReceiveMailLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.RECEIVE);
	}

	public void setSender(String sender) {
		try {
			setReserved0(Long.parseLong(sender));			// reserv0를 보낸사람 APPID로 사용
		} catch (Exception e) {
			setReserved0(-1);
		}
	}
	
	public void setHasItem() {
		hasItem = true;
	}
	
	public boolean hasItem() {
		return hasItem;
	}

	public void setMailKey(int mailKey) {
		setReserved1(mailKey);
	}
	
}
