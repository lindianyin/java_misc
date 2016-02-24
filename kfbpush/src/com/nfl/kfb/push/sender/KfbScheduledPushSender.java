/**
 * 
 */
package com.nfl.kfb.push.sender;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.MulticastResult;
import com.nfl.kfb.push.collector.KfbPushMessage;
import com.nfl.kfb.push.collector.kfbPushDeviceCollector;
import com.nfl.kfb.push.dao.PushSqlSession;
import com.nfl.kfb.push.dao.PushSqlSession.DB_TARGET;
import com.nfl.kfb.push.mapper.PushDevice;
import com.nfl.kfb.push.mapper.PushMapper;
import com.nfl.kfb.push.mapper.PushScheduler;

/**
 * @author KimSeongsu
 * @since 2013. 10. 17.
 *
 */
public class KfbScheduledPushSender extends AbstractKfbPushSender {
	
	private static Logger logger = LoggerFactory.getLogger(KfbScheduledPushSender.class);
	
	public static void main(String[] args) {
//		// debug
//		System.out.println("args.length = " + args.length);
//		for (int i=0; i<args.length; i++) {
//			System.out.println("args[" + i + "] = " + args[i]);
//		}
		
		if (args.length != 1) {
			System.out.println("Usage : KfbScheduledPushSender [db(REAL/TEST)]");
			System.exit(0);
		}
		
		logger.info("PUSH_SCHEDULER NOW[{}]", new Date());
		
		final DB_TARGET db = DB_TARGET.valueOf(args[0]);
		
		List<PushScheduler> pushSchedulers = null;
		
		{
			int nowEpoch = (int)(System.currentTimeMillis() / 1000);
			SqlSession sqlSession = null;
			try {
				sqlSession = PushSqlSession.openSession(db);
				PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
				pushSchedulers = pushMapper.selectPushSchedulers(nowEpoch - (60*60*1), nowEpoch + 60);		// 1시간전 ~ 현재시간(+1분)에 예약된 푸시 
				sqlSession.commit();
			} catch (Exception e) {
				logger.error("Fail to get PushScheduler list from db", e);
			} finally {
				try {
					sqlSession.close();
				} catch (Exception e) {
				}
			}
		}
		
		logger.info("PUSH_SCHEDULER SIZE[{}]", pushSchedulers==null? 0 : pushSchedulers.size());
		
		if (pushSchedulers == null || pushSchedulers.isEmpty()) {
			System.exit(0);
		}
		
		for (PushScheduler pushScheduler : pushSchedulers) {
			logger.info("PUSH_SCHEDULER NOW[{}] PUSH_SCHEDULER_KEY[{}] PUSH_DT[{}] TITLE[{}] MSG[{}] COLLECTOR_CLASS[{}]", new Date() 
					, pushScheduler.getPushSchedulerKey(), new Date(1000L * pushScheduler.getPushDt())
					, pushScheduler.getPushTitle(), pushScheduler.getPushMsg(), pushScheduler.getCollectorClass() );
			
			SqlSession sqlSession = null;
			PushMapper pushMapper;
			try {
				sqlSession = PushSqlSession.openSession(db);
				pushMapper = sqlSession.getMapper(PushMapper.class);
				pushMapper.updatePushSchedulerDt(pushScheduler.getPushSchedulerKey(), (int)(System.currentTimeMillis() / 1000));		// 스케쥴러가 실행되었음을 기록
				sqlSession.commit();
				sqlSession.close();
				sqlSession = null;
				
				@SuppressWarnings("unchecked")
				Class<kfbPushDeviceCollector> collectorClass = (Class<kfbPushDeviceCollector>) Class.forName(pushScheduler.getCollectorClass());
				kfbPushDeviceCollector deviceCollector = collectorClass.newInstance();
				
				List<PushDevice> pushDevices = deviceCollector.collectPushDevices(db);
				
				logger.info("PUSH_SCHEDULER PUSH_UNIQUE_KEY[{}] COLLECTED_DEVICES[{}]", String.valueOf(pushScheduler.getPushSchedulerKey()), pushDevices.size());
				
				sqlSession = PushSqlSession.openSession(db);
				pushMapper = sqlSession.getMapper(PushMapper.class);
				pushMapper.updatePushSchedulerCollectedUsers(pushScheduler.getPushSchedulerKey(), pushDevices.size());		// 대상 유저수
				sqlSession.commit();
				sqlSession.close();
				sqlSession = null;
				
				KfbPushMessage kfbPushMessage = new KfbPushMessage();
				kfbPushMessage.setPushUniqueKey(String.valueOf(pushScheduler.getPushSchedulerKey()));
				kfbPushMessage.setTitle(pushScheduler.getPushTitle());
				kfbPushMessage.setMsg(pushScheduler.getPushMsg());
				kfbPushMessage.addPushDevice(pushDevices);
				
				KfbScheduledPushSender sender = new KfbScheduledPushSender();
				
				// send GCM
				sender.sendGcm(db, kfbPushMessage, ScheduledGcmSender.class);
				
				// send IOS
				sender.sendIos(db, kfbPushMessage);
				
			} catch (Exception e) {
				logger.error("PUSH_SCHEDULER", e);
			} finally {
				if (sqlSession != null) {
					try {
						sqlSession.close();
					} catch (Exception e) {}
				}
			}
		}
		
	}
	
	
	@Override
	public void sendGcm(DB_TARGET db, KfbPushMessage kfbPushMessage, Class<? extends GcmSender> gcmSenderClass) {
		SqlSession sqlSession = null;
		final int pushSchedulerKey = Integer.parseInt(kfbPushMessage.getPushUniqueKey());
		
		// update ANDROID startDt
		try {
			sqlSession = PushSqlSession.openSession(db);
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			pushMapper.updatePushSchedulerAndroidStartDt(pushSchedulerKey, (int)(System.currentTimeMillis() / 1000));		// 안드로이드 실행시각
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("PUSH_SCHEDULER PUSH_SCHEDULER_KEY[{}]", pushSchedulerKey, e);
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
			}
			sqlSession = null;
		}
		
		super.sendGcm(db, kfbPushMessage, gcmSenderClass);
		
		// update ANDROID endDt
		try {
			sqlSession = PushSqlSession.openSession(db);
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			pushMapper.updatePushSchedulerAndroidEndDt(pushSchedulerKey, (int)(System.currentTimeMillis() / 1000));
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("PUSH_SCHEDULER PUSH_SCHEDULER_KEY[{}]", pushSchedulerKey, e);
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
			}
			sqlSession = null;
		}
	}
	
	@Override
	public void sendIos(DB_TARGET db, KfbPushMessage kfbPushMessage) {
		SqlSession sqlSession = null;
		final int pushSchedulerKey = Integer.parseInt(kfbPushMessage.getPushUniqueKey());
		
		// update IOS startDt
		try {
			sqlSession = PushSqlSession.openSession(db);
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			pushMapper.updatePushSchedulerIosStartDt(pushSchedulerKey, (int)(System.currentTimeMillis() / 1000));		// IOS 실행시각
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("PUSH_SCHEDULER PUSH_SCHEDULER_KEY[{}]", pushSchedulerKey, e);
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
			}
			sqlSession = null;
		}
		
		super.sendIos(db, kfbPushMessage);
		
		// update IOS result
		try {
			sqlSession = PushSqlSession.openSession(db);
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			pushMapper.updatePushSchedulerIosEndDt(pushSchedulerKey, (int)(System.currentTimeMillis() / 1000));		// IOS 종료시각
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("PUSH_SCHEDULER PUSH_SCHEDULER_KEY[{}]", pushSchedulerKey, e);
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
			}
			sqlSession = null;
		}
	}
	
}

class ScheduledGcmSender extends GcmSender {
	
	private static Logger logger = LoggerFactory.getLogger(ScheduledGcmSender.class);
	
	@Override
	public MulticastResult call() throws Exception {
		MulticastResult multicastResult = super.call();
		
		if (multicastResult == null)
			return multicastResult;
		
		final int pushSchedulerKey = Integer.parseInt(getPushUniqueKey());
		final int success = multicastResult.getSuccess();
		final int fail = multicastResult.getFailure();
		
		SqlSession sqlSession = null;
		try {
			sqlSession = PushSqlSession.openSession(getDb());
			PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
			pushMapper.updatePushSchedulerAndroidResult(pushSchedulerKey, success, fail);
			sqlSession.commit();
			
			pushMapper.updatePushSchedulerAndroidEndDt(pushSchedulerKey, (int)(System.currentTimeMillis() / 1000));
			sqlSession.commit();
		} catch (Exception e) {
			logger.error("PUSH_SCHEDULER PUSH_SCHEDULER_KEY[{}]", pushSchedulerKey, e);
		} finally {
			try {
				sqlSession.close();
			} catch (Exception e) {
			}
		}
		
		return multicastResult;
	}
}
