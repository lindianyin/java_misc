/**
 * 
 */
package com.nfl.kfb.account;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nfl.kfb.mapper.account.Account;

/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 *
 */
@Service(value="SessionKeyServiceImpl")
public class SessionKeyServiceImpl implements SessionKeyService {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SessionKeyServiceImpl.class);
	
//	@Autowired
//	private AccountMapper accountMapper;
	
	/**
	 * 0이 아닌 Integer 로 sessionKey 랜덤 생성
	 * @param appId
	 */
	@Override
//	@Cacheable(cacheName = "sessionKeyCache")
	public int generateRandomSessionKey(String appId) {
		Random random = new Random();
		int randomValue = random.nextInt();
		while (randomValue == Account.DEFAULT_SESSION_KEY) {
			randomValue = random.nextInt();
		}
		return randomValue;
	}
	
//	@Override
//	@Cacheable(cacheName = "sessionKeyCache")
//	public Integer getCachedSessionKey(String appId) {
//		return accountMapper.selectSessionKey(appId);
//	}

//	@Override
//	@TriggersRemove(cacheName = "sessionKeyCache", removeAll=true, when = When.BEFORE_METHOD_INVOCATION)
//	public void flushCachedSessionKey(String appId) {
//		logger.debug("Force flush sessionKey. appId={}", appId);
//	}

}
