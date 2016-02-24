/**
 * 
 */
package com.nfl.kfb.mail;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.logs.ReceiveMailLog;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
public interface MailService {
	
	void scheduledRemoveOldMail();
	
	void scheduledRemoveOldPunch();

	List<Mail> getMailBox(String appId, int nowEpoch);

	Integer getPunchRegDt(String appId, String fAppId);

	@Transactional
	PunchResponse sendPunch(String appId, String fAppId, int nowEpoch, boolean availableGP);

	@Transactional
	RecvMailResponse recvMail(Account account, int chId, Mail mail, DateUtil dateUtil, ReceiveMailLog receiveMailLog, int shopVer) throws Exception;

	Mail getMail(String appId, int mailKey);

	int countMailBox(String appId, int nowEpoch);

	void ResetDailyTask();

	void ResetAchieveTask();

	void snapshotGamblePoint();

	void ResetRank();



}
