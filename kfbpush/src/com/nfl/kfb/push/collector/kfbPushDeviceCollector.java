/**
 * 
 */
package com.nfl.kfb.push.collector;

import java.util.List;

import com.nfl.kfb.push.dao.PushSqlSession.DB_TARGET;
import com.nfl.kfb.push.mapper.PushDevice;

/**
 * @author KimSeongsu
 * @since 2013. 10. 1.
 *
 */
public interface kfbPushDeviceCollector {
	
	List<PushDevice> collectPushDevices(DB_TARGET db);

}
