/**
 * 
 */
package com.nfl.kfb.play;

import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;


/**
 * 
 * @author KimSeongsu
 * @since 2013. 11. 13.
 *
 */
public class FinishDungeonResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public FinishDungeonResponse(ReturnCode rc) {
		super(rc);
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

	public void setDungeon(int dungeonLimit, int dungeonCnt, int dungeonNextDt, int punch) {
		put("DLIMIT", dungeonLimit);
		put("DCNT", dungeonCnt);
		put("DNEXTDT", dungeonNextDt);
		put("DPUNCH", punch);
	}
	
}
