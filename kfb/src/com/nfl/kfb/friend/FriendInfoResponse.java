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
 * @since 2013. 7. 5.
 *
 */
public class FriendInfoResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public FriendInfoResponse(ReturnCode rc) {
		super(rc);
		
		put("F", new LinkedList<Map<String,Object>>());		// 빈 F 추가
	}

	public void addF(String appId, int chId, int chLv, int petId, boolean push, int punchCoolTime,String nickname
			,String imgurl,int pk,long sc,boolean isFriend,boolean isReq) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> invList = (List<Map<String, Object>>) get("F");
		
		Map<String, Object> InvData = new HashMap<String, Object>();
		InvData.put("FID", appId);
		InvData.put("CHID", chId);
		InvData.put("CLV", chLv);
		InvData.put("PID", petId);
		InvData.put("PUSH", push);
		InvData.put("PNDT", punchCoolTime);
		InvData.put("NICKNAME",nickname);
		InvData.put("IMGURL", imgurl);
		InvData.put("PK", pk);
		InvData.put("SC", sc);
		InvData.put("ISFRI", isFriend);
		InvData.put("ISREQ",isReq);
		invList.add(InvData);
	}
	
}
