/**
 * 
 */
package com.nfl.kfb.mapper.dungeon;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 11. 13.
 *
 */
public interface DungeonMapper {
	
	Dungeon selectDungeon(@Param("appId") String appId);

	void insertDungeon(Dungeon dungeon);

	void updateDungeon(Dungeon dungeon);

}
