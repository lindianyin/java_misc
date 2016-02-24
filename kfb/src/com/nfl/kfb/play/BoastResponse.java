/**
 * 
 */
package com.nfl.kfb.play;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 11. 11.
 *
 */
public class BoastResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public BoastResponse(ReturnCode rc) {
		super(rc);
	}

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}
	
}
