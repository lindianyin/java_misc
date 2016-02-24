/**
 * 
 */
package com.nfl.kfb.push.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 10. 11.
 *
 */
public interface PushMapper {
	
	public List<PushDevice> selectAllTokens();

	public void deleteToken(@Param("token") String token);

	public void updateToken(@Param("oldToken") String oldToken, @Param("newToken") String newToken);

	public List<PushDevice> selectAppId(@Param("appId") String appId);
	
	/**
	 * 지정된 시간 사이의 아직 미발송된 PushScheduler를 찾음<br>
	 * @param startDt
	 * @param endDt
	 * @return
	 */
	public List<PushScheduler> selectPushSchedulers(@Param("startDt") int startDt, @Param("endDt") int endDt);
	
	/**
	 * 스케쥴러가 실행된 시각을 update<br>
	 * @param pushSchedulerKey
	 * @param schedulerDt
	 * @return
	 */
	public int updatePushSchedulerDt(@Param("pushSchedulerKey") int pushSchedulerKey, @Param("schedulerDt") int schedulerDt);
	
	public void updatePushSchedulerAndroidResult(@Param("pushSchedulerKey") int pushSchedulerKey
			, @Param("andSuccess") int andSuccess, @Param("andFail") int andFail);

	public void updatePushSchedulerIosResult(@Param("pushSchedulerKey") int pushSchedulerKey
			, @Param("iosSuccess") int iosSuccess, @Param("iosFail") int iosFail);
	
	public void updatePushSchedulerCollectedUsers(@Param("pushSchedulerKey") int pushSchedulerKey, @Param("collectedUsers") int collectedUsers);

	public void updatePushSchedulerAndroidStartDt(@Param("pushSchedulerKey") int pushSchedulerKey, @Param("andStartDt") int andStartDt);

	public void updatePushSchedulerIosStartDt(@Param("pushSchedulerKey") int pushSchedulerKey, @Param("iosStartDt") int iosStartDt);

	public void updatePushSchedulerAndroidEndDt(@Param("pushSchedulerKey") int pushSchedulerKey, @Param("andEndDt") int andEndDt);
	
	public void updatePushSchedulerIosEndDt(@Param("pushSchedulerKey") int pushSchedulerKey, @Param("iosEndDt") int iosEndDt);
	
}
