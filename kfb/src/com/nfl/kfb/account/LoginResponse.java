/**
 * 
 */
package com.nfl.kfb.account;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public class LoginResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public LoginResponse(ReturnCode rc) {
		super(rc);
		
		put("G", new LinkedList<Map<String,Object>>());		// 빈 G 추가
	}

	/**
	 * 엽전
	 * @param gd
	 */
	public void setGD(int gd) {
		put("GD", gd);
	}
	
	/**
	 * 여의주
	 * @param bl
	 */
	public void setBL(int bl) {
		put("BL", bl);
	}
	
	/**
	 * WEEK 주차
	 * @param week
	 */
	public void setWK(int week) {
		put("WK", week);
	}
	
	/**
	 * 세션키
	 * @param sessionKey
	 */
	public void setSK(int sessionKey) {
		put("SK", sessionKey);
	}

	/**
	 * 다음주 시작까지 남은 시간
	 * @param weekRemainEpoch
	 */
	public void setWKDT(int weekRemainEpoch) {
		put("WKDT", weekRemainEpoch);
	}

	/**
	 * 주먹 개수<br>
	 * @param punch
	 */
	public void setPN(int punch) {
		put("PN", punch);
	}
	
	/**
	 * 주먹 사용 시각 - 리젠될경우 기준시각<br>
	 * @param punch
	 */
	public void setPNDT(int punchDt) {
		put("PNDT", punchDt);
	}

	public void setNOW(int nowEpoch) {
		put("NOW", nowEpoch);
	}
	
	public void setNickname(String nickname){
		put("NICKNAME",nickname);
	}
	
	public void setImg(String img){
		put("IMGURL",img);
	}
	
	public void addG(int gate, int point, int rwDt) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> totalGate = (List<Map<String, Object>>) get("G");
		
		Map<String, Object> gateData = new HashMap<String, Object>();
		gateData.put("GN", gate);
		gateData.put("PT", point);
		gateData.put("RW", rwDt != 0);
		
		totalGate.add(gateData);
	}
	
	public void addINV(int cnt, Set<Integer> rewardIdx, String invCntInfo, String invItemInfo) {
		HashMap<String, Object> invMap = new HashMap<String, Object>();
		invMap.put("CNT", cnt);
		invMap.put("RIDX", rewardIdx);
		invMap.put("INFO", invCntInfo);	// 초대수 정보 e.g. "5,10,20,30"
		invMap.put("ITINFO", invItemInfo);	// 초대보상 아이템 정보 e.g. "514,5,200,3000,100,5,701,1"
		
		put("INV", invMap);
	}

	public void setVR(boolean isNeedUpdate) {
		put("VR", isNeedUpdate);
	}

	public void setATRW(boolean isAttendance) {
		put("ATRW", isAttendance);
	}

	public void setTUTO(int tuto) {
		put("TUTO", tuto);
	}

	public void setPNABLE(boolean ableSendPunch) {
		put("PNABLE", ableSendPunch);
	}

	public void addInvenItemMap(Map<String, Object> invenInfoMap) {
		putAll(invenInfoMap);
	}

	public void setREV(boolean rev) {
		put("REV", rev);
	}

	public void setMAILCNT(int mailBoxCount) {
		put("MAILCNT", mailBoxCount);
	}

	public void setPUSH(boolean push) {
		put("PUSH", push);
	}
	
	public void setGP(int gamblePoint) {
		put("GP", gamblePoint);
	}

	public void setDungeon(int dungeonLimit, int dungeonCnt, int dungeonNextDt, int punch) {
		put("DLIMIT", dungeonLimit);
		put("DCNT", dungeonCnt);
		put("DNEXTDT", dungeonNextDt);
		put("DPUNCH", punch);
	}

	public void setSHOPVER(int latestShopVer) {
		put("SHOPVER", latestShopVer);
	}
	
	public void setTAGS(String tags){
		put("TAGS",tags);
	}
	
	public void setOpenArena(boolean isOpen){
		put("ARENA_OPEN",isOpen);
	}
	
}
