/**
 * 
 */
package com.nfl.kfb.mail;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 주먹 선물<br>
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
public class PunchResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	enum SUC {
		SUCCESS(0)			// 주먹선물 성공
		, COOL_TIME(1);		// 선물 쿨타임중
		
		private final int code;
		private SUC(int code) {
			this.code = code;
		}
		public int getCode() {
			return code;
		}
	}
	
	public PunchResponse(ReturnCode rc) {
		super(rc);
	}

	public void setSUC(SUC success) {
		put("SUC", success.getCode());
	}
	
	public void setDT(int dt) {
		put("DT", dt);
	}

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}
	
}
