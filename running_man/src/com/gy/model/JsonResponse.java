package com.gy.model;

import java.util.HashMap;

import com.nfl.kfb.util.DebugOption;

public class JsonResponse extends HashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;

	public JsonResponse(){
		super();
		setRC(ReturnCode.SUCCESS);
	}
	
	
	public JsonResponse(ReturnCode rc) {
		super();
		setRC(rc);
	}
	
	public final void setRC(ReturnCode rc) {
		super.put("RC", rc.getCode());
	}
	
	public void setException(Throwable t) {
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
