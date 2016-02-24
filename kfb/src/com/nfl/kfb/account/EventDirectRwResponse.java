/**
 * 
 */
package com.nfl.kfb.account;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 10. 23.
 *
 */
public class EventDirectRwResponse extends JsonResponse {
	
	private static final long serialVersionUID = 1L;
	
	public static final int EVENT_DIRECT_RW_SUCCESS = 0;
	public static final int EVENT_DIRECT_RW_DUPLICATE = 1;
	public static final int EVENT_DIRECT_RW_DENY = 2;
	public static final int EVENT_DIRECT_RW_EVENT_ENDED = 3;
	
	public EventDirectRwResponse(ReturnCode rc) {
		super(rc);
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
	
	public void setSuccess(int eventDirectRwSuccess) {
		put("SUCCESS", eventDirectRwSuccess);
	}
	
}