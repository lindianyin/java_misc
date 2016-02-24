/**
 * 
 */
package com.nfl.kfb.model;

import java.util.HashMap;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 9. 25.
 *
 */
public class AdPOPcornResponse extends HashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;
	
	public enum ADPOPCORN_RESULT_CODE {
		SUCCESS(true, 1, "success")
		, INVALID_ITEM_KEY(false, 19, "invalid item key")
		, INVALID_HASH_KEY(false, 1100, "invalid hash key")
		, DUPLICATE_TRANSACTION(false, 3100, "duplicate transaction")
		, INVALID_USER_KEY(false, 3200, "invalid user key")
		, UNDEFINED_ERROR(false, 4000, "UNDEFINED_ERROR")
		, PROTOCOL_ERROR(false, 9000, "protocol error")
		, PROTOCOL_INVALID_DATA_TYPE(false, 9100, "protocol invalid data type")
		;
		
		private final boolean result;
		private final int resultCode;
		private final String resultMsg;
		private ADPOPCORN_RESULT_CODE(boolean result, int resultCode, String resultMsg) {
			this.result = result;
			this.resultCode = resultCode;
			this.resultMsg = resultMsg;
		}
		
		public final boolean getResult() {
			return result;
		}
		
		public final int getResultCode() {
			return resultCode;
		}
		
		public final String getResultMsg() {
			return resultMsg;
		}
	}
	
	public AdPOPcornResponse(ADPOPCORN_RESULT_CODE resultCode) {
		this(resultCode.getResult(), resultCode.getResultCode(), resultCode.getResultMsg());
	}

	public AdPOPcornResponse(boolean result, int resultCode, String resultMsg) {
		super();
		put("Result", result);
		put("ResultCode", resultCode);
		put("ResultMsg", resultMsg);
	}

	
}
