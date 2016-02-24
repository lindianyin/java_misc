package com.nfl.kfb.tcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.session.IoSession;

import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.GameLog.GAMELOG_TYPE;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;

//竞技大厅业务逻辑
public class Arena {

	public static final String APP_ID = "appId";

	public static final String AREA_IDX = "areaIdx";

	private IoSession ioSession;

	private String appId;

	public Arena(IoSession ioSession) {
		this.ioSession = ioSession;
	}

	public String add(String a) {
		System.out.println("ioSession.getId():" + ioSession.getId());
		return a + a;
	}

	public HashMap<String, Object> enterArena(String appId) {
		HashMap<String, Object> retResult = new HashMap<String, Object>();
		SessionMgr.getInstance().addSession(appId, ioSession);
		ioSession.removeAttribute(APP_ID);
		ioSession.setAttribute(APP_ID, appId);
		this.appId = appId;
		// int[] countArr = ArenaMgr.getInstance().getCountOfPlayer();
		int[] countArr = ArenaMgr.getInstance().countOfThreeFightSuccss(appId);
		retResult.put("arenaCountArr", countArr);
		retResult.put("reward", DebugOption.arenaReward);
		return retResult;
	}

	public boolean leaveArena() {
		ArenaMgr.getInstance().removePlayer(appId);
		return true;
	}

	public void enterAreaaa(int idx) {
		int ret = ArenaMgr.getInstance().costGoldAndBall(idx, appId, false);
		if(ret != 0){
			return;
		}
		//costGoldAndBall(idx,appId);
		ArenaMgr.getInstance().enterSomeArea(appId, idx);
	}

	public Map<String, Boolean> submitPoint(long id, int point, boolean isOver,int idx) {
		// int idx = (int)ioSession.getAttribute(AREA_IDX);
		Map<String, Boolean> _map = ArenaMgr.getInstance().submitPoint(appId,
				id, isOver, point,idx);
		return _map;
	}

	public ArrayList<Player> getWorldFightList() throws Exception {
		ArrayList<Player> worldFightList = ArenaMgr.getInstance()
				.getWorldFightList(appId);
		
		System.out.println("世界对中列表:"+DebugOption.toJson(worldFightList));
		return worldFightList;
	}

}
