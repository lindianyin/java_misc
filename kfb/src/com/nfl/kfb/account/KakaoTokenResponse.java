/**
 * 
 */
package com.nfl.kfb.account;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 8. 6.
 *
 */
public class KakaoTokenResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public KakaoTokenResponse(ReturnCode rc) {
		super(rc);
		
	}

	public void setSTATUS(int status) {
		put("STATUS", status);
	}
	
}
