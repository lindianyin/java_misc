/**
 * 
 */
package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 8. 13.
 *
 */
public class FinishPlayLog extends GameLog {
	
	public FinishPlayLog(DateUtil dateUtil, String appId) {
		super(dateUtil, appId, GAMELOG_TYPE.FINISH_PLAY);
	}

	public void setGate(int playGate) {
//		setReserved0(playGate);				// reserv0에 플레이한 관문을 기록
		// 수정함
		setItemCnt(playGate);		// 아이템개수 자리에 관문번호를 저장
	}

	public void setPlayTime(int playTime) {
		setReserved0(playTime);				// reserv0에 플레이시간(초) 기록
	}
	
	public void setPoint(int point) {
		setReserved1(point);				// reserv1에 점수 기록
	}

}
