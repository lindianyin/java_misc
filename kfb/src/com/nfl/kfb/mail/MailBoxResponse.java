/**
 * 
 */
package com.nfl.kfb.mail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * 메시지함 리스트 요청<br>
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
public class MailBoxResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public MailBoxResponse(ReturnCode rc) {
		super(rc);
		
		put("M", new LinkedList<Map<String,Object>>());		// 빈 M 추가
	}

	public void addM(int mailKey, String fAppId, int type, int cnt, int delDt, String text, boolean isRrireq,String title) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> mailList = (List<Map<String, Object>>) get("M");
		
		Map<String, Object> mailData = new HashMap<String, Object>();
		mailData.put("KEY", mailKey);
		mailData.put("FID", fAppId);
		if (text == null) {
			mailData.put("TXT", "");
		}
		else {
			mailData.put("TXT", text);
		}
		mailData.put("ITID", type);
		mailData.put("CNT", cnt);
		mailData.put("DT", delDt);
		mailData.put("ISFRIREQMAIL", isRrireq);
		mailData.put("TITLE", title);
		mailList.add(mailData);
	}
	
}
