/**
 * 
 */
package com.nfl.kfb.friend;

import java.io.IOException;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.logs.InvFriendLog;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 *
 */
public interface FriendService {

	@Transactional
	/**
	 * 친구 초대
	 * @param dateUtil
	 * @param account
	 * @param invCnt
	 * @param friendId
	 * @param invFriendLog
	 * @param availableGP
	 * @param shopVer
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	FriendInvResponse invFriend(DateUtil dateUtil, Account account, int invCnt, String friendId, InvFriendLog invFriendLog, boolean availableGP, int shopVer) 
			throws JsonParseException, JsonMappingException, IOException;

	/**
	 * 친구초대 리스트(쿨타임인 친구들만)
	 * @param dateUtil
	 * @param account
	 * @return
	 */
	FriendInvListResponse invFriendList(DateUtil dateUtil, Account account);

	FriendInfoResponse getFriendInfo(DateUtil dateUtil, Account account, String[] appIds,boolean ISGATE);

	List<String> getFriendList(String id);

	@Transactional
	boolean addFriend(String fri_self, String fri_op);

	
	List<String> getRequestFriendList(String id);

	
	boolean acceptFriendReq(String fri_self,String fri_op);

	void deleteFriend(String fri_self, String fri_op,int status);

	List<String> getFriendListReq(String id);
	
}
