/**
 * 
 */
package com.nfl.kfb.event;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.account.EventDirectRwResponse;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.event.Event;
import com.nfl.kfb.mapper.event.EventMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.logs.EventRewardLog;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.ResourceService.GAME_OPTION;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 10. 14.
 *
 */
@Service(value="EventServiceImpl")
public class EventServiceImpl extends AbstractKfbService implements EventService {
	
	private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
	
	@Autowired
	private EventMapper eventMapper;
	
	@Autowired
	private MailMapper mailMapper;
	
	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;
	
	@Autowired
	private AccountMapper accountMapper;
	
	@Transactional
	@Override
	public List<GameLog> eventLogin(DateUtil dateUtil, Account account) {
		List<Event> myEvents = new ArrayList<Event>();		// 나에게 적용될 이벤트 목록
		List<GameLog> eventRewardLogs = new ArrayList<GameLog>();
		
		final int nowEpoch = dateUtil.getNowEpoch();
		int todayStartEpoch = 0;		// init when necessary
		int hourStartEpoch = 0;			// init when necessary
		
		List<Event> events = resourceService.getEvents();
		for (Event event : events) {
			if (nowEpoch < event.getStart_dt() || nowEpoch >= event.getEnd_dt())
				continue;
			
			switch (EVENT_TYPE.valueOf(event.getEvent_type())) {
			case LOGIN_EVERY:
				myEvents.add(event);
				break;
			case LOGIN_EVERY_DAY:
				if (todayStartEpoch == 0) {
					todayStartEpoch = dateUtil.getTodayStartEpoch();
				}
				Integer lastEventEpoch = eventMapper.selectEventLogLastEpoch(account.getAppId(), event.getEvent_key());
				if (lastEventEpoch == null || lastEventEpoch.intValue() < todayStartEpoch) {
					myEvents.add(event);		// 받은적이 없거나, 오늘 이전에 받았으면 지급
				}
				break;
			case LOGIN_EVERY_HOUR:
				if (hourStartEpoch == 0) {
					hourStartEpoch = dateUtil.getHourStartEpoch();
				}
				lastEventEpoch = eventMapper.selectEventLogLastEpoch(account.getAppId(), event.getEvent_key());
				if (lastEventEpoch == null || lastEventEpoch.intValue() < hourStartEpoch) {
					myEvents.add(event);		// 받은적이 없거나, 현재 시(hour) 이전에 받았으면 지급
				}
				break;
			case LOGIN_ONCE:
				lastEventEpoch = eventMapper.selectEventLogLastEpoch(account.getAppId(), event.getEvent_key());
				if (lastEventEpoch == null || lastEventEpoch.intValue() == 0) {
					myEvents.add(event);		// 받은적이 없으면 지급
				}
				break;
			default:
				break;
			}
		}
		
		if (!myEvents.isEmpty()) {
			for (Event event : myEvents) {
				Mail mail = new Mail();
				mail.setSender(Mail.SENDER_ID_ADMIN);
				mail.setOwner(account.getAppId());
				mail.setItem(event.getItem_id());
				mail.setCnt(event.getItem_cnt());
				mail.setDelDt(nowEpoch + DateUtil.ONE_DAY_EPOCH * event.getMail_keep_days());
				mail.setMsg(event.getMail_msg());
				
				mailMapper.insertMail(mail);
				eventMapper.insertEventLog(account.getAppId(), event.getEvent_key(), nowEpoch, mail.getMailKey(), event.getItem_id(), event.getItem_cnt());
				
				EventRewardLog eventRewardLog = new EventRewardLog(dateUtil, account.getAppId());
				eventRewardLog.setEventKey(event.getEvent_key());
				eventRewardLog.setMailKey(mail.getMailKey());
				eventRewardLog.setItemId(event.getItem_id());
				eventRewardLog.setItemCnt(event.getItem_cnt());
				eventRewardLogs.add(eventRewardLog);
				
				try {
					logger.debug("EVENT_TYPE={}, EVENT_KEY={}, ITEM_ID={}, ITEM_CNT={}, MAIL_KEY={}", EVENT_TYPE.valueOf(event.getEvent_type()).name(), event.getEvent_key(), event.getItem_id(), event.getItem_cnt(), mail.getMailKey());
				} catch (Exception e) {}
			}
		}
		
		return eventRewardLogs;
	}

	@Transactional
	@Override
	public EventDirectRwResponse eventDirectRw(DateUtil dateUtil, Account account, int eventKey, List<GameLog> eventRewardLogs) {
		Event myEvent = null;
		final int nowEpoch = dateUtil.getNowEpoch();
		
		List<Event> events = resourceService.getEvents();
		for (Event event : events) {		// 같은 key값의 이벤트 1개 찾아냄
			if (event.getEvent_key() == eventKey) {
				myEvent = event;
				break;
			}
		}
		
		// 이벤트가 없거나, 기간이 지났음
		if (myEvent == null || (nowEpoch < myEvent.getStart_dt() || nowEpoch >= myEvent.getEnd_dt())) {
			EventDirectRwResponse eventDirectRwResponse = new EventDirectRwResponse(ReturnCode.SUCCESS);
			eventDirectRwResponse.setSuccess(EventDirectRwResponse.EVENT_DIRECT_RW_EVENT_ENDED);
			eventDirectRwResponse.setITID(0);
			eventDirectRwResponse.setITCNT(0);
			eventDirectRwResponse.setGD(account.getGold());
			eventDirectRwResponse.setBL(account.getBall());
			eventDirectRwResponse.setPN(account.getPunch());
			eventDirectRwResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
			
			return eventDirectRwResponse;
		}
		
		int latestShopVer = Integer.parseInt(resourceService.getGameOption(GAME_OPTION.LATEST_SHOP_VER));
		
		switch (EVENT_TYPE.valueOf(myEvent.getEvent_type())) {
		case CLIENT_REQ_ONCE:
			Integer lastEventEpoch = eventMapper.selectEventLogLastEpoch(account.getAppId(), myEvent.getEvent_key());
			if (lastEventEpoch == null || lastEventEpoch.intValue() == 0) {		// 받은적이 없으면 지급
				Shop item = resourceService.getShop(latestShopVer, myEvent.getItem_id());
				if (item == null) {		// 보상 아이템을 찾을 수 없음
					throw new RuntimeException("Couln't find eventDirectRw item. itemId=" + myEvent.getItem_id());
				}
				
				if (!isAccountItem(item.getItemId())) {
					// 이벤트 정보를 공통으로 사용하므로(EVENTS) 엽전/여의주/주먹만 가능하다
					throw new RuntimeException("EventDirectRw item must be accountItem");
				}
				
				logger.debug("EVENT_TYPE={}, EVENT_KEY={}, ITEM_ID={}, ITEM_CNT={}", EVENT_TYPE.valueOf(myEvent.getEvent_type()).name(), myEvent.getEvent_key(), myEvent.getItem_id(), myEvent.getItem_cnt());
				
				EventRewardLog eventRewardLog = new EventRewardLog(dateUtil, account.getAppId());
				eventRewardLog.setEventKey(myEvent.getEvent_key());
//				eventRewardLog.setMailKey(0);		// 메일로 추가되는게 아님
				
				addAccountItem(dateUtil, account, item, myEvent.getItem_cnt(), eventRewardLog);
				accountMapper.updateAccountItem(account);
				
				eventMapper.insertEventLog(account.getAppId(), myEvent.getEvent_key(), nowEpoch, 0, myEvent.getItem_id(), myEvent.getItem_cnt());
				
				EventDirectRwResponse eventDirectRwResponse = new EventDirectRwResponse(ReturnCode.SUCCESS);
				eventDirectRwResponse.setSuccess(EventDirectRwResponse.EVENT_DIRECT_RW_SUCCESS);
				eventDirectRwResponse.setITID(item.getItemId());
				eventDirectRwResponse.setITCNT(myEvent.getItem_cnt());
				eventDirectRwResponse.setGD(account.getGold());
				eventDirectRwResponse.setBL(account.getBall());
				eventDirectRwResponse.setPN(account.getPunch());
				eventDirectRwResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
				
				eventRewardLogs.add(eventRewardLog);
				
				return eventDirectRwResponse;
			}
			else {
				EventDirectRwResponse eventDirectRwResponse = new EventDirectRwResponse(ReturnCode.SUCCESS);
				eventDirectRwResponse.setSuccess(EventDirectRwResponse.EVENT_DIRECT_RW_DUPLICATE);
				eventDirectRwResponse.setITID(0);
				eventDirectRwResponse.setITCNT(0);
				eventDirectRwResponse.setGD(account.getGold());
				eventDirectRwResponse.setBL(account.getBall());
				eventDirectRwResponse.setPN(account.getPunch());
				eventDirectRwResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
				
				return eventDirectRwResponse;
			}
			
		default:
//			EventDirectRwResponse eventDirectRwResponse = new EventDirectRwResponse(ReturnCode.SUCCESS);
//			eventDirectRwResponse.setSuccess(EventDirectRwResponse.EVENT_DIRECT_RW_EVENT_ENDED);
//			eventDirectRwResponse.setITID(0);
//			eventDirectRwResponse.setITCNT(0);
//			eventDirectRwResponse.setGD(account.getGold());
//			eventDirectRwResponse.setBL(account.getBall());
//			eventDirectRwResponse.setPN(account.getPunch());
//			eventDirectRwResponse.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
//			return eventDirectRwResponse;
			throw new RuntimeException("Undefined EVENT_TYPE in eventDirectRw. EVENT_TYPE=" + EVENT_TYPE.valueOf(myEvent.getEvent_type()).name() + ", EVENT_KEY=" + myEvent.getEvent_key());
		}				
	}

}
