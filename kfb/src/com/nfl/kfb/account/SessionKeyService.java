/**
 * 
 */
package com.nfl.kfb.account;


/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 *
 */
public interface SessionKeyService {

//	@Cacheable(cacheName = "sessionKeyCache")
	public int generateRandomSessionKey(String appId);

	/**
	 * 캐시된 세션키를 가져옴. 캐시에 없다면 DB에서 읽어옴
	 * @param appId
	 * @return
	 */
//	@Cacheable(cacheName = "sessionKeyCache")
//	public Integer getCachedSessionKey(String appId);
	
//	@TriggersRemove(cacheName="sessionKeyCache", when=When.BEFORE_METHOD_INVOCATION)
//	public void flushCachedSessionKey(String appId);

}
