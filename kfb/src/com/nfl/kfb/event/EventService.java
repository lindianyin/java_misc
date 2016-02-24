/**
 * 
 */
package com.nfl.kfb.event;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.account.EventDirectRwResponse;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 10. 14.
 *
 */
public interface EventService {
	
	public enum EVENT_TYPE {
		NULL(-1),
		UNKNOWN(0),
		LOGIN_ONCE(1),				// 로그인시. 기간내 한번
		LOGIN_EVERY(2),			// 매번 로그인시
		LOGIN_EVERY_DAY(3),		// 매일 로그인시
		LOGIN_EVERY_HOUR(4),		// 매시각 로그인시
		CLIENT_REQ_ONCE(5);		// 클라이언트가 요청했을때 한번 보상지급
		
		private final int value;

		private EVENT_TYPE(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}

		public static final EVENT_TYPE valueOf(int value) {
			for (EVENT_TYPE storeType : EVENT_TYPE.values()) {
				if (storeType.getValue() == value)
					return storeType;
			}
			return null;
		}
		
	}
	
	@Transactional
	/**
	 * LOGIN, 로그인시 해당 아이템을 메시지함으로 지급<br>
	 * @param dateUtil
	 * @param account
	 * @return	
	 */
	public List<GameLog> eventLogin(DateUtil dateUtil, Account account);
	
	@Transactional
	/**
	 * 클라이언트에서 요청될때. 인벤토리에 이벤트 보상 직접 추가<br>
	 * @param dateUtil
	 * @param account
	 * @param eventKey
	 * @param eventRewardLogs
	 * @return
	 */
	public EventDirectRwResponse eventDirectRw(DateUtil dateUtil, Account account, int eventKey, List<GameLog> eventRewardLogs);
	

}
