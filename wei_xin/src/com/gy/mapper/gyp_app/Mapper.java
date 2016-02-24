package com.gy.mapper.gyp_app;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import com.gy.model.Cfg_game;
import com.gy.model.User_account;
import com.gy.model.User_account_Cfg_game;
import com.gy.model.User_account_Cfg_game_VO;

public interface Mapper {
	@Select("SELECT COUNT(*) FROM cfg_game")
	public int countOf_cfg_game();

	@Select("SELECT od FROM cfg_game where id = #{id}")
	public int select_cfg_game_order(@Param("id") int id);

	@Insert("INSERT INTO user_account (id, openid, nickname, sex, province, city, country, headimgurl, gold) VALUES (#{id}, #{openid}, #{nickname}, #{sex}, #{province}, #{city}, #{country}, #{headimgurl}, #{gold})")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
	public int insert_user_account(User_account user_account);

	@Select("SELECT COUNT(*) FROM user_account WHERE openid = #{openid}")
	public int countOf_user_account_by_openid(@Param("openid") String openid);

	@Select("SELECT a.id,a.name,a.repute,b.name as memo,a.url,a.logo FROM  cfg_game a INNER JOIN cfg_game_type b on a.cfg_game_type_id = b.id")
	public List<Cfg_game> select_cfg_game_list();

	@Update("UPDATE cfg_game SET repute = repute + 1 WHERE id = #{id}")
	public void incrementGameRepute(@Param("id") int id);

	@Select("SELECT id FROM user_account WHERE openid = #{openid}")
	public Integer select_user_account(@Param("openid") String openid);

	@Update("UPDATE user_account SET nickname = #{nickname},headimgurl=#{headimgurl} WHERE openid = #{openid}")
	public int update_user_account(@Param("nickname") String nickname,
			@Param("headimgurl") String headimgurl,
			@Param("openid") String openid);

	@Insert("INSERT INTO user_account_cfg_game (\n"
			+ "	user_account_id,\n"
			+ "	cfg_game_id,\n"
			+ "	score\n"
			+ ")\n"
			+ "VALUES\n"
			+ "	(#{user_account_id}, #{cfg_game_id}, #{score}) ON duplicate key UPDATE score = IF(score > #{score},score,#{score})")
	public void insertOrUpdate_user_account_cfg_game(
			@Param("user_account_id") int user_account_id,
			@Param("cfg_game_id") int cfg_game_id, @Param("score") double score);



	// @Select("SELECT * FROM user_account_cfg_game WHERE cfg_game_id = #{cfg_game_id} ORDER BY score DESC LIMIT 10")
	@Select("SELECT a.user_account_id,b.nickname,b.headimgurl,a.score FROM \n"
			+ "(SELECT * FROM user_account_cfg_game  WHERE cfg_game_id = #{cfg_game_id} ORDER BY score DESC LIMIT 10) as a INNER JOIN user_account b ON a.user_account_id = b.id")
	public List<User_account_Cfg_game_VO> select_user_account_cfg_game(
			@Param("cfg_game_id") int cfg_game_id);

	// @Select("SELECT * FROM user_account_cfg_game WHERE cfg_game_id = #{cfg_game_id} AND user_account_id = #{user_account_id}")
	@Select("SELECT a.user_account_id,b.nickname,b.headimgurl,a.score FROM \n"
			+ "(SELECT * FROM user_account_cfg_game WHERE cfg_game_id = #{cfg_game_id} AND user_account_id = #{user_account_id}) as a INNER JOIN user_account b ON a.user_account_id = b.id")
	public User_account_Cfg_game_VO select_user_account_cfg_game_by_user_account_id(
			@Param("cfg_game_id") int cfg_game_id,
			@Param("user_account_id") int user_account_id);

	@Select("SELECT COUNT(*) FROM user_account_cfg_game WHERE cfg_game_id = #{cfg_game_id} AND score > #{score}")
	public int select_User_account_cfg_game_rank(
			@Param("cfg_game_id") int cfg_game_id, @Param("score") double score);

}
