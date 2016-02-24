/**
 * 
 */
package com.nfl.kfb.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.mapper.week.WeekAchieve;
import com.nfl.kfb.mapper.week.WeekMission;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.week.WeekDataResponse;

/**
 * WEEK 데이터 리소스<br>
 * WeekResourceService에 의해 캐시데이터로 관리됨<br>
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public class WeekResource {
	
	private final Map<Integer, WeekMission> weekMissions = new HashMap<Integer, WeekMission>();
	private final Map<Integer, WeekAchieve> weekAchieves = new HashMap<Integer, WeekAchieve>();
	private final WeekDataResponse weekDataResponse;		// cache

	public WeekResource(List<WeekMission> weekMissions, List<WeekAchieve> weekAchieves) {
		for (WeekMission weekMission : weekMissions) {
			this.weekMissions.put(new Integer(weekMission.getGate()), weekMission);
		}
		for (WeekAchieve weekAchieve : weekAchieves) {
			this.weekAchieves.put(new Integer(weekAchieve.getAidx()), weekAchieve);
		}
		
		weekDataResponse = makeWeekDataResponse();
	}
	
	private WeekDataResponse makeWeekDataResponse() {
		WeekDataResponse weekDataResponse = new WeekDataResponse(ReturnCode.SUCCESS);
		
		for (WeekMission weekMission : weekMissions.values()) {
			weekDataResponse.addMS(weekMission.getGate()
					, weekMission.getMission1Id(), weekMission.getMission1Value()
					, weekMission.getMission2Id(), weekMission.getMission2Value()
					, weekMission.getReward1ItemId(), weekMission.getReward1ItemCnt()
					, weekMission.getReward2ItemId(), weekMission.getReward2ItemCnt()
					, weekMission.getPunch());
		}
		
		for (WeekAchieve weekAchieve : weekAchieves.values()) {
			weekDataResponse.addAC(weekAchieve.getAidx(), weekAchieve.getAchTId()
					, weekAchieve.getAchId(), weekAchieve.getAchValue()
					, weekAchieve.getRewardItemId(), weekAchieve.getRewardItemCnt());
		}
		
		return weekDataResponse;
	}
	
	public WeekDataResponse getCachedWeekDataResponse() {
		return weekDataResponse;
	}

//	public int getWeek() {
//		return week;
//	}

	/**
	 * 이번주의 주건업적이 맞는가
	 * @param achieveIdx
	 * @return
	 */
	public boolean hasAchieve(int achieveIdx) {
		return weekAchieves.containsKey(achieveIdx);
	}
	
	public WeekAchieve getWeekAchieve(int achieveIdx) {
		return weekAchieves.get(achieveIdx);
	}

	public WeekMission getWeekMission(int gateIdx) {
		return weekMissions.get(gateIdx);
	}
	
}
