/**
 * 
 */
package com.nfl.kfb.mapper.inv;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @author KimSeongsu
 * @since 2013. 8. 14.
 *
 */
public interface FriendlyMapper {
	
	public List<String> selectFriendlyList(@Param("fri_self") String fri_self,@Param("status") int status);
	
	public Friendly selectFriendly(@Param("fri_self") String fri_self,@Param("fri_op") String fri_op);
	
	public void insertFriendly(Friendly friendly);
	
	public void updateFriendly(@Param("fri_self") String fri_self,@Param("fri_op") String fri_op,@Param("status") int status);
	
	public void deleteFriendly(@Param("fri_self") String fri_self,@Param("fri_op") String fri_op,@Param("status") int status);
	
	public List<String> selectFriendlyListReq(@Param("fri_self") String fri_self,@Param("status") int status);
}
