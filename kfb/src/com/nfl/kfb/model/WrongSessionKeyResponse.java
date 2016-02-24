/**
 * 
 */
package com.nfl.kfb.model;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public class WrongSessionKeyResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public WrongSessionKeyResponse() {
		super(ReturnCode.WRONG_SESSION_KEY);
		setException(new Exception("wrong sessionKey"));
	}
	
}
