/**
 * 
 */
package com.nfl.kfb.friend;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 
 * @author KimSeongsu
 * @since 2013. 9. 11.
 *
 */
public class FriendInvListResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public FriendInvListResponse(ReturnCode rc) {
		super(rc);
		
		put("F", new LinkedList<Map<String,Object>>());		// 빈 F 추가
	}

	/**
	 * 초대 친구 데이터
	 * @param appId
	 * @param invDt
	 */
	public void addF(String appId, int invDt) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> invList = (List<Map<String, Object>>) get("F");
		
		Map<String, Object> InvData = new HashMap<String, Object>();
		InvData.put("FID", appId);
		InvData.put("IDT", invDt);
		
		invList.add(InvData);
	}
	
}
