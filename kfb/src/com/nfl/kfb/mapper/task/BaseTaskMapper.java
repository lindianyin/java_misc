/**
 * 
 */
package com.nfl.kfb.mapper.task;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 * 
 */
public interface BaseTaskMapper {
	
	List<BaseTask> selectBaseTask();
	BaseTask selectBaseTaskByTaskId(int taskId);
	
	List<RoleTask> selectRoleTaskByAppId(@Param("appId") String appId);
	
	List<RoleTask> selectRoleTaskByAppId1(@Param("appId") String appId);
	
	
	void insertRoleTask(RoleTask roleTask);
	
	void updateRoleTask(RoleTask roleTask);
	
	void updateMorethanRoleTask(RoleTask roleTask);
	
	RoleTask selectRoleTaskById(@Param("id") long id,@Param("appId") String appId);
	
	void deleteRoleTaskByIdAndAppId(@Param("id") String id,@Param("appId") String appId);
	
	void deleteTaskByTaskId(@Param("taskIds") Collection<Integer> taskIds);
	
	
	//List<RoleTask> selectAllRoleTaskByAppId(@Param("appId") String appId);
	
	int countOfRoleTask(@Param("appId") String appId,@Param("taskId") int taskId);
	
	@Select("SELECT * from basetask \n" +
			"WHERE taskId not in \n" +
			"	(SELECT taskId from roletask WHERE appId = #{appId}) AND taskCategory = 3;")
	List<BaseTask> selectActivityBaseTask(@Param("appId") String appId);
	
	@Select("SELECT IFNULL(MAX(id),1) from arena_result")
	long getMaxArenaId();
	
	@Insert("INSERT INTO arena_result(id,appId,point,date,appId1,point1,date1,appIdState,appId1State,idx,winAppId,reward) VALUES (#{id},#{appId},#{point},#{date},#{appId1},#{point1},#{date1},#{appIdState},#{appId1State},#{idx},#{winAppId},#{reward})")
	void insertAreanResult(ArenaResult arenaResult);
	
	//查询结果
	@Select("SELECT * from arena_result where id = #{id}")
	ArenaResult selectArenaResult(@Param("id") long id);
	
	
	@Select("SELECT * FROM\n" +
			"(SELECT * FROM arena_result WHERE idx=#{idx} and appId = #{appId} UNION \n" +
			"SELECT * FROM arena_result WHERE idx=#{idx} and appId1 = #{appId}) as b ORDER BY id DESC LIMIT 0,3")
	List<ArenaResult> selectThreeArenaResult(@Param("appId") String appId,@Param("idx") int idx);
	
	
	@Select("SELECT * FROM\n" +
			"(SELECT * FROM arena_result WHERE appId = #{appId}  UNION \n" +
			"SELECT * FROM arena_result WHERE appId1 = #{appId} ) as b WHERE appIdState IS  NULL or appId1State IS  NULL")
	List<ArenaResult> selectWorldFightArenaList(@Param("appId") String appId);
	
	@Update("UPDATE arena_result SET appIdState = #{appId} WHERE id = #{id}")
	void updateArenaList(@Param("appId") String appId,@Param("id") long id);
	
	@Update("UPDATE arena_result SET appId1State = #{appId} WHERE id = #{id}")
	void updateArenaList1(@Param("appId") String appId,@Param("id") long id);
	
	
	
	
	//无尽模式中不适应穿越的最高分
	@Insert("INSERT INTO fight_activity_rank (appId, point, date) VALUES (#{appId}, #{newPoint}, NOW())\n" +
			"ON DUPLICATE KEY UPDATE point = IF(point < #{newPoint},#{newPoint},point),date = NOW()")
	void insertOrUpdateFightActivityRank(@Param("appId") String appId,@Param("newPoint") int newPoint);
	
	//无尽模式中最高分
	@Insert("INSERT INTO fight_activity_rank_1 (appId, point, date) VALUES (#{appId}, #{newPoint}, NOW())\n" +
			"ON DUPLICATE KEY UPDATE point = IF(point < #{newPoint},#{newPoint},point),date = NOW()")
	void insertOrUpdateFightActivityRank_1(@Param("appId") String appId,@Param("newPoint") int newPoint);
	
	
	
	
	
	
	
	
	
}
