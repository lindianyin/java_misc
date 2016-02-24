/**
 * 
 */
package com.nfl.kfb.mapper.rank;

import java.util.Collection;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 * 
 */
public interface RankMapper {

	/**
	 * GATE_RANK에서 특정 gate를 제외한 sum(point)값을 구함
	 * 
	 * @param appId
	 * @param week
	 * @param gate
	 * @return
	 */
	public long sumGateRankPoint(@Param("appId") String appId,
			@Param("week") int week, @Param("gate") int gate);

	public void insertGateRank(@Param("appId") String appId,
			@Param("week") int week, @Param("gate") int gate,
			@Param("point") int point, @Param("rwDt") int rwDt,
			@Param("star") int star);

	public void updateGateRank(@Param("appId") String appId,
			@Param("week") int week, @Param("gate") int gate,
			@Param("point") int point, @Param("rwDt") int rwDt,
			@Param("star") int star);

	/**
	 * 게임이 끝났을때<br>
	 * RANK insert on duplicate key update<br>
	 * 
	 * @param appId
	 * @param week
	 * @param gate
	 * @param point
	 * @param rewardDt
	 *            // rewardDt default value
	 */
	public void insertOrUpdateRankPoint(@Param("appId") String appId,
			@Param("week") int week, @Param("gate") int gate,
			@Param("point") long point, @Param("rewardDt") int rewardDt);

	public List<Rank> selectUnsordedRank(@Param("week") int week,
			@Param("appIds") Collection<String> appIds);

	public List<GateRank> selectUnsordedGateRank(@Param("week") int week,
			@Param("gate") int gate, @Param("appIds") Collection<String> appIds);

	public Integer selectRankRewardDt(@Param("appId") String appId,
			@Param("week") int week);

	public void updateRankRewardDt(@Param("appId") String appId,
			@Param("week") int week, @Param("rewardDt") int rewardDt);

	public List<GateRank> selectGateRankList(@Param("appId") String appId,
			@Param("week") int week);

	public GateRank selectGateRank(@Param("appId") String appId,
			@Param("week") int week, @Param("gate") int gate);

	public int countGateAllClearFriends(@Param("appId") String appId,
			@Param("week") int week, @Param("point") long point,
			@Param("appIds") Collection<String> appIds,
			@Param("lastGate") int lastGate);

	public void removeGateRank(@Param("appId") String appId,
			@Param("week") int week);

	public void removeRank(@Param("appId") String appId, @Param("week") int week);
	
	public void removeRankByAppId(@Param("appId") String appId);
	

	public List<Grank> selectGolobalRankList(@Param("idx") int idx);
	
	public List<Grank> selectGolobalRankListLessThan10();
	
	

	public int rownumOfGolobalRank(@Param("appid") String appid);


	public GateRank selectAGateRank(@Param("week") int week,
			@Param("gate") int gate, @Param("appId") String appId);

	public List<GateRank> selectGateRankListGate(@Param("gate") int gate,
			@Param("week") int week, @Param("point") int point,
			@Param("limit") int limit);
	
	@Select("select * from rank where week = #{week}")
	public List<Rank> selectAllRank(@Param("week") int wk);
	
	@Select("SELECT * from unlimit_rank")
	public List<Rank> selectAllUnlimitRank();
	
	
	
	@Insert("INSERT INTO unlimit_rank (\n" +
			"	APPID,\n" +
			"	WEEK,\n" +
			"	GATE,\n" +
			"	POINT,\n" +
			"	REWARD_DT\n" +
			")\n" +
			"VALUES\n" +
			"	(#{appId}, 0, 0, 0, 0) ON DUPLICATE KEY UPDATE POINT =  #{point}")
	public void insertOrUpdateUnlimitRank(@Param("appId") String appId, @Param("point") long point );
	
	
	
	@Insert("INSERT INTO rank (\n" +
			"	APPID,\n" +
			"	WEEK,\n" +
			"	GATE,\n" +
			"	POINT,\n" +
			"	REWARD_DT\n" +
			")\n" +
			"VALUES\n" +
			"	(#{appId}, #{week}, 0, 0, 0) ON DUPLICATE KEY UPDATE POINT =  #{point}")
	public void insertOrUpdateRank(@Param("appId") String appId, @Param("week") int week,@Param("point") long point );
	
	

}
