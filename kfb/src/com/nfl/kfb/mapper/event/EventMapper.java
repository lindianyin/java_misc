/**
 * 
 */
package com.nfl.kfb.mapper.event;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author KimSeongsu
 * @since 2013. 10. 14.
 *
 */
public interface EventMapper {
	@Select("SELECT * FROM EVENTS WHERE END_DT >= #{nowEpoch}")
	List<Event> selectEvents(@Param("nowEpoch") int nowEpoch);

	@Select("SELECT * FROM EVENTS WHERE START_DT > #{nowEpoch} || END_DT < #{nowEpoch}")
	List<Event> selectOverTimeEvents(@Param("nowEpoch") int nowEpoch);
	
	
	
	@Insert("INSERT INTO event_log (\n" +
			"	APPID,\n" +
			"	EVENT_KEY,\n" +
			"	EPOCH,\n" +
			"	MAIL_KEY,\n" +
			"	ITEM_ID,\n" +
			"	ITEM_CNT\n" +
			")\n" +
			"VALUES\n" +
			"	(\n" +
			"		#{appId}\n" +
			"		,\n" +
			"		#{eventKey}\n" +
			"		,\n" +
			"		#{epoch}\n" +
			"		,\n" +
			"		#{mailKey}\n" +
			"		,\n" +
			"		#{itemId}\n" +
			"		,\n" +
			"		#{itemCnt}\n" +
			"	)")
	int insertEventLog(@Param("appId") String appId, @Param("eventKey") int eventKey, @Param("epoch") int epoch
			, @Param("mailKey") int mailKey, @Param("itemId") int itemId, @Param("itemCnt") int itemCnt);
	
	@Select("SELECT EPOCH FROM event_log WHERE APPID = #{appId} AND EVENT_KEY = #{eventKey} ORDER BY EPOCH DESC LIMIT 0, 1")
	Integer selectEventLogLastEpoch(@Param("appId") String appId, @Param("eventKey") int eventKey);

}
