/**
 * 
 */
package com.nfl.kfb.friend;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 7. 3.
 *
 */
public class FriendInvResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public FriendInvResponse(ReturnCode rc) {
		super(rc);
	}
	
	public void setNOW(int nowEpoch) {
		put("NOW", nowEpoch);
	}
	
	public void setINVCNT(int invCnt) {
		put("INVCNT", invCnt);
	}

	public void setINVRIDX(int rIdx) {
		put("INVRIDX", rIdx);
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

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}

}
