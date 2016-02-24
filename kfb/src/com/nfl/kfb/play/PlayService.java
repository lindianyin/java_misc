/**
 * 
 */
package com.nfl.kfb.play;

import java.util.Collection;

import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 6. 27.
 * 
 */
public interface PlayService {

	@Transactional
	/**
	 * 게임 시작<br>
	 * @param dateUtil
	 * @param account
	 * @param playWeek
	 * @param playGate
	 * @param chId
	 * @param chLv
	 * @param petId
	 * @param availableGP
	 * @param reqPunch
	 */
	StartGameResponse startPlay(DateUtil dateUtil, Account account,
			int playWeek, int playGate, int chId, int chLv, int petId,
			boolean availableGP, int reqPunch);

	@Transactional
	/**
	 * 게임결과를 적용<br>
	 * @param dateUtil
	 * @param account
	 * @param gold			획득한 엽전
	 * @param ball			획득한 여의주
	 * @param point			획득 점수
	 * @param missionRw		미션 클리어 보상 요청
	 * @param fAppIds
	 * @param useItemIds	사용한 아이템ID list
	 * @param useItemCnts	사용한 아이템 개수 list
	 * @param randomInvCnt	랜덤 친구초대 부활 회수
	 * @param gameLogs
	 * @param availableGP
	 * @param shopVer
	 * @return
	 */
	FinishGameResponse finishPlay(DateUtil dateUtil, Account account, int gold,
			int ball, int point, boolean missionRw, String[] fAppIds,
			Integer[] useItemIds, Integer[] useItemCnts, int randomInvCnt,
			Collection<GameLog> gameLogs, boolean availableGP, int shopVer,int playGate);

	BoastResponse boast(DateUtil dateUtil, Account account, String fID);

	@Transactional
	/**
	 * 던전 시작
	 * @param dateUtil
	 * @param account
	 * @param chId
	 * @param chLv
	 * @param petId
	 * @return
	 */
	StartDungeonResponse startDungeon(DateUtil dateUtil, Account account,
			int chId, int chLv, int petId);

	@Transactional
	/**
	 * 던전 끝
	 * @param dateUtil
	 * @param account
	 * @param gold
	 * @param ball
	 * @param useItemIds
	 * @param useItemCnts
	 * @param gameLogs
	 * @param dungeonLimit
	 * @param dungeonPunch
	 * @param shopVer
	 * @return
	 */
	FinishDungeonResponse finishDungeon(DateUtil dateUtil, Account account,
			int gold, int ball, Integer[] useItemIds, Integer[] useItemCnts,
			Collection<GameLog> gameLogs, int dungeonLimit, int dungeonPunch,
			int shopVer);

//	StartDungeonResponse startUnlimit(DateUtil dateUtil, Account account,
//			int chId, int chLv, int petId);
//
//	FinishDungeonResponse finishUnlimit(DateUtil dateUtil, Account account,
//			int gold, int ball, Integer[] useItemIds, Integer[] useItemCnts,
//			Collection<GameLog> gameLogs, int dungeonLimit, int dungeonPunch,
//			int shopVer);

}
