/**
 * 
 */
package com.nfl.kfb.mail;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mail.PunchResponse.SUC;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.logs.ReceiveMailLog;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 *
 */
@Controller
@RequestMapping(value="/mail", method={RequestMethod.POST , RequestMethod.GET})
public class MailController extends AbstractKfbController {
	
	private static final Logger logger = LoggerFactory.getLogger(MailController.class);
	
	@Autowired
	@Qualifier("MailServiceImpl")
	private MailService mailService;
	
	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;
	
	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;
	
	
	@Autowired
	private TaskController taskController;
	
	
	
	private static final String URL_MAIL_BOX_LIST = "list";
	private static final String URL_SEND_PUNCH = "punch";
	private static final String URL_RECV_MAIL = "recv";

	
	@RequestMapping(value=URL_MAIL_BOX_LIST)
	@ResponseBody
	/**
	 * 메시지함 리스트 요청
	 * @param ID
	 * @param SK
	 * @return
	 */
	public JsonResponse getMailBoxList(
			@RequestParam(value="ID", required=true) String ID
			, @RequestParam(value="SK", required=true) int SK
			) {
		try {
			Account account = accountService.getAccount(ID);
			
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			
			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			
			List<Mail> mailBox = mailService.getMailBox(ID, dateUtil.getNowEpoch());
			for(Mail mail : mailBox){
				if(!mail.getSender().equals("0")){
					String nickname = accountService.getAccount(mail.getSender()).getNickname();
					mail.setMsg(nickname+"#"+mail.getMsg());
				}
			}
			
			
			MailBoxResponse mailBoxResponse = new MailBoxResponse(ReturnCode.SUCCESS);
			for (Mail mail : mailBox) {
				mailBoxResponse.addM(mail.getMailKey(), mail.getSender(), mail.getType(), mail.getCnt(), mail.getDelDt(), mail.getMsg(),mail.getMsg().indexOf("好友") != -1,mail.getTitle());
			}
			
			return mailBoxResponse;
		} catch (Exception e) {
			logger.error("ID="+ID, e);
			JsonResponse errorResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}
	
	@RequestMapping(value=URL_SEND_PUNCH)
	@ResponseBody
	/**
	 * 주먹 선물<br>
	 * @param ID
	 * @param SK
	 * @param FID
	 * @return
	 */
	public JsonResponse sendPunch(
			@RequestParam(value="ID", required=true) String ID
			, @RequestParam(value="SK", required=true) int SK
			, @RequestParam(value="FID", required=true) String FID
			, @RequestParam(value="GP", required=false, defaultValue="false") boolean GP
			) {
		try {
			Account account = accountService.getAccount(ID);
			
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			
			if (ID.equalsIgnoreCase(FID)) {
				JsonResponse errorResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Couldn't send punch to yourself"));
				return errorResponse;
			}
			
			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			
			Integer regDt = mailService.getPunchRegDt(ID, FID);
			
			// 주먹선물 쿨타임이 지나지 않았음
			if (regDt != null && (regDt.intValue() + DebugOption.SEND_PUNCH_COOL_TIME_EPOCH) > dateUtil.getNowEpoch()) {
				PunchResponse punchResponse = new PunchResponse(ReturnCode.SUCCESS);
				punchResponse.setSUC(SUC.COOL_TIME);
				punchResponse.setDT((regDt.intValue() + DebugOption.SEND_PUNCH_COOL_TIME_EPOCH) - dateUtil.getNowEpoch());
				punchResponse.setADDGP(0);
				return punchResponse;
			}
			
			// 주먹선물 시각 기록 & 메일 insert & 겜블포인트 지급
			PunchResponse punchResponse = mailService.sendPunch(ID, FID, dateUtil.getNowEpoch(), GP);
			
			{
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.GET_REWARD_PUNCH,1);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}
			
			
			return punchResponse;
		} catch (Exception e) {
			logger.error("ID="+ID, e);
			JsonResponse errorResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}
	
	@RequestMapping(value=URL_RECV_MAIL)
	@ResponseBody
	/**
	 * 메시지 받기<br>
	 * @param ID
	 * @param SK
	 * @param KEY			메일키
	 * @return
	 */
	public JsonResponse recvMail(
			@RequestParam(value="ID", required=true) String ID
			, @RequestParam(value="SK", required=true) int SK
			, @RequestParam(value="KEY", required=true) int KEY
			, @RequestParam(value="CHID", required=true) int CHID
			, @RequestParam(value="SHOPVER", required=false, defaultValue="0") int SHOPVER
			) {
		try {
			Account account = accountService.getAccount(ID);
			
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			
			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			
			Mail mail = mailService.getMail(ID, KEY);
			
			// 메일이 없음
			if (mail == null) {
				JsonResponse jsonResponse = new JsonResponse(ReturnCode.DELETED_MAIL);
				return jsonResponse;
			}
			// 이미 삭제되었을 시각
			if (mail.getDelDt() < dateUtil.getNowEpoch()) {
				JsonResponse jsonResponse = new JsonResponse(ReturnCode.DELETED_MAIL);
				jsonResponse.setException(new Exception("expired mail"));
				return jsonResponse;
			}
			
			ReceiveMailLog receiveMailLog = new ReceiveMailLog(dateUtil, account.getAppId());
			
			RecvMailResponse recvMailResponse = mailService.recvMail(account, CHID, mail, dateUtil, receiveMailLog, SHOPVER);
			
			if (receiveMailLog != null && receiveMailLog.hasItem()) {		// 아이템을 추가했을때만 로그를 남김
				loggingService.insertGameLog(receiveMailLog);
			}
			
			return recvMailResponse;
		} catch (Exception e) {
			logger.error("ID="+ID, e);
			JsonResponse errorResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}
	

	
}
