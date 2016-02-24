/**
 * 
 */
package com.nfl.kfb.mail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import redis.clients.jedis.Jedis;

import com.nfl.kfb.AbstractKfbService;
import com.nfl.kfb.mail.PunchResponse.SUC;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.logs.ReceiveMailLog;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.rank.RankService;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.ResourceService.GAME_OPTION;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.ITEM_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_CATEGORY;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 * 
 */
@Service(value = "MailServiceImpl")
public class MailServiceImpl extends AbstractKfbService implements MailService {

	private static final Logger logger = LoggerFactory
			.getLogger(MailServiceImpl.class);

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private InvenMapper invenMapper;

	@Autowired
	private MailMapper mailMapper;

	@Autowired
	private GambleMapper gambleMapper;

	@Autowired
	private TaskController taskController;

	@Autowired
	private RankService rankService;

	@Autowired
	private RankMapper rankMapper;

	@Override
	@Scheduled(fixedDelay = 600000)
	// 10 minutes
	public void scheduledRemoveOldMail() {
		logger.debug("scheduledRemoveOldMail now={}",
				System.currentTimeMillis());

		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

		// 시간이 지난 오래된 메일 삭제(10분정도 여유값)
		try {
			mailMapper.deleteOldMail(dateUtil.getNowEpoch()
					- DateUtil.ONE_MINUTE_EPOCH * 10);
		} catch (Exception e) {
			logger.error("Exception while remove old mail by scheduler", e);
		}

	}

	@Override
	@Scheduled(cron = "59 59 23 * * ?")
	// @Scheduled(fixedDelay=10*1000) // 10 minutes
	public void ResetDailyTask() {
		logger.debug("delete daily task now={}", System.currentTimeMillis());
		taskController.deleteTask(TASK_CATEGORY.DAILY_TASK);
		logger.debug("delete daily activity now={}", System.currentTimeMillis());
		taskController.deleteTask(TASK_CATEGORY.ACTIVITY);
	}

	@Override
	@Scheduled(cron = "59 59 23 ? * SUN")
	// @Scheduled(fixedDelay=10*1000)
	public void ResetAchieveTask() {
		logger.debug("delete achieve task now={}", System.currentTimeMillis());
		taskController.deleteTask(TASK_CATEGORY.ACHIEVE);
	}

	@Override
	@Scheduled(cron = "59 59 23 ? * SUN")
	// @Scheduled(fixedDelay=10*1000)
	//@Scheduled(fixedDelay = 1*60*1000)
	public void ResetRank() {

		DebugOption.timerThread.execute(new Runnable() {
			@Override
			public void run() {
				List<String> selectAppId = accountMapper.selectAppId();
				DateUtil dateUtil = new DateUtil(System.currentTimeMillis()+1*60*60*1000);
				for (String appId : selectAppId) {
					rankMapper.insertOrUpdateRank(appId, dateUtil.getThisWeek(), 0);
				}
				
				Jedis jedis = new Jedis(DebugOption.REDIS_URL);
				jedis.del(DebugOption.REDIS_RANK_KEY);
				List<Rank> selectAllRank = rankService.getAllRank();
				for (Rank rank : selectAllRank) {
					jedis.zadd(DebugOption.REDIS_RANK_KEY, rank.getPoint(),
							rank.getAppId());
				}

				jedis.del(DebugOption.REDIS_UNLIMIT_RNANK_KEY);

				List<Rank> unlimitRankList = rankService.getAllUlimitRank();
				for (Rank rank : unlimitRankList) {
					jedis.zadd(DebugOption.REDIS_UNLIMIT_RNANK_KEY, rank.getPoint(),
							rank.getAppId());
				}

				jedis.close();

			}
		});

	}

	@Transactional
	@Override
	@Scheduled(cron = "59 59 23 * * ?")
	public void snapshotGamblePoint() {
		accountMapper.deleteGamblePoint();
		logger.debug("deleteGamblePoint now={}", System.currentTimeMillis());
		accountMapper.snapshotGamblePoint();
		logger.debug("snapshotGamblePoint now={}", System.currentTimeMillis());
	}

	@Override
	@Scheduled(fixedDelay = 300000)
	// 5 minutes
	public void scheduledRemoveOldPunch() {
		logger.debug("scheduledRemoveOldPunch now={}",
				System.currentTimeMillis());

		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

		// 시간이 지난 오래된 주먹쿨타임 정보 삭제(5분정도 여유값)
		try {
			mailMapper.deleteOldPunch(dateUtil.getNowEpoch()
					- DebugOption.SEND_PUNCH_COOL_TIME_EPOCH
					- DateUtil.ONE_MINUTE_EPOCH * 5);
		} catch (Exception e) {
			logger.error("Exception while remove old punch by scheduler", e);
		}

	}

	@Override
	public List<Mail> getMailBox(String appId, int nowEpoch) {
		return mailMapper.selectMailBox(appId, nowEpoch,
				DebugOption.MAIL_BOX_SQL_LIMIT);
	}

	@Override
	public Integer getPunchRegDt(String appId, String fAppId) {
		return mailMapper.selectPunchRegDt(appId, fAppId);
	}

	@Override
	@Transactional
	public PunchResponse sendPunch(String appId, String fAppId, int nowEpoch,
			boolean availableGP) {
		mailMapper.replacePunch(appId, fAppId, nowEpoch);

		final int item = DebugOption.REWARD_ITEM_PUNCH;
		final int count = 1;
		final int mailDelDt = nowEpoch + DebugOption.MAIL_PUNCH_KEEP_EPOCH;

		Mail mail = new Mail();
		mail.setSender(appId);
		mail.setOwner(fAppId);
		mail.setItem(item);
		mail.setCnt(count);
		mail.setDelDt(mailDelDt);
		mail.setMsg("");
		// mailMapper.insertMail(appId, fAppId, item, count, mailDelDt, "");
		mailMapper.insertMail(mail);

		// 겜블 포인트 지급
		int gamblePoint = 0;
		if (availableGP) {
			gamblePoint = DebugOption.GAMBLE_POINT_SEND_PUNCH;
			gambleMapper.increaseGamblePoint(appId, gamblePoint);
		}

		PunchResponse punchResponse = new PunchResponse(ReturnCode.SUCCESS);
		punchResponse.setSUC(SUC.SUCCESS);
		punchResponse.setDT(DebugOption.SEND_PUNCH_COOL_TIME_EPOCH);
		punchResponse.setADDGP(gamblePoint);
		return punchResponse;
	}

	@Override
	@Transactional
	public RecvMailResponse recvMail(Account account, int chId, Mail mail,
			DateUtil dateUtil, ReceiveMailLog receiveMailLog, int shopVer)
			throws Exception {
		RecvMailResponse recvMailResponse = new RecvMailResponse(
				ReturnCode.SUCCESS);

		int mailItemId = mail.getType();
		int mailItemCnt = mail.getCnt();

		Shop mailItem = resourceService.getShop(shopVer, mailItemId);

		receiveMailLog.setSender(mail.getSender());
		receiveMailLog.setMailKey(mail.getMailKey());

		if (mailItem == null) {
			recvMailResponse.setITID(0);
			recvMailResponse.setITCNT(0);
		} else {
			receiveMailLog.setHasItem();

			if (isAccountItem(mailItem.getItemId())) {
				addAccountItem(dateUtil, account, mailItem, mailItemCnt,
						receiveMailLog);
				accountMapper.updateAccountItem(account);
				recvMailResponse.setITID(mailItem.getItemId());
				recvMailResponse.setITCNT(mailItemCnt);
			} else if (isInvenItem(mailItem.getItemId())) {
				Inven newItem = createInvenItemFromShop(account.getAppId(),
						chId, mailItem, mailItemCnt, false);
				Inven existItem = invenMapper.selectItem(account.getAppId(),
						newItem.getChId(), newItem.getItemId());

				receiveMailLog.setItemId(mailItem.getItemId());
				receiveMailLog.setItemCnt(mailItemCnt);

				recvMailResponse.setITID(newItem.getItemId());
				recvMailResponse.setITCNT(mailItemCnt);

				final ITEM_TYPE itemType = DebugOption.getItemType(newItem
						.getItemId());

				switch (itemType) {
				case CHARACTER:
					if (existItem == null) {
						invenMapper.insertItem(newItem);
					} else {
						existItem.setItemLv(Math.min(
								DebugOption.MAX_CHARACTER_LV,
								existItem.getItemLv() + newItem.getItemLv()));
						int affectedRow = invenMapper.updateItem(existItem);
						if (affectedRow != 1)
							throw new RuntimeException(
									"updateItem affectedRow is not 1. something wrong.");
					}
					break;

				case SKILL:
					if (existItem == null) {
						invenMapper.insertItem(newItem);
					} else {
						existItem.setItemCnt(existItem.getItemCnt()
								+ newItem.getItemCnt());
						int affectedRow = invenMapper.updateItem(existItem);
						if (affectedRow != 1)
							throw new RuntimeException(
									"updateItem affectedRow is not 1. something wrong.");
					}
					break;

				case CONSUME:
					if (existItem == null) {
						invenMapper.insertItem(newItem);
					} else {
						existItem.setItemCnt(existItem.getItemCnt()
								+ newItem.getItemCnt());
						int affectedRow = invenMapper.updateItem(existItem);
						if (affectedRow != 1)
							throw new RuntimeException(
									"updateItem affectedRow is not 1. something wrong.");
					}
					break;

				case EQUIP:
					if (existItem == null) {
						invenMapper.insertItem(newItem);
					} else {
						existItem.setItemLv(existItem.getItemLv()
								+ newItem.getItemLv());
						int affectedRow = invenMapper.updateItem(existItem);
						if (affectedRow != 1)
							throw new RuntimeException(
									"updateItem affectedRow is not 1. something wrong.");
					}
					break;

				case PET:
					if (existItem == null) {
						invenMapper.insertItem(newItem);
					} else {
						existItem.setItemLv(Math.min(DebugOption.MAX_PET_LV,
								existItem.getItemLv() + newItem.getItemLv()));
						int affectedRow = invenMapper.updateItem(existItem);
						if (affectedRow != 1)
							throw new RuntimeException(
									"updateItem affectedRow is not 1. something wrong.");
					}
					break;

				default: // 그외의 아이템은 INVEN에 들어가는 아이템이 아님
					throw new RuntimeException(
							"Cannot add item to inven. this is not inven item");
				}
			}
		}

		recvMailResponse.setGD(account.getGold());
		recvMailResponse.setBL(account.getBall());
		recvMailResponse.setPN(account.getPunch());
		recvMailResponse.setPNDT(account.getPunchRemainDt(dateUtil
				.getNowEpoch()));

		mailMapper.removeMail(mail.getMailKey());

		return recvMailResponse;
	}

	@Override
	public Mail getMail(String appId, int mailKey) {
		return mailMapper.selectMail(appId, mailKey);
	}

	@Override
	public int countMailBox(String appId, int nowEpoch) {
		return mailMapper.countMailBox(appId, nowEpoch);
	}

}
