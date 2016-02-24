/**
 * 
 */
package com.nfl.kfb.play;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public class StartGameResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public StartGameResponse(ReturnCode rc) {
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
	 * PLAY_KEY
	 * @param playKey
	 */
	public void setPK(int playKey) {
		super.put("PK", playKey);
	}
	
	public void setPN(int punch) {
		put("PN", punch);
	}

	/**
	 * 주먹 사용 시각
	 * @param punchDt
	 */
	public void setPNDT(int punchDt) {
		super.put("PNDT", punchDt);
	}

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}
	
}
