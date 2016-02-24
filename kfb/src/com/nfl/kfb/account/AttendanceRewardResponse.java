/**
 * 
 */
package com.nfl.kfb.account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 7. 18.
 *
 */
public class AttendanceRewardResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public AttendanceRewardResponse(ReturnCode rc) {
		super(rc);
	}

	public void setATDC(int attendanceDayCnt) {
		super.put("ATDC",attendanceDayCnt);
	}

	public void setATNRD(int nextAttendanceDt) {
		super.put("ATNRD", nextAttendanceDt);
	}

	public void setATRW(boolean reward) {
		put("ATRW", reward);
	}

	public void setAT(int[][] attendanceReward) {
		List<Map<String, Integer>> attendanceRwList = new ArrayList<Map<String,Integer>>();
		for (int i=0; i<attendanceReward.length; i++) {
			int itemId = attendanceReward[i][0];
			int itemCnt = attendanceReward[i][1];
			
			Map<String, Integer> attendanceDataMap = new HashMap<String, Integer>();
			attendanceDataMap.put("DAY", i);
			attendanceDataMap.put("ID", itemId);
			attendanceDataMap.put("CNT", itemCnt);
			
			attendanceRwList.add(attendanceDataMap);
		}
		put("AT", attendanceRwList);
	}

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}

}
