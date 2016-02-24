/**
 * 
 */
package com.nfl.kfb.mapper.mail;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
public interface MailMapper {

	/**
	 * 아직 삭제되지 않은 메일들<br>
	 * @param appId
	 * @param delDt		삭제될 날짜
	 * @param limit		가져올 개수
	 * @return
	 */
	List<Mail> selectMailBox(@Param("owner") String appId, @Param("delDt") int delDt, @Param("limit") int limit);
	
	int countMailBox(@Param("owner") String appId, @Param("delDt") int delDt);
	
	/**
	 * 메일 select<br>
	 * @param appId
	 * @param mailKey
	 * @return
	 */
	Mail selectMail(@Param("owner") String appId, @Param("mailKey") int mailKey);

//	/**
//	 * 메일 보냄<br>
//	 * 받는 사람(fAppId)이 소유자(owner)가 된다<br>
//	 * @param appId		보내는 사람
//	 * @param fAppId	받는 사람
//	 * @param item
//	 * @param count
//	 * @param delDt
//	 * @param msg
//	 * @return inserted mailKey
//	 */
//	int insertMail(@Param("sender") String appId, @Param("owner") String fAppId
//			, @Param("item") int item, @Param("cnt") int count, @Param("delDt") int delDt, @Param("msg") String msg);
	
	int insertMail(Mail mail);

	/**
	 * 메일 삭제<br>
	 * @param mailKey
	 */
	void removeMail(@Param("mailKey") int mailKey);

	
	
	
	/**
	 * 주먹을 마지막으로 선물한 시각(epoch)<br>
	 * @param appId
	 * @param fAppId
	 * @return
	 */
	Integer selectPunchRegDt(@Param("appId") String appId, @Param("fAppId") String fAppId);

	/**
	 * 주먹 기록 insert<br>
	 * @param appId
	 * @param fAppId
	 * @param regDt
	 */
	void replacePunch(@Param("appId") String appId, @Param("fAppId") String fAppId, @Param("regDt") int regDt);

	/**
	 * 오래된 메일 삭제<br>
	 * @param nowEpoch
	 */
	void deleteOldMail(@Param("delDt") int delDt);

	void deleteAllMail(@Param("appId") String appId);

	void deleteAllPunch(@Param("appId") String appId);
	
	/**
	 * regDt가 regDtLimit 보다 큰 Punch 를 select <br>
	 * 쿨타임이 지나지않은 Punch를 얻어오기 위함<br>
	 * @param appId
	 * @param regDtLimit
	 * @return
	 */
	List<Punch> selectPunchList(@Param("appId") String appId, @Param("regDtLimit") int regDtLimit);
	
	void deleteOldPunch(@Param("regDt") int regDt);

}
