/**
 * 
 */
package com.nfl.kfb;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfl.kfb.account.SessionKeyService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.util.DebugOption;



/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 *
 */
public abstract class AbstractKfbController {
	
	@Autowired
	@Qualifier("SessionKeyServiceImpl")
	private SessionKeyService sessionKeyService;
	
	@ExceptionHandler(Exception.class)
	@ResponseBody
	public JsonResponse handleException(Exception ex, HttpServletRequest request) {
		JsonResponse jsonResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
		
		ex.printStackTrace();
		
		try {
			if (DebugOption.ENABLE_EXCEPTION_HANDLER_ERRMSG) {
				jsonResponse.setException(ex);
			}
		} catch (Exception e) {}
		
		return jsonResponse;
	}
	
//	/**
//	 * 클라에서 받은 세션키가 올바른지 캐시된 데이터(없으면 DB에서 다시 읽음)와 비교
//	 * @param appId
//	 * @param sessionKey
//	 * @return
//	 */
//	public boolean isValidSessionKey(String appId, int sessionKey) {
//		if (DebugOption.SKIP_SESSION_KEY)
//			return true;
//		
//		if (DebugOption.ENABLE_DEBUG_SESSION_KEY && (DebugOption.DEBUG_SESSION_KEY == sessionKey)) {
//			return true;		// this is valid
//		}
//		
//		if (sessionKey == 0)
//			return false;
//		
//		Integer cachedSessionKey = sessionKeyService.getCachedSessionKey(appId);
//		if (cachedSessionKey == null)
//			return false;
//		
//		// 캐시된 세션키와 클라가 보내준 세션키가 맞지않다
//		if (cachedSessionKey != sessionKey) {
//			// 캐시된 세션키를 비운다
//			sessionKeyService.flushCachedSessionKey(appId);
//			
//			// DB에서 다시 읽어본다
//			// 캐시에서 비웠으니, DB에서 읽어올 것이다
//			cachedSessionKey = sessionKeyService.getCachedSessionKey(appId);
//			
//			// DB에 새로 읽어온 세션값과도 맞지않으면, 잘못된 세션키를 보냈거나 오래된 세션키이다 
//			return cachedSessionKey != null && cachedSessionKey == sessionKey;
//		}
//		
//		return true;
//	}
	
	/**
	 * 세션키 검증. Account를 이미 DB에서 읽었을경우 유용함
	 * @param account
	 * @param sessionKey
	 * @return
	 */
	public boolean isValidSessionKey(Account account, int sessionKey) {
		
		//return true;//这里得改回来
		
		if (DebugOption.ENABLE_DEBUG_SESSION_KEY && (DebugOption.DEBUG_SESSION_KEY == sessionKey)) {
			return true;		// this is valid
		}
		
		if (sessionKey == Account.DEFAULT_SESSION_KEY)
			return false;
		
		return account.getSessionKey() == sessionKey;
	}
	
	public int generateNewSessionKeyForLogin(String appId) {
//		// 캐시되어 있는것이 있다면 비운다
//		sessionKeyService.flushCachedSessionKey(appId);
		
		// 새로운 세션키를 생성 & 캐시
		return sessionKeyService.generateRandomSessionKey(appId);
	}
	
}
