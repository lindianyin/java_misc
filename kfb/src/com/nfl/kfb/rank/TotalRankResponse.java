/**
 * 
 */
package com.nfl.kfb.rank;

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
public class TotalRankResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public TotalRankResponse(ReturnCode rc) {
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
	 * @param chId
	 * @param chLv
	 * @param petId
	 */
	public void addF(String appId, int rank, long point, int gate,String nickname,int chid
			,String imgurl,int lv) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> totalRank = (List<Map<String, Object>>) get("F");
		
		Map<String, Object> rankData = new HashMap<String, Object>();
		rankData.put("FID", appId);
		rankData.put("RK", rank);
		rankData.put("CPT", point);
		rankData.put("CGN", gate);
		rankData.put("NICKNAME", nickname);
		rankData.put("CHID", chid);
		rankData.put("IMGURL", imgurl);
		rankData.put("CLV", lv);
		totalRank.add(rankData);
	}
	
}
