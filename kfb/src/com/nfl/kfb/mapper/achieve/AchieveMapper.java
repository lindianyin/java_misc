/**
 * 
 */
package com.nfl.kfb.mapper.achieve;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DuplicateKeyException;

/**
 * @author KimSeongsu
 * @since 2013. 7. 4.
 *
 */
public interface AchieveMapper {

	List<Integer> selectAchieve(@Param("appId") String appId, @Param("week") int week);

	void insertAchieve(@Param("appId") String appId, @Param("week") int week, @Param("achIdx") int achIdx, @Param("achDt") int achDt) throws DuplicateKeyException;

	void deleteAllAchieve(@Param("appId") String String, @Param("week") int week);

}
