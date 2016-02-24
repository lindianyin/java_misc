/**
 * 
 */
package com.nfl.kfb.mapper.inv;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

/**
 * @author KimSeongsu
 * @since 2013. 8. 14.
 *
 */
public interface InvMapper {
	
	public Inv selectInv(@Param("appId") String appId);

	public void updateInv(Inv inv);
	
	public void insertInv(Inv inv);

	public void deleteInv(@Param("appId") String appId);

	public void insertOrUpdateInvList(@Param("appId") String appId, @Param("fAppId") String fAppId, @Param("invDt") int invDt);

	public List<InvList> selectInvList(@Param("appId") String appId, @Param("invDtLimit") int invDtLimit);
	
	
	@Select("SELECT * from inv_bak \n" +
			"WHERE  appId = #{appId} AND state = 0 AND DATEDIFF(time,NOW()) = 0")
	public List<InvBak> selectInvBak(@Param("appId") String appId);
	
	@Insert("INSERT INTO inv_bak(id,appId,time,state,friId) \n" +
			"VALUES(#{id},#{appId},#{time},#{state},#{friId})")
	@SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=int.class)
	public int insertInvBak(InvBak invBak);
	
	@Select("SELECT COUNT(*) from inv_bak WHERE  appId = #{appId} AND DATEDIFF(time,NOW()) = 0")
	public int countOfInvBak(@Param("appId") String appId);
	
	@Delete("DELETE FROM inv_bak WHERE appId = #{appId}")
	public int deleteInvBak(@Param("appId") String appId);
	
	@Update("UPDATE inv_bak SET  state = 1 WHERE appId = #{appId} AND friId= #{friId}")
	public int updateInvBak(@Param("appId") String appId,@Param("friId") String friId);
	
	
	
	
	
	
	
	
}
