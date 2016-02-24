/**
 * 
 */
package com.nfl.kfb.maintenance.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 10. 15.
 *
 */
public interface MaintenanceMapper {
	
	//////////////////////////////////////////
	// 2013. 10. 15. cheating user
	//////////////////////////////////////////
	List<String> selectCheatingAppId();
	int updateCheatingAppIdGoldBall(@Param("appId") String appId);

	
	//////////////////////////////////////////
	// 2013. 10. 22. send mail
	//////////////////////////////////////////
	void insertMail(Mail mail);
	
	void insertGameLog(GameLog gameLog);
	
	
	
	List<String> selectAllAppId();
	
}
