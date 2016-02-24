/**
 * 
 */
package com.nfl.kfb.week;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 8. 6.
 *
 */
public class WeekDataResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public WeekDataResponse(ReturnCode rc) {
		super(rc);
		
		put("MS", new LinkedList<Map<String,Object>>());		// 빈 MS 추가
		put("AC", new LinkedList<Map<String,Object>>());		// 빈 AC 추가
	}

	public void addMS(int gateNum, int mission1Id, int mission1Value, int mission2Id, int mission2Value
			, int reward1Id, int reward1Value, int reward2Id, int reward2Value, int punch) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> missionList = (List<Map<String, Object>>) get("MS");
		
		Map<String, Object> missionData = new HashMap<String, Object>();
		missionData.put("GN", gateNum);
		missionData.put("MID", mission1Id);
		missionData.put("VL", mission1Value);
		missionData.put("MID2", mission2Id);
		missionData.put("VL2", mission2Value);
		missionData.put("RWID", reward1Id);
		missionData.put("RWVL", reward1Value);
		missionData.put("RWID2", reward2Id);
		missionData.put("RWVL2", reward2Value);
		missionData.put("PUNCH", punch);
		
		missionList.add(missionData);
	}
	
	public void addAC(int idx, int tid, int achId, int achValue, int rewardId, int rewardValue) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> achieveList = (List<Map<String, Object>>) get("AC");
		
		Map<String, Object> achieveData = new HashMap<String, Object>();
		achieveData.put("AIDX", idx);
		achieveData.put("TID", tid);
		achieveData.put("AID", achId);
		achieveData.put("VL", achValue);
		achieveData.put("RWID", rewardId);
		achieveData.put("RWVL", rewardValue);
		
		
		
		
		achieveList.add(achieveData);
	}
	
}
