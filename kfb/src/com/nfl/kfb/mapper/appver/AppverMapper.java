/**
 * 
 */
package com.nfl.kfb.mapper.appver;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 7. 30.
 *
 */
public interface AppverMapper {
	
	Appver selectAppver(@Param("app") String app, @Param("ver") String ver);

}
