package com.nfl.kfb.tcp;

import java.util.HashMap;
import java.util.Set;

import org.apache.mina.core.session.IoSession;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SessionMgr {
	private HashMap<String, IoSession> sessionList = new HashMap<>();
	private static SessionMgr sessionMgr = null;

	private static Object lock = new Object();

	private SessionMgr() {

	}

	public IoSession getSession(String appId) {
		return sessionList.get(appId);
	}

	public void addSession(String appId, IoSession ioSession) {
		synchronized (lock) {
			IoSession session = sessionList.get(appId);
			String _appId = null;
			int _arenaIdx = -1;
			if (session != null) {
				if(ioSession.getId() == session.getId()){
					return;
				}
				
				Object attribute = session.getAttribute(Arena.APP_ID);
				if(attribute != null){
					_appId = (String)attribute;
				}
				Object attribute1 = session.getAttribute(Arena.AREA_IDX);
				if(attribute1 != null){
					_arenaIdx = (int)attribute1;
				}
				session.close(true);
				sessionList.remove(appId);
			}
			if(_appId != null){
				ioSession.setAttribute(Arena.APP_ID,appId);
			}
			if(_arenaIdx != -1){
				ioSession.setAttribute(Arena.AREA_IDX,_arenaIdx);
			}
			sessionList.put(appId, ioSession);
		}

	}

	public void removeSession(String appId) {
		synchronized (lock) {
			sessionList.remove(appId);
		}
	}

	public static SessionMgr getInstance() {
		synchronized (lock) {
			if (sessionMgr == null) {
				sessionMgr = new SessionMgr();
			}
			return sessionMgr;
		}

	}
	
	public void SendMsgToAll(CMD cmd,Object[] params){
		synchronized (lock) {
			Set<String> keySet = sessionList.keySet();
			for(String key : keySet){
				sendMsg(key,cmd,params);
			}
		}
	}
	

	public void sendMsg(String appId, CMD cmd, Object[] params) {
		synchronized (lock) {
			IoSession session = sessionList.get(appId);
			if (session == null) {
				return;
			}
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				String result = objectMapper.writeValueAsString(params);
				System.out.println(session.getId()+"########Sendmessage"+"appId:"+appId+" cmd "+cmd+" send:" + result);
				byte[] result1 = result.getBytes("UTF-8");
				byte[] btTag_arr = new byte[4];
				byte[] btPackId_arr = new byte[4];
				byte[] btCmd_arr = ByteUtil.Integer2ByteArray(cmd.getValue());
				byte[] retResult = new byte[4 + 4 + 4 + result1.length];
				System.arraycopy(btTag_arr, 0, retResult, 0, 4);
				System.arraycopy(btPackId_arr, 0, retResult, 4, 4);
				System.arraycopy(btCmd_arr, 0, retResult, 8, 4);
				System.arraycopy(result1, 0, retResult, 12, result1.length);
				session.write(retResult);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
