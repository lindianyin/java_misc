/**
 * 
 */
package com.nfl.kfb.push.sender;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.nfl.kfb.push.dao.PushSqlSession;
import com.nfl.kfb.push.dao.PushSqlSession.DB_TARGET;
import com.nfl.kfb.push.mapper.PushDevice.DEVICE_TYPE;
import com.nfl.kfb.push.mapper.PushMapper;
import com.nfl.kfb.push.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 10. 17.
 *
 */
public class GcmSender implements Callable<MulticastResult> {
	
	private static Logger logger = LoggerFactory.getLogger(GcmSender.class);
	
	private DB_TARGET db;
	private String title;
	private String msg;
	private String pushUniqueKey;
	private List<String> gcmRegIds;
	
	public DB_TARGET getDb() {
		return db;
	}

	public void setDb(DB_TARGET db) {
		this.db = db;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getPushUniqueKey() {
		return pushUniqueKey;
	}

	public void setPushUniqueKey(String pushUniqueKey) {
		this.pushUniqueKey = pushUniqueKey;
	}

	public List<String> getGcmRegIds() {
		return gcmRegIds;
	}

	public void setGcmRegIds(List<String> gcmRegIds) {
		this.gcmRegIds = gcmRegIds;
	}

	@Override
	public MulticastResult call() throws Exception {
		MulticastResult multicastResult = null;
		
		try {
			// 안드로이드 GCM 푸시 발송
			// create android push sender
			Sender gcmPushSender = new Sender(DebugOption.GOOGLE_API_KEY);
			
			// create push message for android
			Message gcmMessage = new Builder()
										.collapseKey(pushUniqueKey)
										.addData("title", title)
										.addData("msg", msg)
										.build();
			
			multicastResult = gcmPushSender.send(gcmMessage, gcmRegIds, 2);
			
			List<Result> results = multicastResult.getResults();
			for (int i=0; i<gcmRegIds.size(); i++) {
				String regId = gcmRegIds.get(i);
				Result result = results.get(i);
				String messageId = result.getMessageId();
				
				if (messageId == null) {
					// fail
					String error = result.getErrorCodeName();
					logger.info("PUSH_UNIQUE_KEY[{}] DEVICE[{}] RESULT[FAIL] TOKEN[{}] GCMCODENAME[{}]", pushUniqueKey, DEVICE_TYPE.AND, regId, error);
					if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
						// application has been removed from device - unregister it
						SqlSession sqlSession = null;
						try {
							sqlSession = PushSqlSession.openSession(db);
							PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
							pushMapper.deleteToken(regId);
							sqlSession.commit();
						} catch (Exception e) {
							logger.error("PUSH_UNIQUE_KEY[{}] DEVICE[{}] RESULT[FAIL] TOKEN[{}] GCMCODENAME[{}]", pushUniqueKey, DEVICE_TYPE.AND, regId, error, e);
						} finally {
							try {
								sqlSession.close();
							} catch (Exception e) {
							}
						}
					}
				}
				else {
					// success
					String canonicalRegId = result.getCanonicalRegistrationId();
					
					if (canonicalRegId == null) {
						logger.info("PUSH_UNIQUE_KEY[{}] DEVICE[{}] RESULT[SUCCESS] TOKEN[{}]", pushUniqueKey, DEVICE_TYPE.AND, regId);
					}
					else {
						logger.info("PUSH_UNIQUE_KEY[{}] DEVICE[{}] RESULT[SUCCESS] TOKEN[{}] CAN_TOKEN[{}]", pushUniqueKey, DEVICE_TYPE.AND, regId, canonicalRegId);
						
						// same device has more than on registration id : update it
						SqlSession sqlSession = null;
						try {
							sqlSession = PushSqlSession.openSession(db);
							PushMapper pushMapper = sqlSession.getMapper(PushMapper.class);
							pushMapper.updateToken(regId, canonicalRegId);
							sqlSession.commit();
						} catch (Exception e) {
							logger.error("PUSH_UNIQUE_KEY[{}] DEVICE[{}] RESULT[SUCCESS] TOKEN[{}] CAN_TOKEN[{}]", pushUniqueKey, DEVICE_TYPE.AND, regId, canonicalRegId, e);
						} finally {
							try {
								sqlSession.close();
							} catch (Exception e) {
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(this.toString(), e);
		}
		
		return multicastResult;
	}
}
