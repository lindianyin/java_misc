/**
 * 
 */
package com.nfl.kfb.push.sender;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nfl.kfb.push.collector.KfbPushMessage;
import com.nfl.kfb.push.collector.kfbPushDeviceCollector;
import com.nfl.kfb.push.dao.PushSqlSession.DB_TARGET;
import com.nfl.kfb.push.mapper.PushDevice;

/**
 * @author KimSeongsu
 * @since 2013. 10. 1.
 *
 */
public class KfbPushSender extends AbstractKfbPushSender {
	
	private static Logger logger = LoggerFactory.getLogger(KfbPushSender.class);
	
	public static void main(String[] args) {
		// debug
		System.out.println("args.length = " + args.length);
		for (int i=0; i<args.length; i++) {
			System.out.println("args[" + i + "] = " + args[i]);
		}
		
		if (args.length != 4) {
			System.out.println("Usage : KfbPushSender [pushUniqueKey(string)] [db(REAL/TEST)] [msg(string)] [collectorClassName]");
			System.exit(0);
		}
		
		try {
			String pushUniqueKey = args[0];
			DB_TARGET db = DB_TARGET.valueOf(args[1]);
			String msg = args[2];
			String collectorClassName = args[3];
			
			logger.info("PUSH_UNIQUE_KEY[{}] STARTED", pushUniqueKey);
			
			@SuppressWarnings("unchecked")
			Class<kfbPushDeviceCollector> collectorClass = (Class<kfbPushDeviceCollector>) Class.forName(collectorClassName);
			kfbPushDeviceCollector deviceCollector = collectorClass.newInstance();
			List<PushDevice> pushDevices = deviceCollector.collectPushDevices(db);
			
			logger.info("PUSH_UNIQUE_KEY[{}] COLLECTED_DEVICES[{}]", pushUniqueKey, pushDevices.size());
			
			KfbPushMessage kfbPushMessage = new KfbPushMessage();
			kfbPushMessage.setPushUniqueKey(pushUniqueKey);
			kfbPushMessage.setTitle("쿵푸버드 for kakao");
			kfbPushMessage.setMsg(msg);
			kfbPushMessage.addPushDevice(pushDevices);
			
			KfbPushSender sender = new KfbPushSender();
			sender.sendGcm(db, kfbPushMessage, GcmSender.class);
			sender.sendIos(db, kfbPushMessage);
		} catch (Exception e) {
			System.out.println("Usage : KfbPushSender [pushUniqueKey(string)] [db(REAL/TEST)] [msg(string)] [collectorClassName]");
			logger.error("KfbPushSender", e);
			e.printStackTrace();
			System.exit(0);
		}
		
		// for test
//		testAndroidPush("88311665281022209", 
//				DEVICE_TYPE.ANDROID, 
//				"APA91bFr30DtqpiDUEYLcVjFl206BTTIW-uJDmCs1jnuaUvI9EGp8SkAQk0ATlpLmcursRrl-iRbbjVdhz8iOa23oVOvbcsJOsz7sCU60c-DZO5Mi3omX38Bk8YCzRjHoV83votCLYrTsiOXGdEL5Q08rQ6_s2L9LdYl5c4hKKe_krFUxlhpO0s",
//				"push testest...!!");
	}
	
}
