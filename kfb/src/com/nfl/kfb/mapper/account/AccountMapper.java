/**
 * 
 */
package com.nfl.kfb.mapper.account;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import com.nfl.kfb.account.Notice;
import com.nfl.kfb.mapper.task.RoleTask;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 *
 */
public interface AccountMapper {
	
	public Account selectAccount(@Param("appId") String appId);
	
	public void insertAccount(Account account);
	
	/**
	 * 리뷰 보상 지급<br>
	 * GOLD, BALL, PUNCH, PUNCH_DT, REV<br>
	 * @param account
	 */
	public void updateReviewRw(Account account);

	/**
	 * 튜토리얼 보상 지급<br>
	 * GOLD, BALL, PUNCH, PUNCH_DT, TUTO<br>
	 * @param account
	 */
	void updateTutorialRw(Account account);

	/**
	 * Account에 관련된 아이템이 추가되어서 update<br>
	 * GOLD, BALL, PUNCH, PUNCH_DT, ATT_CNT, ATT_DT<br>
	 * @param account
	 */
	public void updateAttendanceRw(Account account);
	
	/**
	 * Account에 관련된 아이템이 추가되어서 update<br>
	 * GOLD, BALL, PUNCH, PUNCH_DT<br>
	 * @param account
	 */
	public void updateAccountItem(Account account);
	
	/**
	 * 푸시 옵션 변경<br>
	 * @param appId
	 * @param push		0=false, 1=true
	 */
	public void updatePushOption(@Param("appId") String appId, @Param("push") int push);

	public List<FriendAccount> selectFriendAccount(@Param("appIds") Collection<String> appIds);

	public void updateLogin(Account account);

	public void updateStartPlay(Account account);

	/**
	 * GODL, BALL, PUNCH, PUNCH_DT, PLAY_KEY<br>
	 * @param account
	 */
	public void updateFinishPlay(Account account);

	public void deleteAccount(@Param("appId") String appId);

	public void insertOrUpdatePushToken(@Param("appId") String appId, @Param("token") String token, @Param("loginDt") int loginDt
			, @Param("loginTy") String loginTy, @Param("loginVr") String loginVr, @Param("ccode") String ccode);
	
	public void updatePushTokenLogin(@Param("appId") String appId, @Param("loginDt") int loginDt
			, @Param("loginTy") String loginTy, @Param("loginVr") String loginVr, @Param("ccode") String ccode);
	
	public void deletePushToken(@Param("appId") String appId);
	
	
	/**
	 * 지정된 시간에 블럭되어 있는지 여부<br>
	 * 개수를 리턴<br>
	 * @param appId
	 * @param epoch
	 * @return
	 */
	public int countBlockAccount(@Param("appId") String appId, @Param("epoch") int epoch);

	
	public List<Account> selectAccountList(@Param("number") int number);
	
	
	public List<Account> selectFriendlyRecommendList(@Param("fri_self") String fri_self,@Param("count") int count);
	
	
	public void updateNickname(@Param("nickname") String nickname,@Param("appId") String appId);
	
	public int countOfNickname(@Param("nickname") String nickname);
	
	public int updateTUTO(@Param("appId") String appId,@Param("tuto") int tuto);
	
	@Update("UPDATE account SET PLAY_GATE = #{play_gate} WHERE APPID = #{appId}")
	public int updatePlayGate(@Param("appId") String appId,@Param("play_gate") int play_gate);
	
	
	/*Month_card*/
	@Select("SELECT * FROM month_card WHERE appId=#{appId}  AND DATEDIFF(get_time,NOW()) =0")
	public Month_card selectTodayMonthCardByAppId(@Param("appId") String appId);
	
	@Insert("INSERT INTO month_card(id,appId,get_time,state ) VALUES (#{id},#{appId},#{get_time},#{state})")
	@SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=long.class)
	public int insertMonthCard(Month_card mc);
	
	@Delete("DELETE FROM month_card where appid=#{appId}")
	public int deleteMonthCardByAppid(@Param("appId") String appId);
	
	@Select("SELECT MAX(get_time) FROM month_card  WHERE appId = #{appId}")
	public Date selectMaxMonthCardDate(@Param("appId") String appId);
	
	@Update("UPDATE month_card SET state = 1  WHERE appId=#{appId} AND DATEDIFF(get_time,NOW()) =0")
	public int updateTodayMonthCardStateByAppid(@Param("appId") String appId);
	
	@Select("SELECT count(*) FROM month_card WHERE appId=#{appId} AND state = 0 AND DATEDIFF(get_time,NOW())  >= 0")
	public int countOfMonthCardNotGetReward(@Param("appId") String appId);
	
	
	
	@Update("UPDATE account SET TAGS = #{tags} WHERE APPID = #{appId}")
	public int updateAccountTags(@Param("tags") String tags,@Param("appId") String appId);
	
	
	
	/*BaseReward*/
	@Select("SELECT * FROM basereward where type = #{type}")
	public List<BaseReward> selectBaseRewardByType(@Param("type") int type);
	
	@Delete("DELETE FROM gamble_point_bak;")
	public void deleteGamblePoint();
	
	
	@Select("INSERT INTO gamble_point_bak SELECT * FROM gamble_point ORDER BY GAMBLE_POINT DESC;")
	public void snapshotGamblePoint();

	/*RoleTask*/
	@Insert("INSERT INTO rolereward(id,appId,type,gettime) VALUES (#{id},#{appId},#{type},#{gettime})")
	@SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
	public int insertRoleReward(RoleReward roleReward);
	
	//search today is get reward
	@Select("SELECT * from rolereward WHERE appId = #{appId} and type = #{type} AND DATEDIFF(gettime,NOW()) = 0")
	public RoleReward selectRoleReward(@Param("appId") String appId,@Param("type") int type);
	
	
	
	
	
	
	
	/*gamble_point_bak*/
	@Select("SELECT count(*) from gamble_point_bak \n" +
			"WHERE GAMBLE_POINT  >= (SELECT gamble_point  from gamble_point_bak WHERE appid = #{appId})")
	public int selectRankOfGamblePointBak(@Param("appId") String appId);
	
	
	
	
	@Update("UPDATE account SET IMG = #{img} WHERE APPID = #{appId}")
	public int updateAccountImg(@Param("img") String img,@Param("appId") String appId);
	
	@Select("SELECT APPID from account")
	public List<String> selectAppId();
	
	//无尽模式不使用穿越最高分
	@Select("SELECT a.APPID,a.NICKNAME as nickname ,a.IMG as img ,b.point as point FROM account  as a INNER JOIN\n" +
			"(SELECT * FROM fight_activity_rank ORDER BY point DESC LIMIT 0, 20) as b on a.APPID = b.appId")
	public List<FightActivityRankVo> selectFightActivityRank();
	
	@Select("SELECT a.APPID,a.NICKNAME as nickname ,a.IMG as img ,b.point as point FROM account  as a INNER JOIN\n" +
			"			(SELECT * FROM fight_activity_rank WHERE appId= #{appId}) as b on a.APPID = b.appId")
	public FightActivityRankVo selectSelfFightActivityRank(@Param("appId") String appId);
	
	
	//无尽模式最高分
	@Select("SELECT a.APPID,a.NICKNAME as nickname ,a.IMG as img ,b.point as point FROM account  as a INNER JOIN\n" +
			"(SELECT * FROM fight_activity_rank_1 ORDER BY point DESC LIMIT 0, 10) as b on a.APPID = b.appId")
	public List<FightActivityRankVo> selectFightActivityRank_1();
	
	@Select("SELECT a.APPID,a.NICKNAME as nickname ,a.IMG as img ,b.point as point FROM account  as a INNER JOIN\n" +
			"			(SELECT * FROM fight_activity_rank_1 WHERE appId= #{appId}) as b on a.APPID = b.appId")
	public FightActivityRankVo selectSelfFightActivityRank_1(@Param("appId") String appId);
	
	
	
	
	
	
	
	
	@Select("SELECT COUNT(*) FROM active_code WHERE code = #{code}")
	public int countOfActiveCode(@Param("code") String code);
	
	@Insert("INSERT INTO active_code (appId,code,date) VALUES (#{appId},#{code},NOW())")
	public int insertActiveCode(@Param("appId") String appId,@Param("code") String code);
	
	
	@Select("SELECT * FROM activity_board WHERE start_time < NOW() AND NOW() < end_time")
	public List<ActivityBoard> selectActivityBoard();
	
	@Insert("INSERT INTO client_info (mac, info,uid) VALUES (#{mac}, #{info},#{uid});")
	public void insertClientInfo(@Param("mac") String mac,@Param("info") String info,@Param("uid") String uid);
	
	@Insert("INSERT INTO server_exception (exception, type) VALUES (#{exception}, #{type})")
	public void insertServerException(@Param("exception") String exception,@Param("type") String type);
	
	@Insert("INSERT INTO account_channel (appId, channelId, registDate, loginDate, loginCount) VALUES (#{appId}, #{channelId}, NOW(), NOW(), 1)\n" +
			"ON DUPLICATE KEY UPDATE loginDate = NOW() , loginCount = loginCount+1")
	public void insertOrUpdateAccountChannel(@Param("appId") String appId,@Param("channelId") String channelId);
	
	@Select("SELECT * FROM admin_channel WHERE number = #{number}")
	public AdminChannel selectAdminChannel(@Param("number") String number);
	
	
	@Select("SELECT ITEM_ID FROM shop  WHERE ITEM_ID >= 400 AND INSTANT !=0 AND\n" +
			"			ITEM_ID NOT IN\n" +
			"			(SELECT ITEM_ID FROM inven WHERE APPID = #{appId})\n" +
			"			ORDER BY RAND() LIMIT 1;")
	public Integer getUnBuyItemId(@Param("appId") String appId);
	
	
	@Select("SELECT count(*) FROM rolereward WHERE type = #{type} AND appId = #{appId}")
	public int selectRoleRewardByTypeAndID(@Param("appId") String appId,@Param("type") int type);
	
	@Update("UPDATE rolereward SET type = #{type} WHERE appId = #{appId} AND type = 5")
	public void updateRoleReward(@Param("appId") String appId,@Param("type") int type);
	
	@Select("SELECT * FROM notice WHERE startDate <= NOW() AND  NOW() <= endDate")
	public List<Notice> selectNotice();
	
	@Insert("INSERT account_notice (appId,id,times) VALUE (#{appId},#{id},1)\n" +
			"ON DUPLICATE KEY UPDATE times = times +1;")
	public int insertOrUpdateAccountNotice(@Param("appId") String appId,@Param("id") int id);
	
	@Select("SELECT COUNT(*) FROM account_notice WHERE appId = #{appId} AND id = #{id}")
	public int countOfAccountNotice(@Param("appId") String appId,@Param("id") int id);
}
