/**
 * 
 */
package com.nfl.kfb.mail;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 8. 14.
 *
 */
public class RecvMailResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public RecvMailResponse(ReturnCode rc) {
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
	
}
