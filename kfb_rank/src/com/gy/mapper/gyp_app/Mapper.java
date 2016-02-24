package com.gy.mapper.gyp_app;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import com.gy.model.ItemCount;
import com.gy.model.User_account;
import com.gy.model.User_rank_vo;
import com.gy.model.User_statistics;

public interface Mapper {
	@Insert("INSERT INTO user_account (id, mac_addr, nickname) VALUES (#{id}, #{mac_addr}, #{nickname})")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
	int insert_user_account(User_account ua);

	@Insert("INSERT INTO user_rank(user_account_id, score) VALUES (#{user_account_id}, #{score})")
	int inset_user_rank(@Param("user_account_id") int user_account_id,@Param("score") int score);
	
	
	@Select("SELECT * FROM user_account WHERE mac_addr = #{mac_addr}")
	User_account select_user_account(@Param("mac_addr") String mac_addr);

	@Select("SELECT\n" + "	b.user_account_id,\n" + "	a.nickname,\n"
			+ "	b.score\n" + "FROM\n" + "	user_account a\n"
			+ "INNER JOIN user_rank b ON a.id = b.user_account_id\n"
			+ "ORDER BY\n" + "	score DESC\n" + "LIMIT #{rank_size}")
	List<User_rank_vo> select_user_rank_vo_list(
			@Param("rank_size") int rank_size);

	@Select("SELECT\n" + "	b.user_account_id,\n" + "	a.nickname,\n"
			+ "	b.score\n" + "FROM\n" + "	user_account a\n"
			+ "INNER JOIN user_rank b ON a.id = b.user_account_id\n"
			+ "where b.user_account_id = #{user_account_id}")
	User_rank_vo select_user_rank_vo(
			@Param("user_account_id") int user_account_id);

	@Update("UPDATE user_rank SET score = IF (score > #{score}, score,  #{score}) WHERE user_account_id = #{user_account_id}")
	int update_user_rank_score(@Param("user_account_id") int user_account_id,@Param("score") int score);
	
	
	
	@Select("SELECT value FROM base_first_name_female")
	List<String> select_base_first_name_female();
	
	@Select("SELECT value FROM base_first_name_male")
	List<String> select_base_first_name_male();
	
	@Select("SELECT value FROM base_second_name_female")
	List<String> select_base_second_name_female();
	
	@Select("SELECT value FROM base_second_name_male")
	List<String> select_base_second_name_male();

	
	
	@Insert("INSERT INTO user_statistics (id, user_account_id,base_statistics_id, time, number, ext, ip_port) VALUES (#{id}, #{user_account_id},#{base_statistics_id}, #{time}, #{number}, #{ext}, #{ip_port});")
	@SelectKey(statement = "SELECT LAST_INSERT_ID()", keyProperty = "id", before = false, resultType = int.class)
	int insert_user_statistics(User_statistics us);
	
	@Update("update user_account set nickname = #{nickname} where id = #{id}")
	int update_user_account_nickname(@Param("nickname") String nickname,@Param("id") int id);
	
	
	
	
	/*以下是后台数据统计的sql*/
	@Select("SELECT COUNT(*) FROM user_account;")
	int select_user_account_count();
	
	@Select("SELECT SUM(c.amount) total FROM\n" +
			"(SELECT  a.price amount   FROM base_statistics a INNER JOIN user_statistics b on a.id = b.base_statistics_id and b.base_statistics_id > 4 and b.base_statistics_id < 1000) as c")
	Integer select_total_amount();
	
	
	@Select("SELECT COUNT(*) FROM\n" +
            "(SELECT DISTINCT(user_account_id) FROM user_statistics a WHERE a.base_statistics_id > 4 and a.base_statistics_id < 1000 ) as b")
	int select_recharge_user_count();
	
	@Select("SELECT COUNT(*) FROM user_statistics a WHERE a.base_statistics_id > 4 AND a.base_statistics_id < 1000")
	int select_recharge_count();
	
	@Select("SELECT COUNT(*) FROM user_statistics a WHERE a.base_statistics_id = 2")
	int select_open_game_count();
	
	
	@Select("SELECT DISTINCT(user_account_id) FROM user_statistics WHERE base_statistics_id > 4 AND base_statistics_id < 1000 ")
	List<Integer> select_recharge_id_list();
	
	@Select("SELECT nickname FROM user_account WHERE id = #{id};")
	String select_user_account_nickname(@Param("id") int id);
	
	@Select("SELECT SUM(c.amount) total FROM\n" +
			"			(SELECT  a.price amount,b.user_account_id   FROM base_statistics a INNER JOIN user_statistics b on a.id = b.base_statistics_id) as c WHERE c.user_account_id = #{id}")
	int select_user_amount(@Param("id") int id);
	
	@Select("SELECT COUNT(*) FROM user_statistics a WHERE a.base_statistics_id > 4 AND a.base_statistics_id < 1000 AND user_account_id = #{id}")
	int select_user_recharge_count(@Param("id") int id);
	
	@Select("SELECT COUNT(*) FROM user_statistics a WHERE  user_account_id = #{id} AND a.base_statistics_id = 2")
	int select_user_open_game_time(@Param("id") int id);
	
	@Select("SELECT IF(SUM(number) IS NULL,0,SUM(number)) as sum FROM user_statistics WHERE user_account_id = #{id} AND base_statistics_id = 3")
	int select_total_play_time(@Param("id") int id);
	
	@Select("SELECT IF(SUM(number) IS NULL,0,SUM(number)) as sum FROM user_statistics WHERE  base_statistics_id = 3")
	int select_all_play_time();
	
	
	@Select("SELECT a.name,b.count  FROM\n" +
			" (SELECT * FROM base_statistics a where id > 4 AND id < 1000 ) a   LEFT JOIN \n" +
			"			(SELECT base_statistics_id,COUNT(*) count FROM user_statistics WHERE base_statistics_id > 4 AND base_statistics_id < 1000 GROUP BY base_statistics_id ) b on a.id = b.base_statistics_id\n" +
			"			ORDER BY b.count desc;")
	List<ItemCount> select_user_statistics();
	
	@Select("SELECT a.id,a.name,b.count FROM base_statistics a  INNER JOIN \n" +
			"	(SELECT *,COUNT(*) count FROM\n" +
			"	 (SELECT  user_account_id,max(base_statistics_id) base_statistics_id FROM user_statistics WHERE base_statistics_id >= 1000 AND base_statistics_id < 1100 GROUP BY user_account_id) c GROUP BY c.base_statistics_id) b on a.id = b.base_statistics_id")
	List<ItemCount> select_user_statistics_progress();
	
	
	
	
	
}
