/**
 * 
 */
package com.nfl.kfb.model;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.kfb.account.AccountController;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.tcp.ArenaMgr;
import com.nfl.kfb.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 6. 11.
 *
 */
public class JsonResponse extends HashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(JsonResponse.class);
	

	
	public JsonResponse(ReturnCode rc) {
		super();
		setRC(rc);
	}
	
	public final void setRC(ReturnCode rc) {
		super.put("RC", rc.getCode());
	}
	
	public String getException(Throwable t){
		StringBuilder sb = new StringBuilder();
	    for (StackTraceElement element : t.getStackTrace()) {
	    	sb.append("\n");
	        sb.append(element.toString());
	    }
		//super.put("EXCEPTION", sb.toString());
		return sb.toString();
	}
	
	
	public void setException(Throwable t) {
		String exception = getException(t);
		logger.error(exception);
		if(DebugOption.ENABLE_EXCEPTION_DB){
			ObjectMapper mapper = new ObjectMapper();
			String str = "EXCEPTION_MSG:"+t.getMessage()+"\n";
			try {
				str += mapper.writeValueAsString(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ArenaMgr.getInstance().getAccountMapper().insertServerException(exception,str);
		}
		
		if (DebugOption.ENABLE_EXCEPTION_MESSAGE) {
			put("EXCEPTION_MSG", t.getMessage());
			try {
				StringBuilder sb = new StringBuilder();
			    for (StackTraceElement element : t.getStackTrace()) {
			    	sb.append("\n");
			        sb.append(element.toString());
			    }
				super.put("EXCEPTION", sb.toString());
			} catch (Exception e) {}
		}
	}
}
