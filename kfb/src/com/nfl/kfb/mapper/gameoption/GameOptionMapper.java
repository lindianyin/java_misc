/**
 * 
 */
package com.nfl.kfb.mapper.gameoption;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 11. 13.
 *
 */
public interface GameOptionMapper {

	String selectGameOption(@Param("optionKey") String optionKey);
	
}
