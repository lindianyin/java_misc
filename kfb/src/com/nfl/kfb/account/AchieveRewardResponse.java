/**
 * 
 */
package com.nfl.kfb.account;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 7. 4.
 *
 */
public class AchieveRewardResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public AchieveRewardResponse(ReturnCode rc) {
		super(rc);
	}

	/**
	 * WEEK 주차
	 * @param week
	 */
	public void setWK(int week) {
		super.put("WK", week);
	}

	public void setGOT(boolean youGotAlready) {
		super.put("GOT", youGotAlready);
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
	
}
