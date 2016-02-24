/**
 * 
 */
package com.nfl.kfb.account;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 11. 8.
 *
 */
public class GambleResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public GambleResponse(ReturnCode rc) {
		super(rc);
		
		put("MISS", new LinkedList<Map<String,Object>>());
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
	
	public void addMissItem(int itemId, int itemCnt) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> missItems = (List<Map<String, Object>>) get("MISS");
		
		Map<String, Object> missItem = new HashMap<String, Object>();
		missItem.put("ITID", itemId);
		missItem.put("ITCNT", itemCnt);
		
		missItems.add(missItem);
	}
	
	public void setGP(int gamblePoint) {
		put("GP", gamblePoint);
	}

}
