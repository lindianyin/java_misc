/**
 * 
 */
package com.nfl.kfb.push.sender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.nfl.kfb.push.collector.KfbPushMessage;
import com.nfl.kfb.push.dao.PushSqlSession;
import com.nfl.kfb.push.dao.PushSqlSession.DB_TARGET;
import com.nfl.kfb.push.mapper.PushDevice;
import com.nfl.kfb.push.mapper.PushDevice.DEVICE_TYPE;
import com.nfl.kfb.push.mapper.PushMapper;
import com.nfl.kfb.push.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 10. 17.
 *
 */
public abstract class AbstractKfbPushSender {
	
	private static Logger logger = LoggerFactory.getLogger(KfbPushSender.class);
	
//	public void send(DB_TARGET db, KfbPushMessage kfbPushMessage, Class<? extends GcmSender> gcmSenderClass) {
//		sendGcm(db, kfbPushMessage, gcmSenderClass);
//		sendIos(db, kfbPushMessage);
//	}
	
	public void sendIos(DB_TARGET db, KfbPushMessage kfbPushMessage) {
		// TODO: ios push notification
		List<PushDevice> gcmDevices = kfbPushMessage.getPushDevices(DEVICE_TYPE.IOS);
		
		logger.info("PUSH_UNIQUE_KEY[{}] DEVICE[{}] MSG[{}] SIZE[{}] NOT_IMPLEMENTED_YET", kfbPushMessage.getPushUniqueKey(), DEVICE_TYPE.IOS, kfbPushMessage.getMsg(), gcmDevices.size());
		
		// not implemented yet for IOS. update for fail cnt
		final int pushSchedulerKey = Integer.parseInt(kfbPushMessage.getPushUniqueKey());
		final int success = 0;
		final int fail = gcmDevices.size();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = PushSqlSession.openSession(db);
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			pushMapper.updatePushSchedulerIosResult(pushSchedulerKey, success, fail);
			sqlSession.commit();
			
			pushMapper.updatePushSchedulerIosEndDt(pushSchedulerKey, (int)(System.currentTimeMillis() / 1000));
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("PUSH_SCHEDULER PUSH_SCHEDULER_KEY[{}]", pushSchedulerKey, e);
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
			}
		}
	}
	
	public void sendGcm(DB_TARGET db, KfbPushMessage kfbPushMessage, Class<? extends GcmSender> gcmSenderClass) {
		final ExecutorService GCM_SENDER_THREAD_POOL = Executors.newFixedThreadPool(5);
		final int GCM_REG_ID_LIMIT = 1000;
		
		List<PushDevice> gcmDevices = kfbPushMessage.getPushDevices(DEVICE_TYPE.AND);
		
		// get valid regids from pushDevice
		List<String> gcmRegIds = new ArrayList<String>(gcmDevices.size());
		for (PushDevice pushDevice : gcmDevices) {
			final String regId = pushDevice.getToken();
			if (regId.trim().length() < 1) {
				logger.error("PUSH_UNIQUE_KEY[{}] DEVICE[{}] WRONG_TOKEN[{}]", kfbPushMessage.getPushUniqueKey(), DEVICE_TYPE.AND, regId);
				continue;
			}
			
			gcmRegIds.add(regId);
		}
		
		logger.info("PUSH_UNIQUE_KEY[{}] DEVICE[{}] MSG[{}] SIZE[{}]", kfbPushMessage.getPushUniqueKey(), DEVICE_TYPE.AND, kfbPushMessage.getMsg(), gcmRegIds.size());
		
		if (gcmRegIds.isEmpty()) {
			return;
		}
		
		final int threadCount = ((gcmRegIds.size() - 1) / GCM_REG_ID_LIMIT) + 1;
		for (int i=0; i<threadCount; i++) {
			final List<String> gcmRegIdsInThread = gcmRegIds.subList(i*GCM_REG_ID_LIMIT, Math.min(gcmRegIds.size(), (i+1)*GCM_REG_ID_LIMIT));
			
			try {
				GcmSender gcmSender = gcmSenderClass.newInstance();
				gcmSender.setDb(db);
				gcmSender.setTitle(kfbPushMessage.getTitle());
				gcmSender.setMsg(kfbPushMessage.getMsg());
				gcmSender.setGcmRegIds(gcmRegIdsInThread);
				gcmSender.setPushUniqueKey(kfbPushMessage.getPushUniqueKey());
				
				GCM_SENDER_THREAD_POOL.submit(gcmSender);
			} catch (Exception e) {
				logger.debug(this.toString(), e);
			}
		}
		
		try {
			Thread.sleep(1000*1);
		} catch (Exception e) {}
		GCM_SENDER_THREAD_POOL.shutdown();
	}

	public final static void testAndroidPush(String appId, DEVICE_TYPE deviceType, String regId, String msg) {
		KfbPushMessage kfbPushMessage = new KfbPushMessage();
		kfbPushMessage.setMsg(msg);
		
		PushDevice testPushDevice = new PushDevice();
		testPushDevice.setAppId(appId);
		testPushDevice.setDeviceType(deviceType.name());
		testPushDevice.setToken(regId);
		
		kfbPushMessage.addPushDevice(testPushDevice);
		
		// create android push sender
		Sender androidPushSender = new Sender(DebugOption.GOOGLE_API_KEY);
		
		// create push message for android
		Message androidPushMessage = new Builder().addData("msg", kfbPushMessage.getMsg()).build();
		
		// send android push
		List<PushDevice> androidPushDevices = kfbPushMessage.getPushDevices(DEVICE_TYPE.AND);
		for (PushDevice pushDevice : androidPushDevices) {
			try {
				logger.debug("[PUSH] DEVICE[{}] MSG[{}], APPID[{}] TOKEN[{}]", DEVICE_TYPE.AND.name(), kfbPushMessage.getMsg(), pushDevice.getAppId(), pushDevice.getToken());
				Result androidPushResult = androidPushSender.send(androidPushMessage, pushDevice.getToken(), DebugOption.ANDROID_PUSH_RETRIES);
				if (androidPushResult.getMessageId() != null) {
					logger.debug("[SENT] DEVICE[{}] SUCCESS[{}]", DEVICE_TYPE.AND.name(), androidPushResult.toString());
				}
				else {
					logger.debug("[SENT] DEVICE[{}] FAILED[{}]", DEVICE_TYPE.AND.name(), androidPushResult.toString());
				}
			} catch (IOException e) {
				logger.debug("[EXCEPTION] DEVICE[{}]", DEVICE_TYPE.AND.name(), e);
			}
		}
		
	}
	
}
