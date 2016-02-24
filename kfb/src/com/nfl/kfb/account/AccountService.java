/**
 * 
 */
package com.nfl.kfb.account;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.dungeon.Dungeon;
import com.nfl.kfb.mapper.gamble.GambleProb;
import com.nfl.kfb.mapper.inv.Inv;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.AttendanceLog;
import com.nfl.kfb.mapper.logging.logs.GambleLog;
import com.nfl.kfb.mapper.logging.logs.LoginLog;
import com.nfl.kfb.mapper.logging.logs.ReviewLog;
import com.nfl.kfb.mapper.logging.logs.TutorialLog;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 *
 */
public interface AccountService {
	
	Account getAccount(String appId);

	@Transactional
	/**
	 * 새로운 계정 생성
	 * @param appId
	 * @param sessionKey
	 * @param dateUtil
	 * @param appType
	 * @param appVer
	 * @param ccode
	 */
	Account createAccount(String appId, int sessionKey, DateUtil dateUtil, String appType, String appVer, String ccode,String img) throws Exception;

	void loginAccount(DateUtil dateUtil, Account account, String appType, String appVer, String ccode, LoginLog loginLog);
	
	void setPushOption(String appId, boolean pushOption);
	
	int[][] getDefaultInvenItem();

	/**
	 * 출석 계산
	 * @param lastAttendanceEpoch
	 * @param dateUtil
	 * @return					출석 보상이 필요한가
	 */
	boolean isAttendance(int lastAttendanceEpoch, DateUtil dateUtil);

	@Transactional
	/**
	 * 출석 보상<br>
	 * @param account
	 * @param chId
	 * @param dateUtil
	 * @param attendanceLog
	 * @param gamblePoint
	 * @param shopVer
	 * @throws Exception 
	 */
	void rewardAttendance(Account account, int chId, DateUtil dateUtil, AttendanceLog attendanceLog, int gamblePoint, int shopVer) throws Exception;

	/**
	 * 완료한 주간업적 리스트 요청<br>
	 * @param appId
	 * @param week
	 * @return
	 */
	List<Integer> getAchieveList(String appId, int week);

	@Transactional
	/**
	 * 주간업적 보상 지급<br>
	 * @param daetUtil
	 * @param account
	 * @param week
	 * @param achieveIdx
	 * @param chId
	 * @param rewardItem
	 * @param rewardValue
	 * @param achieveLog
	 * @throws DuplicateKeyException
	 */
	AchieveRewardResponse rewardAchieve(DateUtil dateUtil, Account account, int week, int achieveIdx, int chId, Shop reward1Item, int reward1Value, AchieveLog achieveLog) throws DuplicateKeyException, Exception;

	List<Inven> getInven(String appId);

	Map<String, Object> getInvenInfoMap(List<Inven> allInven);
	
	Map<String, Object> getInvenInfoMap(int[][] invenItemList);

	@Transactional
	TutorialResponse rewardTutorial(DateUtil dateUtil, Account account, Shop item, int itemCnt, TutorialLog tutorialLog);
	
	@Transactional
	ReviewResponse rewardReview(DateUtil dateUtil, Account account, Shop item, int itemCnt, ReviewLog reviewLog);

	KakaoTokenResponse kakaoTokenCheck(String access_token, String user_id, String sdkver) throws ClientProtocolException, IOException, URISyntaxException;

	Inv getInv(String appId);

	/**
	 * 탈퇴. DB에서 유저 삭제<br>
	 * @param dateUtil
	 * @param appId
	 */
	void withdraw(DateUtil dateUtil, String appId);

	void registerPushToken(String appId, String loginType, String loginVer, String ccode, String pushToken, int nowEpoch);

	/**
	 * 로그인시에 푸시토큰 관련값들을 update
	 * @param account
	 */
	void updatePushToken(Account account);

	boolean isBlockAccount(DateUtil dateUtil, String appId);
	
	@Transactional
	/**
	 * 뽑기
	 * @param dateUtil
	 * @param account
	 * @param chId
	 * @param gambleProbList
	 * @param gambleLog
	 * @param shopVer
	 * @return
	 */
	GambleResponse gamble(DateUtil dateUtil, Account account, int chId, List<GambleProb> gambleProbList, GambleLog gambleLog, int shopVer);

	int getGamblePoint(String appId);

	Dungeon getDungeon(String appId);

	void insertDungeon(Dungeon dungeon);

	void updateDungeon(Dungeon dungeon);

	List<Account> getAccountList(int count);

	List<Account> getAccountOfRecommand(String Appid, int count);
	
	boolean changeNickname(String nickname,String appId);
	
}
