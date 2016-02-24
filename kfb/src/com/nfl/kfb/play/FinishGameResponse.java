/**
 * 
 */
package com.nfl.kfb.play;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public class FinishGameResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	private static final String KEY_WK = "WK";		// WEEK

	public FinishGameResponse(ReturnCode rc) {
		super(rc);
		
		
	}

	/**
	 * WEEK 주차
	 * @param week
	 */
	public void setWK(int week) {
		super.put(KEY_WK, week);
	}

	public void setST(int star, int gold, int ball) {
		super.put("ST", star);
		super.put("SRWGD", gold);
		super.put("SRWBL", ball);
	}
	
//	public void setRW(boolean missionRw) {
//		put("RW", missionRw);
//	}

//	public void setSRW(boolean star1Reward, boolean star2Reward, boolean star3Reward) {
//		put("SRW1", star1Reward);
//		put("SRW2", star2Reward);
//		put("SRW3", star3Reward);
//	}

	public void setRWIT(int idx, int itemId, int itemCnt) {
		if (idx == 0) {
			put("RWIT0ID", itemId);		
			put("RWIT0CNT", itemCnt);		
		}
		else if (idx == 1) {
			put("RWIT1ID", itemId);		
			put("RWIT1CNT", itemCnt);		
		}
		else {
			throw new RuntimeException("Unknown mission reward idx");
		}
	}

//	public void setSRW(int starCount, int itemId, int itemCnt) {
//		if (starCount == 1) {
//			put("SRW1ID", itemId);		
//			put("SRW1CNT", itemCnt);
//		}
//		else if (starCount == 2) {
//			put("SRW2ID", itemId);		
//			put("SRW2CNT", itemCnt);
//		}
//		else if (starCount == 3) {
//			put("SRW3ID", itemId);		
//			put("SRW3CNT", itemCnt);
//		}
//		else {
//			throw new RuntimeException("Unknown starCount");
//		}
//	}

	public void setLASTGATE(int lastGateRank, int lastGateItemId, int lastGateItemCnt) {
		put("LASTGATE", lastGateRank);		
		put("GRWITID", lastGateItemId);		
		put("GRWITCNT", lastGateItemCnt);
	}
	
	public void setGD(int gold) {
		put("GD", gold);
	}
	
	public void setBL(int ball) {
		put("BL", ball);
	}
	
	public void setPN(int punch) {
		put("PN", punch);
	}
	
	public void setPNDT(int punchDt) {
		put("PNDT", punchDt);
	}

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}
	
}
