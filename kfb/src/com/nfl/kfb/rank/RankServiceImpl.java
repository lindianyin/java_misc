/**
 * 
 */
package com.nfl.kfb.rank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.googlecode.ehcache.annotations.PartialCacheKey;
import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.logs.LastRankLog;
import com.nfl.kfb.mapper.rank.GateRank;
import com.nfl.kfb.mapper.rank.Grank;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 *
 */
@Service(value="RankServiceImpl")
public class RankServiceImpl extends AbstractKfbService implements RankService {
	
	private static final Logger logger = LoggerFactory.getLogger(RankServiceImpl.class);
	
	@Autowired
	private RankMapper rankMapper;
	
	@Autowired
	private AccountMapper accountMapper;
	
	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;
	
	@Autowired
	private InvenMapper invenMapper;
	
	@Override
//	@TriggersRemove(cacheName="thisWeekTotalRank", removeAll=true, when=When.BEFORE_METHOD_INVOCATION)
	public void flushTotalRank(@PartialCacheKey int week, @PartialCacheKey String appId) {
		logger.debug("Force flush totalRank. appId={}, week={}", appId, week);
	}
	
	@Override
//	@Cacheable(cacheName="thisWeekTotalRank")
	public List<Rank> getCachedTotalRank(@PartialCacheKey int week, @PartialCacheKey String appId, String[] appIds) {
		return getTotalRank(week, appId, appIds);
	}
		
	@Override
	public List<Rank> getTotalRank(int week, String appId, String[] appIds) {
		logger.debug("getTotalRank, week={}, appId={}", week, appId);
		
		// 전체랭크를 조회할 appId
		Set<String> appIdForRank = asFidSet(appIds);
		appIdForRank.add(appId);		// 나도 추가
		
		// 정렬없이 DB에서 select
		List<Rank> thisWeekRank = rankMapper.selectUnsordedRank(week, appIdForRank);
		
		// 점수순으로 정렬
		Collections.sort(thisWeekRank, new Comparator<Rank>() {
			@Override
			public int compare(Rank o1, Rank o2) {
				long diff = o2.getPoint() - o1.getPoint();
				return diff == 0? 0 : (diff > 0? 1 : -1);
			}
		});
		
		int rankOrder = 0;
		Rank prevRank = null;
		Iterator<Rank> iter = thisWeekRank.iterator();
		while (iter.hasNext()) {
			Rank rank = iter.next();
//			if (rank.getPoint() <= Rank.DEFAULT_POINT) {		// 0점은 랭킹리스트에서 빼버린다
//				iter.remove();
//				continue;
//			}
			
			if (prevRank == null) {
				prevRank = rank;
				rank.setRank(0);		// 니가 1등이다
				continue;
			}
			
			rankOrder++;
			
			if (prevRank.getPoint() == rank.getPoint()) {		// 점수가 같으면 같은 등수다
				rank.setRank(prevRank.getRank());
			}
			else {
				rank.setRank(rankOrder);		// 점수가 낮으면 자기보다 점수가 높은사람수 이다
			}
			
			prevRank = rank;
		}
		
		logger.debug("totalRank size={}", thisWeekRank.size());
		if (logger.isDebugEnabled()) {
			for (Rank totalRank : thisWeekRank) {
				logger.debug("appId={}, point={}, gate={}", totalRank.getAppId(), totalRank.getPoint(), totalRank.getGate());
			}
		}
		
		return thisWeekRank;
	}
	
	@Override
	public boolean hasRankReward(String appId, int week) {
		Integer rankRewardDt = rankMapper.selectRankRewardDt(appId, week);
		return rankRewardDt != null && rankRewardDt.intValue() > Rank.DEFAULT_REWARD_DT;
	}
	
	@Transactional
	@Override
	public void rewardLastWeekRank(DateUtil dateUtil, Account account, int week, int rankOrder, int chId
			, LastRankLog lastRankLog, LastWeekRewardResponse lastWeekRewardResponse, int shopVer) throws Exception {
		// 지난주 등수에 따라 보상 결정
		int[] rewardItemData = DebugOption.getLastWeekRankReward(rankOrder);
		int rewardItemId = rewardItemData[0];
		int rewardCnt = rewardItemData[1];
		
		Shop rewardItem = resourceService.getShop(shopVer, rewardItemId);
		
		rankMapper.updateRankRewardDt(account.getAppId(), week, dateUtil.getNowEpoch());
		
		if (isAccountItem(rewardItem.getItemId())) {
			addAccountItem(dateUtil, account, rewardItem, rewardCnt, lastRankLog);
			accountMapper.updateAccountItem(account);
			lastWeekRewardResponse.setITID(rewardItem.getItemId());
			lastWeekRewardResponse.setITCNT(rewardCnt);
		}
		else {
			throw new RuntimeException("The PastWeekRankReward must be gold or ball");
		}
	}
	
	@Override
	public List<GateRank> getGateRank(String appId, int week) {
		return rankMapper.selectGateRankList(appId, week);
	}
	
	
	
	
	
	@Override
	public int getRownumOfGlobalRank(String appId){
		return rankMapper.rownumOfGolobalRank(appId);
	}
	
	
	@Override
	public List<Grank> getGlobalRank(int idex) {
		
		return  rankMapper.selectGolobalRankList(idex);
	}

	@Override
	public List<GateRank> getGateRankOfweek(String[] appIds,int week,int gate) {
		List<String> asList = Arrays.asList(appIds);
		List<GateRank> selectUnsordedGateRank = rankMapper.selectUnsordedGateRank(week,gate, asList);
		return selectUnsordedGateRank;
	}
	
	@Override
	public List<Grank> getTopTen() {
		return rankMapper.selectGolobalRankListLessThan10();
	}

	@Override
	public List<Rank> getAllRank() {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		int wk  = dateUtil.getThisWeek();
		return rankMapper.selectAllRank(wk);
	}

	@Override
	public List<Rank> getAllUlimitRank() {
		List<Rank> unlimitRank = rankMapper.selectAllUnlimitRank();
		return unlimitRank;
	}

}
