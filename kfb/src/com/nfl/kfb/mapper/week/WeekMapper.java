/**
 * 
 */
package com.nfl.kfb.mapper.week;

import java.util.List;


/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public interface WeekMapper {
	
	public List<WeekMission> selectWeekMission(int week);
	
	public List<WeekAchieve> selectWeekAchieve(int week);
	
//	public int selectLastWeekData(int week);

}
