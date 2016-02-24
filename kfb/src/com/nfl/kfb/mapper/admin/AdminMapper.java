/**
 * 
 */
package com.nfl.kfb.mapper.admin;

import org.apache.ibatis.annotations.Param;


/**
 * @author KimSeongsu
 * @since 2013. 9. 4.
 *
 */
public interface AdminMapper {
	
	AdminSubtract selectAdminSubtract(@Param("appId") String appId);
	
	void deleteAdminSubtract(@Param("appId") String appId);

}
