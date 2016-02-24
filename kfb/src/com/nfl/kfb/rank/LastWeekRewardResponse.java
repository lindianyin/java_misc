/**
 * 
 */
package com.nfl.kfb.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public class LastWeekRewardResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public LastWeekRewardResponse(ReturnCode rc) {
		super(rc);
		
		put("F", new LinkedList<Map<String,Object>>());		// 빈 F 추가
	}

	/**
	 * WEEK 주차
	 * @param week
	 */
	public void setWK(int week) {
		super.put("WK", week);
	}
	
	/**
	 * 랭크 데이터 추가
	 * @param appId
	 * @param rank
	 * @param point
	 * @param gate
	 */
	public void addF(String appId, int rank, long point, int gate) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> totalRank = (List<Map<String, Object>>) get("F");
		
		Map<String, Object> rankData = new HashMap<String, Object>();
		rankData.put("FID", appId);
		rankData.put("RK", rank);
		rankData.put("CPT", point);
		rankData.put("CGN", gate);
		
		totalRank.add(rankData);
	}
	
	public void setRTY(int rewardType) {
		super.put("RTY", rewardType);
	}
	
	public void setRVL(int rewardValue) {
		super.put("RVL", rewardValue);
	}

	public void setGOT(boolean got) {
		super.put("GOT", got);
	}

	public void setITID(int itemId) {
		put("ITID", itemId);
	}
	
	public void setITCNT(int itemCnt) {
		put("ITCNT", itemCnt);
	}

	public void setGD(int gold) {
		put("GD", gold);
	}
	
	public void setBL(int ball) {
		put("BL", ball);
	}
	
	public void setPN(int punch) {
		put("PN", punch);
	}
	
	public void setPNDT(int punchDt) {
		put("PNDT", punchDt);
	}

	public void setRW(int[][] lastWeekRankReward) {
		List<Map<String, Integer>> lastWeekRankRewardList = new ArrayList<Map<String,Integer>>();
		for (int i=0; i<lastWeekRankReward.length; i++) {
			int itemId = lastWeekRankReward[i][0];
			int itemCnt = lastWeekRankReward[i][1];
			
			Map<String, Integer> lastWeekRankRewardDataMap = new HashMap<String, Integer>();
			lastWeekRankRewardDataMap.put("IDX", i);
			lastWeekRankRewardDataMap.put("ITID", itemId);
			lastWeekRankRewardDataMap.put("ITCNT", itemCnt);
			
			lastWeekRankRewardList.add(lastWeekRankRewardDataMap);
		}
		put("RW", lastWeekRankRewardList);
	}
	
}
