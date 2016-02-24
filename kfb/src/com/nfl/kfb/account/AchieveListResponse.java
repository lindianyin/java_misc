/**
 * 
 */
package com.nfl.kfb.account;

import java.util.List;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 7. 4.
 *
 */
public class AchieveListResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public AchieveListResponse(ReturnCode rc) {
		super(rc);
	}

	/**
	 * WEEK 주차
	 * @param week
	 */
	public void setWK(int week) {
		super.put("WK", week);
	}

	/**
	 * 내가 완료한 주간업적 리스트
	 * @param achieveList
	 */
	public void setAIDX(List<Integer> achieveList) {
		super.put("AIDX", achieveList);
	}

}
