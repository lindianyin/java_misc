/**
 * 
 */
package com.nfl.kfb.rank;

import java.util.List;

import org.apache.ibatis.annotations.Select;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.ehcache.annotations.Cacheable;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.logs.LastRankLog;
import com.nfl.kfb.mapper.rank.GateRank;
import com.nfl.kfb.mapper.rank.Grank;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 *
 */
public interface RankService {
	
//	@TriggersRemove(cacheName="thisWeekTotalRank", removeAll=true, when=When.BEFORE_METHOD_INVOCATION)
	public void flushTotalRank(int week, String appId);
		
	/**
	 * 전체랭킹을 조회
	 * @param week
	 * @param appId		조회하는 앱ID
	 * @param appIds	친구 앱ID
	 * @return
	 */
//	@Cacheable(cacheName="thisWeekTotalRank")
	public List<Rank> getCachedTotalRank(int week, String appId, String[] appIds);
	
	public List<Rank> getTotalRank(int week, String appId, String[] appIds);

	@Transactional
	/**
	 * 랭크 보상 시각 insert<br>
	 * @param dateUtil
	 * @param account
	 * @param week
	 * @param rankOrder		등수
	 * @param chId
	 * @param lastRankLog
	 * @param lastWeekRewardResponse	ITID, ITCNT를 입력하기 위해서
	 * @param shopVer
	 */
	public void rewardLastWeekRank(DateUtil dateUtil, Account account, int week, int rankOrder, int chId
			, LastRankLog lastRankLog, LastWeekRewardResponse lastWeekRewardResponse, int shopVer) throws Exception;

	public boolean hasRankReward(String appId, int week);

	public List<GateRank> getGateRank(String appId, int week);

	public int getRownumOfGlobalRank(String appId);

	@Cacheable(cacheName = "globalRankListCache")
	public List<Grank> getGlobalRank(int idex);

	List<GateRank> getGateRankOfweek(String[] appIds, int week, int gate);

	public List<Grank> getTopTen();

	List<Rank> getAllRank();

	//List<GateRank> getGateRankOfweek(int week, int gate, String appId);
	
	List<Rank> getAllUlimitRank();
	
	
	
}
