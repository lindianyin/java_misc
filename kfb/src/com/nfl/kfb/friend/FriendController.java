/**
 * 
 */
package com.nfl.kfb.friend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Consumer;

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
import com.nfl.kfb.mail.MailController;
import com.nfl.kfb.mail.RecvMailResponse;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.inv.Friendly;
import com.nfl.kfb.mapper.inv.FriendlyMapper;
import com.nfl.kfb.mapper.inv.InvBak;
import com.nfl.kfb.mapper.inv.InvMapper;
import com.nfl.kfb.mapper.logging.logs.InvFriendLog;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.shop.ShopService;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 6. 25.
 * 
 */
@Controller
@RequestMapping(value = "/fr", method = { RequestMethod.POST, RequestMethod.GET })
public class FriendController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory
			.getLogger(FriendController.class);

	private static final String URL_INV_FRIEND = "/inv";
	private static final String URL_INV_FRIEND_LIST = "/invList";
	private static final String URL_FRIEND_INFO = "/info";

	private static final String URL_GET20PLAYER = "/get20player";

	private static final String URL_GET_FRIEND_LIST = "/getfriendlist";

	// private static final String URL_ADD_FRIEND ="/addfriend";

	private static final String URL_ACCEPT_FRIEND_REQ = "/acceptfriendreq";

	private static final String URL_AUTO_ADD_FRIEND = "/autoaddfriend";

	// private static final String URL_DELETE_FRIEND ="/deletefriend";

	@Autowired
	@Qualifier("FriendServiceImpl")
	private FriendService friendService;

	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;

	@Autowired
	@Qualifier("ShopServiceImpl")
	private ShopService shopService;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	private MailMapper mailMapper;

	@Autowired
	private TaskController taskController;

	@Autowired
	private InvMapper invMapper;

	@Autowired
	private MailController mailController;

	@Autowired
	private FriendlyMapper friendlyMapper;

	@RequestMapping(value = URL_INV_FRIEND)
	@ResponseBody
	public JsonResponse invFriend(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "INVCNT", required = true) int INVCNT,
			@RequestParam(value = "MD5", required = true) String MD5,
			@RequestParam(value = "FID", required = true) String FID,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {

		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			if (!DebugOption.isValidFriendInvMD5(MD5, ID, SK, INVCNT)) {
				FriendInvResponse friendInvResponse = new FriendInvResponse(
						ReturnCode.WRONG_MD5);
				return friendInvResponse;
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			InvFriendLog invFriendLog = new InvFriendLog(dateUtil,
					account.getAppId());

			FriendInvResponse friendInvResponse = friendService.invFriend(
					dateUtil, account, INVCNT, FID, invFriendLog, GP, SHOPVER);

			if (invFriendLog != null && invFriendLog.hasReward()) { // 초대 보상을
																	// 받았을때만 로깅
				loggingService.insertGameLog(invFriendLog);
			}
			// JsonResponse addfriend = this.addfriend(ID, SK, FID);
			boolean addFriend = friendService.addFriend(ID, FID);
			if (!addFriend) {
				// throw new Exception("加好友异常");
				logger.info("fail add friend!!");
				JsonResponse jr = new JsonResponse(ReturnCode.UNKNOWN_ERR);
				return jr;
				// friendInvResponse.setRC(ReturnCode.UNKNOWN_ERR);
				// return friendInvResponse;
			}
			Mail mail = new Mail();
			mail.setSender(ID);
			mail.setOwner(FID);
			mail.setItem(200);
			mail.setCnt(1);
			mail.setDelDt(dateUtil.getNowEpoch()
					+ DebugOption.MAIL_PUNCH_KEEP_EPOCH);
			// mail.setMsg("加好友申请:"+ID+"想加你为好友");
			mail.setMsg("好友申请:[" + account.getNickname() + "]想加你为友");
			mailMapper.insertMail(mail);

			{
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.INVIVE_FRIEND, 1);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}

			invMapper.updateInvBak(ID, FID);
			invMapper.insertOrUpdateInvList(ID, FID,
					new DateUtil(System.currentTimeMillis()).getNowEpoch());

			//System.out.println("#########updateInvBak");
			return friendInvResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_INV_FRIEND_LIST)
	@ResponseBody
	public JsonResponse invFriendList(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {

		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			FriendInvListResponse friendInvListResponse = friendService
					.invFriendList(dateUtil, account);

			return friendInvListResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_FRIEND_INFO)
	@ResponseBody
	public JsonResponse getFriendInfo(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "FID", required = false) String[] FID,
			@RequestParam(value = "ISGATE", required = false,defaultValue="true") boolean ISGATE) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			List<String> friendList = friendService.getFriendList(ID);
			// friendList.add(ID);
			// friendList.forEach(new Consumer<String>() {
			// @Override
			// public void accept(String t) {
			// // TODO Auto-generated method stub
			// System.out.println(t);
			// }
			// });
			friendList.add(ID);
			String[] strings = new String[friendList.size()];
			friendList.toArray(strings);

			FriendInfoResponse friendInfoResponse = friendService
					.getFriendInfo(dateUtil, account, strings,ISGATE);

			return friendInfoResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	// 推荐的加好友的人
	@RequestMapping(value = URL_GET20PLAYER)
	@ResponseBody
	public JsonResponse get20Player(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		Account account = accountService.getAccount(ID);
		if (!isValidSessionKey(account, SK)) {
			return new WrongSessionKeyResponse();
		}

		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		List<InvBak> selectInvBak = invMapper.selectInvBak(ID);
		List<Account> accountList = new ArrayList<Account>();
		if (selectInvBak.size() > 0) {
			for (int i = 0; i < selectInvBak.size(); i++) {
				InvBak invBak = selectInvBak.get(i);
				String friId = invBak.getFriId();
				Account account2 = accountService.getAccount(friId);
				accountList.add(account2);
			}
		} else {
			if (invMapper.countOfInvBak(ID) == 0) {
				invMapper.deleteInvBak(ID);
				List<Account> _accountOfRecommand = accountService
						.getAccountOfRecommand(ID, 10);
				for (int i = 0; i < _accountOfRecommand.size(); i++) {
					InvBak invBak = new InvBak();
					invBak.setAppId(ID);
					invBak.setFriId(_accountOfRecommand.get(i).getAppId());
					invBak.setState(0);
					invBak.setTime(new Date());
					invMapper.insertInvBak(invBak);
				}
				accountList = _accountOfRecommand;
			}
		}

		FriendList fl = new FriendList();
		fl.setStatus(0);
		fl.setFriends_count(accountList.size());
		final List<Friend> fdList = new ArrayList<Friend>();
		accountList.forEach(new Consumer<Account>() {
			@Override
			public void accept(Account t) {
				Friend fd = new Friend();
				fd.setMessage_blocked(false);
				fd.setNickname(t.getNickname());
				fd.setProfile_image_url(t.getImg());
				fd.setUserid(t.getAppId());
				fdList.add(fd);
			}
		});
		fl.setFriends_info(fdList);
		jr.put("frList", fl);
		return jr;
	}

	// 获得自己的好友
	@RequestMapping(value = URL_GET_FRIEND_LIST)
	@ResponseBody
	public JsonResponse getFriendList(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "ISREQ", required = true) boolean ISREQ) {
		Account account = accountService.getAccount(ID);
		if (!isValidSessionKey(account, SK)) {
			return new WrongSessionKeyResponse();
		}
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		String appId = account.getAppId();
		List<String> friendList = null;
		if (ISREQ) {
			friendList = friendService.getFriendListReq(appId);// 获得加好友请求列表
		} else {
			friendList = friendService.getFriendList(appId);// 获得好友列表
		}
		final List<Account> accountList = new ArrayList<Account>();
		friendList.forEach(new Consumer<String>() {
			@Override
			public void accept(String t) {
				accountList.add(accountService.getAccount(t));
			}

		});
		FriendList fl = new FriendList();
		fl.setStatus(0);
		fl.setFriends_count(accountList.size());
		final List<Friend> fdList = new ArrayList<Friend>();
		accountList.forEach(new Consumer<Account>() {
			@Override
			public void accept(Account t) {
				Friend fd = new Friend();
				fd.setMessage_blocked(false);
				fd.setNickname(t.getAppId());
				fd.setProfile_image_url(t.getImg());
				fd.setUserid(t.getAppId());
				fdList.add(fd);
			}
		});
		fl.setFriends_info(fdList);
		jr.put("frList", fl);
		return jr;
	}

	@RequestMapping(value = URL_ACCEPT_FRIEND_REQ)
	@ResponseBody
	public JsonResponse acceptFriendRequest(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "FRIID", required = true) String FRIID,
			@RequestParam(value = "ISACC", required = true) boolean isacc,
			@RequestParam(value = "KEY", required = true) int mailKey) {
		RecvMailResponse jr = new RecvMailResponse(ReturnCode.SUCCESS);

		Account account = accountService.getAccount(ID);
		if (!isValidSessionKey(account, SK)) {
			return new WrongSessionKeyResponse();
		}
		List<String> requestFriendList = friendService.getRequestFriendList(ID);
		boolean contains = requestFriendList.contains(FRIID);
		if (!contains) {
			jr.setRC(ReturnCode.UNKNOWN_ERR);
		}
		if (isacc) {
			boolean acceptFriendReq = friendService.acceptFriendReq(ID, FRIID);
			if (!acceptFriendReq) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);

			} else {
				{
					// tasktask
					List<TaskDetail> newTaskDetailList = taskController
							.newTaskDetailList(TASK_TYPE.ADD_FRIEND, 1);
					taskController.submitTaskDetailInfo(account.getAppId(),
							newTaskDetailList);
				}
				mailMapper.removeMail(mailKey);
			}
		} else {
			friendService.deleteFriend(ID, FRIID, 0);// 删除好友请求
		}

		Account acc = accountService.getAccount(ID);
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		jr.setGD(acc.getGold());
		jr.setBL(acc.getBall());
		jr.setPN(acc.getPunch());
		jr.setPNDT(acc.getPunchRemainDt(dateUtil.getNowEpoch()));

		return jr;
	}

	// @RequestMapping(value=URL_DELETE_FRIEND)
	// @ResponseBody
	public JsonResponse deleteFriend(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "FRIID", required = true) String FRIID) {
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		Account account = accountService.getAccount(ID);
		if (!isValidSessionKey(account, SK)) {
			return new WrongSessionKeyResponse();
		}
		friendService.deleteFriend(ID, FRIID, 1);
		return jr;
	}

	@RequestMapping(value = URL_AUTO_ADD_FRIEND)
	@ResponseBody
	public JsonResponse autoAddFriend(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "FRIIDS", required = true) String FRIIDS) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			List<String> qqfriendList = new ArrayList<String>();
			StringTokenizer strToken = new StringTokenizer(FRIIDS, ",");
			while (strToken.hasMoreElements()) {
				String _fri = (String) strToken.nextElement();
				qqfriendList.add(_fri);
			}
			System.out.println("########qqfriendList.size()####"
					+ qqfriendList.size());
			List<String> gamefriendList = friendlyMapper.selectFriendlyList(ID,
					1);
			for (String qqID : qqfriendList) {
				if (gamefriendList.indexOf(qqID) == -1) {
					Friendly friendly = new Friendly();
					friendly.setFri_self(ID);
					friendly.setFri_op(qqID);
					friendly.setStatus(1);
					friendlyMapper.insertFriendly(friendly);
				}
			}
			return jr;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;

		}
	}

}
