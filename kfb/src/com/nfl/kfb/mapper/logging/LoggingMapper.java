/**
 * 
 */
package com.nfl.kfb.mapper.logging;

import java.util.Collection;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author KimSeongsu
 * @since 2013. 7. 29.
 *
 */
public interface LoggingMapper {

	public void insertGameLog(GameLog gameLog);
	
	public void insertGameLogs(Collection<GameLog> gameLog);
	
	public void insertCurrencyLog(CurrencyLog currencyLog);

	public void insertCurrncyErrLog(@Param("now") long now, @Param("appId") String appId, @Param("errMsg") String errMsg);
	
	
	public GameLog selectGameLog(@Param("appId") String appId, @Param("log_type") int log_type);
	
	@Select("SELECT COUNT(*) as count\n" +
            "FROM\n" +
            "(SELECT * from game_log WHERE APPID = #{appId} AND LOG_TYPE = #{log_type} AND DATEDIFF(FROM_UNIXTIME(EPOCH),NOW()) = 0) as b")
	public int countOfTodayResetDaiyTask(@Param("appId") String appId, @Param("log_type") int log_type);
	
	
	@Select("SELECT COUNT(*) as count \n" +
			"FROM\n" +
			"(SELECT * from game_log \n" +
			"WHERE appid = #{appId} and LOG_TYPE = #{log_type} \n" +
			"and WEEKOFYEAR(FROM_UNIXTIME(EPOCH)) = WEEKOFYEAR(NOW())\n" +
			"AND YEAR(FROM_UNIXTIME(EPOCH)) = YEAR(NOW())) as b;")
	public int countOfThisWeekResetWeekAchivev(@Param("appId") String appId, @Param("log_type") int log_type);
	
	
}
