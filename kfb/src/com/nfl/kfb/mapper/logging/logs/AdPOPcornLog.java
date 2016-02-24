/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 9. 25.
 *
 */
public class AdPOPcornLog extends GameLog {
	
	public AdPOPcornLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.ADPOPCORN);
	}

}
