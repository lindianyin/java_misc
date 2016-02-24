/**
 * 
 */
package com.nfl.kfb.mapper.gamble;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 11. 7.
 *
 */
public interface GambleMapper {
	
	List<GambleProb> selectGambleProb();

	public int selectGamblePoint(@Param("appId") String appId);

	public void increaseGamblePoint(@Param("appId") String appId, @Param("gamblePoint") int gamblePoint);
	
}
