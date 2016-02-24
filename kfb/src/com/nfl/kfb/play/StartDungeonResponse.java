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
public class StartDungeonResponse extends JsonResponse {

	private static final long serialVersionUID = 1L;
	
	public StartDungeonResponse(ReturnCode rc) {
		super(rc);
	}

	public void setPK(int playKey) {
		super.put("PK", playKey);
	}
	
	public void setPN(int punch) {
		put("PN", punch);
	}

	public void setPNDT(int punchDt) {
		super.put("PNDT", punchDt);
	}

	public void setADDGP(int gamblePoint) {
		put("ADDGP", gamblePoint);
	}
	
	public void setDungeon(int dungeonLimit, int dungeonCnt, int dungeonNextDt, int punch) {
		put("DLIMIT", dungeonLimit);
		put("DCNT", dungeonCnt);
		put("DNEXTDT", dungeonNextDt);
		put("DPUNCH", punch);
	}
	
}
