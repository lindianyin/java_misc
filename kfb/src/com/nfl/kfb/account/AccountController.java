/**
 * 
 */
package com.nfl.kfb.account;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import redis.clients.jedis.Jedis;
import sun.management.counter.Units;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.event.EventService;
import com.nfl.kfb.friend.FriendService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mail.MailService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.Account.LOGIN_TY;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.account.ActivityBoard;
import com.nfl.kfb.mapper.account.AdminChannel;
import com.nfl.kfb.mapper.account.BaseReward;
import com.nfl.kfb.mapper.account.FightActivityRankVo;
import com.nfl.kfb.mapper.account.Month_card;
import com.nfl.kfb.mapper.account.RoleReward;
import com.nfl.kfb.mapper.appver.Appver;
import com.nfl.kfb.mapper.dungeon.Dungeon;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.gamble.GambleProb;
import com.nfl.kfb.mapper.inv.Friendly;
import com.nfl.kfb.mapper.inv.FriendlyMapper;
import com.nfl.kfb.mapper.inv.Inv;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.AttendanceLog;
import com.nfl.kfb.mapper.logging.logs.GambleLog;
import com.nfl.kfb.mapper.logging.logs.GrankRewardLog;
import com.nfl.kfb.mapper.logging.logs.LoginLog;
import com.nfl.kfb.mapper.logging.logs.NewRegisterUserLog;
import com.nfl.kfb.mapper.logging.logs.ReviewLog;
import com.nfl.kfb.mapper.logging.logs.RewardLog;
import com.nfl.kfb.mapper.logging.logs.TutorialLog;
import com.nfl.kfb.mapper.mail.Mail;
import com.nfl.kfb.mapper.mail.MailMapper;
import com.nfl.kfb.mapper.rank.GateRank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.shop.ShopCurrency;
import com.nfl.kfb.mapper.shop.ShopMapper;
import com.nfl.kfb.mapper.week.WeekAchieve;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.play.PlayService;
import com.nfl.kfb.rank.RankService;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.ResourceService.GAME_OPTION;
import com.nfl.kfb.resource.WeekResource;
import com.nfl.kfb.shop.BuyResponse;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskService;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.REWARD_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 6. 20.
 * 
 */
@Controller
@RequestMapping(value = "/acc", method = { RequestMethod.POST,
		RequestMethod.GET })
public class AccountController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory
			.getLogger(AccountController.class);

	private static final String URL_KAKAO_TOKEN_CHECK = "/kakaotkn";
	private static final String URL_LOGIN = "/login";
	private static final String URL_PUSH_OPTION = "/optPush";
	private static final String URL_ACHIEVE_LIST = "/achList";
	private static final String URL_ACHIEVE_REWARD = "/achRw";
	private static final String URL_ATTENDANCE_REWARD = "/attRw";
	private static final String URL_TUTORIAL = "/tutoRw";
	private static final String URL_REVIEW = "/revRw";
	private static final String URL_WITHDRAW = "/withdraw";
	private static final String URL_REGSITER_PUSH_TOKEN = "/pushToken";
	private static final String URL_EVENT_DIRECT_RW = "/eventDirectRw";
	private static final String URL_GAMBLE = "/gamble";

	private static final String URL_CHANGENAME = "/changenickname";

	private static final String URL_GET_TUTO = "/gettuto";

	private static final String URL_UPDATE_TUTO = "/updatetuto";

	private static final String URL_GET_MONTH_CARD = "/getmonthcard";

	private static final String URL_GET_MONTH_CARD_REWARD = "/getmonthcardreward";

	private static final String URL_UPDATE_TAGS = "/updatetags";

	private static final String URL_GET_GLOBAL_RANK_REWARD = "/grankreward";

	private static final String URL_RECEIVE_GRANK_REWARD = "/receivegrankreward";

	private static final String URL_RECEIVE_UNLIMIT_RANK_REWARD = "/receiveunlimitrankreward";

	private static final String URL_GET_UNLIMIT_RANK_REWARD = "/unlimitrankreward";

	private static final String URL_ANYSDK_LOGIN = "/anysdklogin";

	private static final String URL_ACTIVITY_BOARD = "/acboard";

	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;

	@Autowired
	@Qualifier("FriendServiceImpl")
	private FriendService friendService;

	@Autowired
	@Qualifier("PlayServiceImpl")
	private PlayService playService;

	@Autowired
	@Qualifier("RankServiceImpl")
	private RankService rankService;

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	@Qualifier("MailServiceImpl")
	private MailService mailService;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	@Qualifier("EventServiceImpl")
	private EventService eventService;

	@Autowired
	private RankMapper rankMapper;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private TaskController taskController;

	@Autowired
	private GambleMapper gambleMapper;

	@Autowired
	private InvenMapper invenMapper;

	@Autowired
	@Qualifier("TaskServiceImpl")
	private TaskService taskService;

	@Autowired
	private ShopMapper shopMapper;

	@Autowired
	private FriendlyMapper friendlyMapper;

	@Autowired
	private MailMapper mailMapper;

	/**
	 * anysdk统一登录地址
	 */
	private String loginCheckUrl = "http://oauth.anysdk.com/api/User/LoginOauth/";

	/**
	 * connect time out
	 * 
	 * @var int
	 */
	private int connectTimeOut = 30 * 1000;

	/**
	 * time out second
	 * 
	 * @var int
	 */
	private int timeOut = 30 * 1000;

	/**
	 * user agent
	 * 
	 * @var string
	 */
	private static final String userAgent = "px v1.0";

	private static final String URL_GET_ACTIVITY_FIGHT_RANK = "/getactivityrank";

	private static final String URL_GET_GBP = "/getgbp";

	private static final String URL_ACTIVE_CODE = "/activecode";

	private static final String URL_CLIENT_INFO = "/clientinfo";

	private static final String URL_GENERATE_CODE = "/generate_ac_code";

	private static final String URL_GAME_VERSION = "/version";

	private static final String URL_IS_PAY_SUCCESS = "/ispaysuccess";

	private static final String URL_GET_NOTICE = "/notice";

	/**
	 * 检查登录合法性及返回sdk返回的用户id或部分用户信息
	 * 
	 * @param request
	 * @param response
	 * @return 验证合法 返回true 不合法返回 false
	 */
	public String check(HttpServletRequest request, HttpServletResponse response) {

		try {
			Map<String, String[]> params = request.getParameterMap();
			// 检测必要参数
			if (parametersIsset(params)) {
				// sendToClient( response, "parameter not complete" );
				return "parameter not complete";
			}

			String queryString = getQueryString(request);

			URL url = new URL(loginCheckUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", userAgent);
			conn.setReadTimeout(timeOut);
			conn.setConnectTimeout(connectTimeOut);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			OutputStream os = conn.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					os, "UTF-8"));
			writer.write(queryString);
			writer.flush();
			tryClose(writer);
			tryClose(os);
			conn.connect();

			InputStream is = conn.getInputStream();
			String result = stream2String(is);
			System.out.println("quyerString" + queryString);
			System.out.println("################result" + result);
			// sendToClient( response, result );
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		// sendToClient( response, "Unknown error!" );
		return "Unknown error!";
	}

	public void setLoginCheckUrl(String loginCheckUrl) {
		this.loginCheckUrl = loginCheckUrl;
	}

	/**
	 * 设置连接超时
	 * 
	 * @param connectTimeOut
	 */
	public void setConnectTimeOut(int connectTimeOut) {
		this.connectTimeOut = connectTimeOut;
	}

	/**
	 * 设置超时时间
	 * 
	 * @param timeOut
	 */
	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	/**
	 * check needed parameters isset 检查必须的参数 channel
	 * uapi_key：渠道提供给应用的app_id或app_key（标识应用的id）
	 * uapi_secret：渠道提供给应用的app_key或app_secret（支付签名使用的密钥）
	 * 
	 * @param params
	 * @return boolean
	 */
	private boolean parametersIsset(Map<String, String[]> params) {
		return !(params.containsKey("channel")
				&& params.containsKey("uapi_key") && params
					.containsKey("uapi_secret"));
	}

	/**
	 * 获取查询字符串
	 * 
	 * @param request
	 * @return
	 */
	private String getQueryString(HttpServletRequest request) {
		Map<String, String[]> params = request.getParameterMap();
		String queryString = "";
		for (String key : params.keySet()) {
			String[] values = params.get(key);
			for (int i = 0; i < values.length; i++) {
				String value = values[i];
				queryString += key + "=" + value + "&";
			}
		}
		queryString = queryString.substring(0, queryString.length() - 1);
		return queryString;
	}

	/**
	 * 获取流中的字符串
	 * 
	 * @param is
	 * @return
	 */
	private String stream2String(InputStream is) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new java.io.InputStreamReader(is));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tryClose(br);
		}
		return "";
	}

	/**
	 * 向客户端应答结果
	 * 
	 * @param response
	 * @param content
	 */
	private void sendToClient(HttpServletResponse response, String content) {
		response.setContentType("text/plain;charset=utf-8");
		try {
			PrintWriter writer = response.getWriter();
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭输出流
	 * 
	 * @param os
	 */
	private void tryClose(OutputStream os) {
		try {
			if (null != os) {
				os.close();
				os = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭writer
	 * 
	 * @param writer
	 */
	private void tryClose(java.io.Writer writer) {
		try {
			if (null != writer) {
				writer.close();
				writer = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭Reader
	 * 
	 * @param reader
	 */
	private void tryClose(java.io.Reader reader) {
		try {
			if (null != reader) {
				reader.close();
				reader = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = URL_ANYSDK_LOGIN)
	@ResponseBody
	public void login(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		// PrintWriter writer2 = response.getWriter();
		// writer2.write("hello");
		logger.info("#################################AnysdkLogin##");
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		String isSuccess = check(request, response);
		logger.info("###########################" + isSuccess);
		// PrintWriter writer = response.getWriter();
		// writer.write("hello world");
		// writer.close();

		ObjectMapper om = new ObjectMapper();
		JsonNode readTree = om.readTree(isSuccess);
		String asText = readTree.get("status").asText();
		if (!asText.equals("ok")) {
			return;
		}
		JsonNode jsonNode = readTree.get("data");
		String id = "";
		if (jsonNode.get("id") != null) {
			id = jsonNode.get("id").asText();
		}
		String name = "";
		if (jsonNode.get("name") != null) {
			name = jsonNode.get("name").asText();
		}

		String avatar = "";
		if (jsonNode.get("avatar") != null) {
			avatar = jsonNode.get("avatar").asText();
		}

		String nick = "";
		if (jsonNode.get("nick") != null) {
			nick = jsonNode.get("nick").asText();
		}

		JsonNode commonNode = readTree.get("common");
		String channel = commonNode.get("channel").asText();
		String user_sdk = commonNode.get("user_sdk").asText();
		String uid = commonNode.get("uid").asText();
		String server_id = commonNode.get("server_id").asText();

		if (channel == null || channel.equals("")) {
			System.out.println("channel为空");
			return;
		}

		if (user_sdk == null || user_sdk.equals("")) {
			System.out.println("user_sdk为空");
			return;
		}

		if (uid == null || uid.equals("")) {
			System.out.println("uid为空");
			return;
		}

		if (server_id == null || server_id.equals("")) {
			System.out.println("server_id为空");
			return;
		}

		HashMap<String, String> hashmap = new HashMap<String, String>();

		switch (channel) {
		case "000255": // UC
		case "000116":// 豌豆荚
		case "000066":// 小米
		case "000002":// 机锋
		case "000008":// 木蚂蚁
		case "000005":// 安智市场
			hashmap.put("id", uid);
			hashmap.put("name", (user_sdk + uid).substring(0));
			break;
		default:
			hashmap.put("id", id);
			hashmap.put("name", name);
			hashmap.put("avatar", avatar);
			hashmap.put("nick", nick);
			break;
		}

		String result = "";
		try {
			result = DebugOption.toJson(hashmap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String format = String.format("\"ext\":%s", result);
		String replaceFirst = isSuccess.replaceFirst("\"ext\":\"\"", format);
		System.out.println("replaceFirst" + replaceFirst);

		sendToClient(response, replaceFirst);
		return;
		// return jr;
	}

	@RequestMapping(value = URL_LOGIN)
	@ResponseBody
	public JsonResponse login(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "TY", required = true) String TY,
			@RequestParam(value = "VR", required = true) String VR,
			@RequestParam(value = "NOW", required = true) int NOW,
			@RequestParam(value = "MD5", required = true) String MD5,
			@RequestParam(value = "CCODE", required = false, defaultValue = "KR") String CCODE,
			@RequestParam(value = "BLOCK", required = false, defaultValue = "false") boolean BLOCK,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "DUNGEON", required = false, defaultValue = "false") boolean DUNGEON,
			@RequestParam(value = "NICKNAME", required = false, defaultValue = "游客") String NICKNAME,
			@RequestParam(value = "IMG", required = false) String IMG,
			@RequestParam(value = "accessToken", required = false) String accessToken,
			@RequestParam(value = "channelId", required = false, defaultValue = "999998") String channelId) {
		try {

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			logger.debug("longin----------------------------------------------------"
					+ dateUtil.getNow());
			System.out.println(IMG + "img");
			// NOW 시간이 5분이상 차이가 난다면 거부
			if (Math.abs(dateUtil.getNowEpoch() - NOW) > (60 * 5)) {
				logger.debug("Wrong NOW. RECV[{}]", NOW);
				// JsonResponse errorResponse = new
				// JsonResponse(ReturnCode.UNKNOWN_ERR);
				// errorResponse.setException(new
				// Exception("Login.NOW is not correct. Check now time"));
				// return errorResponse;
			}

			try {
				// System.out.println("ID:" + ID);
				logger.info("--------->ID:{}", ID);
				char[] IDCharArray = ID.toCharArray();
				if (IDCharArray.length == 0 || IDCharArray.length > 100) {
					throw new RuntimeException("Wrong ID Format. ID=" + ID);
				}

				// for (char IDChar : IDCharArray) {
				// // if (!Character.isLetter((IDChar))) {
				// // throw new RuntimeException("Wrong ID Format. ID=" + ID);
				// // }
				// }
			} catch (Exception e) {
				logger.error(ID, e);
				JsonResponse errorResponse = new JsonResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Wrong ID Format. ID="
						+ ID));
				return errorResponse;
			}

			// MD5 맞는지 확인
			if (!DebugOption.isValidLoginMD5(MD5, ID, NOW)) {
				// MD5 틀렸다
				JsonResponse errorResponse = new JsonResponse(
						ReturnCode.WRONG_MD5);
				return errorResponse;
			}

			LOGIN_TY loginType = LOGIN_TY.valueOf(TY);
			if (loginType == null) {
				logger.error("Unknown Login.TY=" + TY);
				JsonResponse errorResponse = new JsonResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Unknown Login.TY="
						+ TY));
				return errorResponse;
			}

			// 블럭여부 체크
			boolean isBlockAccount = accountService
					.isBlockAccount(dateUtil, ID);
			if (isBlockAccount) {
				JsonResponse errorResponse = new JsonResponse(
						BLOCK ? ReturnCode.BLOCK_USER : ReturnCode.UNKNOWN_ERR);
				return errorResponse;
			}

			LoginResponse loginResponse = new LoginResponse(ReturnCode.SUCCESS);
			loginResponse.setNOW(dateUtil.getNowEpoch());

			// 새로운 SessionKey 생성
			int sessionKey = generateNewSessionKeyForLogin(ID);
			loginResponse.setSK(sessionKey);

			// WEEK, 다음WEEK 시작시각
			int thisWeek = dateUtil.getThisWeek();
			loginResponse.setWK(thisWeek);
			loginResponse.setWKDT(dateUtil.getWeekStartEpoch(thisWeek + 1)
					- dateUtil.getNowEpoch());

			Account account = accountService.getAccount(ID);

			LoginLog loginLog = new LoginLog(dateUtil, ID);
			loginLog.setLoginType(loginType.getValueForLog());
			NewRegisterUserLog newRegisterUserLog = null;

			// 계정이 없을때는 계정 자동생성
			if (account == null) {
				newRegisterUserLog = new NewRegisterUserLog(dateUtil, ID);
				newRegisterUserLog.setLoginType(loginType.getValueForLog());
				logger.debug("account not found. ID={}, create new.", ID);

				try {
					account = accountService.createAccount(ID, sessionKey,
							dateUtil, TY, VR, CCODE, IMG);
					// boolean changeNickname =
					accountService.changeNickname(NICKNAME, ID);
					//
					// if(!changeNickname){
					// throw new Exception("昵称已经被注册");
					// }
					// accountMapper.updateNickname(NICKNAME + ID, ID);
					account = accountService.getAccount(ID);
					rankMapper.insertOrUpdateRankPoint(ID, thisWeek, 0, 0, 0);// 在总排行榜中插入最低排行

					rankMapper.insertOrUpdateUnlimitRank(ID, 0);

					// synchronized (RedisUtil.lock) {
					// RedisUtil.getgRedis().zrem(DebugOption.REDIS_RANK_KEY,
					// account.getAppId());
					// Jedis redis = RedisUtil.getRedis();
					Jedis redis = DebugOption.getJedisPool().getResource();
					redis.zadd(DebugOption.REDIS_RANK_KEY, 0,
							account.getAppId());
					// redis.close();
					// }

					redis.zadd(DebugOption.REDIS_UNLIMIT_RNANK_KEY, 0,
							account.getAppId());

					DebugOption.getJedisPool().returnResource(redis);

					logger.debug(ID + "插入总排行榜成功");

					gambleMapper.increaseGamblePoint(ID,
							DebugOption.DEFAULT_GAMBLE_POINT);

					logger.debug(ID + "初始化积分成功");

				} catch (Exception e) {
					logger.error("fail create account. ID:" + ID, e);
					JsonResponse errorResponse = new JsonResponse(
							ReturnCode.UNKNOWN_ERR);
					errorResponse.setException(new Exception(
							"failed to create new account. ID:" + ID));
					return errorResponse;
				}
			} else {
				// 새로운 세션키를 지정
				account.setSessionKey(sessionKey);

				// 접속으로 인한 계정정보 업데이트
				accountService.loginAccount(dateUtil, account, TY, VR, CCODE,
						loginLog);

				// 계정이 존재
				logger.debug("account found. ID={}, gold={}, ball={}",
						account.getAppId(), account.getGold(),
						account.getBall());

				// if(IMG != null &&account.getImg() != null &&
				// !account.getImg().equals(IMG)){
				accountMapper.updateAccountImg(IMG, ID);
				// accountMapper.updateNickname(NICKNAME, ID);
				// account.setNickname(NICKNAME);
				account.setImg(IMG);
				// }

			}

			// event : 로그인 이벤트 리스너
			try {
				List<GameLog> eventRewardLogs = eventService.eventLogin(
						dateUtil, account);
				if (eventRewardLogs != null && !eventRewardLogs.isEmpty()) {
					loggingService.insertGameLog(eventRewardLogs);
				}
			} catch (Exception e) {
				logger.error("ID=" + account.getAppId(), e);
			}

			// 인벤토리 정보
			List<Inven> allInven = accountService.getInven(ID);
			Map<String, Object> invenInfoMap = accountService
					.getInvenInfoMap(allInven);
			loginResponse.addInvenItemMap(invenInfoMap);

			loginResponse.setGD(account.getGold());
			loginResponse.setBL(account.getBall());

			// 주먹개수, 주먹 사용시각
			loginResponse.setPN(account.getPunch());
			loginResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			// 设置昵称
			loginResponse.setNickname(account.getNickname());

			logger.info("------------>nickname:{}", account.getNickname());

			loginResponse.setImg(account.getImg());

			// 겜블 포인트
			int gamblePoint = 0;
			if (GP) {
				gamblePoint = accountService.getGamblePoint(account.getAppId());
			}
			loginResponse.setGP(gamblePoint);

			// 던전 정보
			if (DUNGEON) {
				int dungeonLimit = Integer.parseInt(resourceService
						.getGameOption(GAME_OPTION.DUNGEON_DAILY_LIMIT));
				int dungeonPunch = Integer.parseInt(resourceService
						.getGameOption(GAME_OPTION.DUNGEON_REQ_PUNCH));

				Dungeon dungeon = accountService.getDungeon(account.getAppId());
				if (dungeon == null) {
					dungeon = new Dungeon();
					dungeon.setAppId(account.getAppId());
					dungeon.setPlayDt(dateUtil.getNowEpoch());
					dungeon.setPlayLimit(dungeonLimit);
					dungeon.setPlayCnt(0);
					dungeon.setPunch(dungeonPunch);
					accountService.insertDungeon(dungeon);
				} else {
					if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()
							|| dungeon.getPlayLimit() != dungeonLimit
							|| dungeon.getPunch() != dungeonPunch) {
						if (dungeon.getPlayDt() < dateUtil.getTodayStartEpoch()) {
							dungeon.setPlayDt(dateUtil.getNowEpoch());
							dungeon.setPlayCnt(0);
						}
						dungeon.setPlayLimit(dungeonLimit);
						dungeon.setPunch(dungeonPunch);
						accountService.updateDungeon(dungeon);
					}
				}
				loginResponse.setDungeon(dungeon.getPlayLimit(),
						dungeon.getPlayCnt(), dateUtil.getTomorrowStartEpoch()
								- dateUtil.getNowEpoch(), dungeon.getPunch());
			} else {
				loginResponse.setDungeon(0, 0, 0, 0);
			}

			// SHOP_VER
			int latestShopVer = Integer.parseInt(resourceService
					.getGameOption(GAME_OPTION.LATEST_SHOP_VER));
			loginResponse.setSHOPVER(latestShopVer);

			// 관문별 랭킹정보
			List<GateRank> gateRanks = rankService.getGateRank(ID, thisWeek);
			for (GateRank gateRank : gateRanks) {
				loginResponse.addG(gateRank.getGate(), gateRank.getPoint(),
						gateRank.getRwDt());
			}

			// 친구초대 정보
			Inv inv = accountService.getInv(account.getAppId());
			loginResponse.addINV(inv == null ? 0 : inv.getCnt(),
					inv == null ? new HashSet<Integer>() : inv.getRidxSet(),
					DebugOption.INV_CNT_INFO, DebugOption.INV_ITEM_INFO);

			// 앱버전 확인
			Appver appver = resourceService.getAppver(TY, VR);
			if (appver == null) {
				loginResponse.setVR(false);
				loginResponse.setPNABLE(true);
			} else {
				loginResponse.setVR(appver.isNeedUpdate());
				loginResponse.setPNABLE(appver.isAbleSendPunch());
			}

			// 출석보상 가능여부
			boolean isAttendance = accountService.isAttendance(
					account.getAttDt(), dateUtil);
			loginResponse.setATRW(isAttendance);

			// 튜토리얼 보상여부
			// loginResponse.setTUTO(account.getTuto() != 0);

			loginResponse.setTUTO(account.getTuto());

			// 리뷰보상 여부
			loginResponse.setREV(account.getRev() != 0);

			// 메세지함 개수
			int mailBoxCount = mailService.countMailBox(account.getAppId(),
					dateUtil.getNowEpoch());
			loginResponse.setMAILCNT(mailBoxCount);

			// 푸시 설정값
			loginResponse.setPUSH(account.getPush() != 0);
			// tags
			loginResponse.setTAGS(account.getTags());

			loginLog.setNowGold(account.getGold());
			loginLog.setNowBall(account.getBall());
			loginLog.setNowPunch(account.getPunch());

			if (newRegisterUserLog != null) {
				loggingService.insertGameLog(newRegisterUserLog);
			}
			loggingService.insertGameLog(loginLog);

			// 푸시토큰 table update
			accountService.updatePushToken(account);

			List<ShopCurrency> shopCurrencyList = shopMapper
					.selectShopCurrencyByStore(DebugOption.STORE_TYPE.ANYSDK
							.name());
			loginResponse.put("shopCurrency", shopCurrencyList);

			Jedis redis = DebugOption.getJedisPool().getResource();
			Double d1 = redis.zscore(DebugOption.REDIS_RANK_KEY, ID);
			if (d1 == null) {
				redis.zadd(DebugOption.REDIS_RANK_KEY, 0, account.getAppId());
				rankMapper.insertOrUpdateRankPoint(ID, thisWeek, 0, 0, 0);
			}

			Double d2 = redis.zscore(DebugOption.REDIS_UNLIMIT_RNANK_KEY, ID);
			if (d2 == null) {
				redis.zadd(DebugOption.REDIS_UNLIMIT_RNANK_KEY, 0,
						account.getAppId());
				rankMapper.insertOrUpdateUnlimitRank(ID, 0);
			}

			DebugOption.getJedisPool().returnResource(redis);

			// 自动添加腾讯好友
			if (accessToken != null && accessToken != "") {
				final String _ID = ID;
				final String _accessToken = accessToken;
				DebugOption.poolExecuteTask.execute(new Runnable() {
					@Override
					public void run() {
						addTencentFirend(_ID, _accessToken);
					}
				});
			}
			// 竞技场功能开放
			String opArena = resourceService
					.getGameOption(GAME_OPTION.OPEN_ARENA);
			if (opArena.equals("OPEN")) {
				loginResponse.setOpenArena(true);
			} else {
				loginResponse.setOpenArena(false);
			}
			// 插入或者更新渠道消息
			accountMapper.insertOrUpdateAccountChannel(ID, channelId);
			String str = DebugOption.toJson(loginResponse);
			logger.debug("##login_return####{}", str);
			return loginResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_KAKAO_TOKEN_CHECK)
	@ResponseBody
	/**
	 * 카카오 토큰 체크<br>
	 * @param ATKN
	 * @param UID
	 * @param SDKV
	 * @return
	 */
	public JsonResponse kakaoTokenCheck(
			@RequestParam(value = "ATKN", required = true) String ATKN,
			@RequestParam(value = "UID", required = true) String UID,
			@RequestParam(value = "SDKV", required = true) String SDKV) {
		try {
			KakaoTokenResponse kakaoTokenResponse = accountService
					.kakaoTokenCheck(ATKN, UID, SDKV);

			return kakaoTokenResponse;
		} catch (Exception e) {
			logger.error("kakaoTokenCheck", e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_PUSH_OPTION)
	@ResponseBody
	/**
	 * 푸시 옵션 변경<br>
	 * @param ID
	 * @param SK
	 * @param PUSH
	 * @return
	 */
	public JsonResponse setPushOption(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "PUSH", required = true) boolean PUSH) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			accountService.setPushOption(ID, PUSH);

			return new JsonResponse(ReturnCode.SUCCESS);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_TUTORIAL)
	@ResponseBody
	/**
	 * 튜토리얼 보상 요청<br>
	 * @param ID
	 * @param SK
	 * @return
	 */
	public JsonResponse rewardTutorial(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			// 이미 튜토리얼 보상을 받았음//以
			logger.info("------------->tuto:{}",account.getTuto());
			if (account.getTuto() != 0) {
				TutorialResponse response = new TutorialResponse(
						ReturnCode.UNKNOWN_ERR);
//				response.setException(new Exception(
//						"You'v got already tutorial reward item"));
				logger.info("You'v got already tutorial reward item");
				return response;
			}


			int rewardItemId = DebugOption.TUTORIAL_REWARD[0];
			int rewardItemCnt = DebugOption.TUTORIAL_REWARD[1];

			Shop item = resourceService.getShop(SHOPVER, rewardItemId);
			if (item == null) { // 보상 아이템을 찾을 수 없음
				TutorialResponse response = new TutorialResponse(
						ReturnCode.UNKNOWN_ERR);
				response.setException(new Exception(
						"Couln't find tutorial reward item. itemId="
								+ rewardItemId));
				return response;
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			TutorialLog tutorialLog = new TutorialLog(dateUtil, ID);

			TutorialResponse tutorialResponse = accountService.rewardTutorial(
					dateUtil, account, item, rewardItemCnt, tutorialLog);

			loggingService.insertGameLog(tutorialLog);

			return tutorialResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_REVIEW)
	@ResponseBody
	/**
	 * 리뷰 보상 요청<br>
	 * @param ID
	 * @param SK
	 * @return
	 */
	public JsonResponse rewardReview(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			// 이미 리뷰 보상을 받았음
			if (account.getRev() != 0) {
				ReviewResponse response = new ReviewResponse(
						ReturnCode.UNKNOWN_ERR);
				response.setException(new Exception(
						"You'v got already review reward item"));
				return response;
			}

			int rewardItemId = DebugOption.REVIEW_REWARD[0];
			int rewardItemCnt = DebugOption.REVIEW_REWARD[1];

			Shop item = resourceService.getShop(SHOPVER, rewardItemId);
			if (item == null) { // 보상 아이템을 찾을 수 없음
				ReviewResponse response = new ReviewResponse(
						ReturnCode.UNKNOWN_ERR);
				response.setException(new Exception(
						"Couln't find review reward item. itemId="
								+ rewardItemId));
				return response;
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			ReviewLog reviewLog = new ReviewLog(dateUtil, ID);

			ReviewResponse reviewResponse = accountService.rewardReview(
					dateUtil, account, item, rewardItemCnt, reviewLog);

			loggingService.insertGameLog(reviewLog);

			return reviewResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_ATTENDANCE_REWARD)
	@ResponseBody
	/**
	 * 출석보상//签到
	 * @param ID
	 * @param SK
	 * @param CHID
	 * @return
	 */
	public JsonResponse rewardAttendance(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			boolean isAttendance = accountService.isAttendance(
					account.getAttDt(), dateUtil);

			if (!isAttendance) { // 출석 아님
				AttendanceRewardResponse attendanceRewardResponse = new AttendanceRewardResponse(
						ReturnCode.SUCCESS);
				attendanceRewardResponse.setATDC(account.getAttCnt());
				attendanceRewardResponse.setATRW(false);
				attendanceRewardResponse.setATNRD(dateUtil
						.getTomorrowStartEpoch() - dateUtil.getNowEpoch());
				attendanceRewardResponse.setAT(DebugOption.ATTENDANCE_REWARD);
				attendanceRewardResponse.setADDGP(0);
				return attendanceRewardResponse;
			} else {
				AttendanceLog attendanceLog = new AttendanceLog(dateUtil,
						account.getAppId());

				int gamblePoint = 0;
				if (GP) {
					gamblePoint = DebugOption.GAMBLE_POINT_ATTENDANCE;
				}

				// 출석 보상 지급
				accountService.rewardAttendance(account, CHID, dateUtil,
						attendanceLog, gamblePoint, SHOPVER);

				loggingService.insertGameLog(attendanceLog);

				AttendanceRewardResponse attendanceRewardResponse = new AttendanceRewardResponse(
						ReturnCode.SUCCESS);
				attendanceRewardResponse.setATDC(account.getAttCnt());
				attendanceRewardResponse.setATRW(true);
				attendanceRewardResponse.setATNRD(dateUtil
						.getTomorrowStartEpoch() - dateUtil.getNowEpoch());
				attendanceRewardResponse.setAT(DebugOption.ATTENDANCE_REWARD);
				attendanceRewardResponse.setADDGP(gamblePoint);
				return attendanceRewardResponse;
			}
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_ACHIEVE_LIST)
	@ResponseBody
	/**
	 * 주간업적 리스트 요청 - 완료한 업적 IDX만<br>
	 * @param ID
	 * @param SK
	 * @param WK
	 * @return
	 */
	public JsonResponse getMyAchieve(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "WK", required = true) int WK) {
		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			int thisWeek = dateUtil.getThisWeek();

			if (WK != thisWeek) {
				// 주가 바꼈음
				AchieveListResponse achieveListResponse = new AchieveListResponse(
						ReturnCode.WRONG_WEEK);
				achieveListResponse.setWK(thisWeek);
				return achieveListResponse;
			}

			AchieveListResponse achieveListResponse = new AchieveListResponse(
					ReturnCode.SUCCESS);

			List<Integer> achieveList = accountService.getAchieveList(ID,
					thisWeek);

			achieveListResponse.setAIDX(achieveList);

			return achieveListResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_ACHIEVE_REWARD)
	@ResponseBody
	/**
	 * 주간 업적 보상 요청<br>
	 * @param ID
	 * @param SK
	 * @param WK
	 * @param AID
	 * @return
	 */
	public JsonResponse rewardAchieve(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "WK", required = true) int WK,
			@RequestParam(value = "AIDX", required = true) int AIDX,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			int thisWeek = dateUtil.getThisWeek();

			if (WK != thisWeek) {
				// WEEK가 안맞음
				AchieveRewardResponse achieveRewardResponse = new AchieveRewardResponse(
						ReturnCode.WRONG_WEEK);
				achieveRewardResponse.setWK(thisWeek);
				achieveRewardResponse.setGOT(true);
				achieveRewardResponse.setITID(0);
				achieveRewardResponse.setITCNT(0);
				achieveRewardResponse.setGD(account.getGold());
				achieveRewardResponse.setBL(account.getBall());
				achieveRewardResponse.setPN(account.getPunch());
				achieveRewardResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				return achieveRewardResponse;
			}

			WeekResource weekResource = resourceService
					.getWeekResource(thisWeek);

			WeekAchieve weekAchieve = weekResource.getWeekAchieve(AIDX);

			// 없는 AIDX
			if (weekAchieve == null) {
				AchieveRewardResponse achieveRewardResponse = new AchieveRewardResponse(
						ReturnCode.UNKNOWN_ERR);
				achieveRewardResponse.setException(new Exception(
						"Not found AIDX"));
				achieveRewardResponse.setWK(thisWeek);
				achieveRewardResponse.setGOT(true);
				achieveRewardResponse.setITID(0);
				achieveRewardResponse.setITCNT(0);
				achieveRewardResponse.setGD(account.getGold());
				achieveRewardResponse.setBL(account.getBall());
				achieveRewardResponse.setPN(account.getPunch());
				achieveRewardResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				return achieveRewardResponse;
			}

			AchieveLog achieveLog = new AchieveLog(dateUtil, account.getAppId());
			achieveLog.setAchIdx(AIDX);

			try {
				Shop rewardItem = null;
				int rewardValue = 0;

				if (weekAchieve.getRewardItemId() > 0) {
					rewardItem = resourceService.getShop(SHOPVER,
							weekAchieve.getRewardItemId());
					rewardValue = weekAchieve.getRewardItemCnt();
				}

				AchieveRewardResponse achieveRewardResponse = accountService
						.rewardAchieve(dateUtil, account, WK, AIDX, CHID,
								rewardItem, rewardValue, achieveLog);

				loggingService.insertGameLog(achieveLog);

				return achieveRewardResponse;
			} catch (DuplicateKeyException e) {
				// 보상을 이미 받았었다
				AchieveRewardResponse achieveRewardErrResponse = new AchieveRewardResponse(
						ReturnCode.SUCCESS);
				achieveRewardErrResponse.setException(new Exception(
						"You got achievement reward already"));
				achieveRewardErrResponse.setWK(thisWeek);
				achieveRewardErrResponse.setGOT(true);
				achieveRewardErrResponse.setITID(0);
				achieveRewardErrResponse.setITCNT(0);
				achieveRewardErrResponse.setGD(account.getGold());
				achieveRewardErrResponse.setBL(account.getBall());
				achieveRewardErrResponse.setPN(account.getPunch());
				achieveRewardErrResponse.setPNDT(account
						.getPunchRemainDt(dateUtil.getNowEpoch()));
				return achieveRewardErrResponse;
			}
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_WITHDRAW)
	@ResponseBody
	/**
	 * 탈퇴 
	 * @param ID
	 * @param SK
	 * @param NOW
	 * @param MD5
	 * @return
	 */
	public JsonResponse withdraw(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "NOW", required = true) int NOW,
			@RequestParam(value = "MD5", required = true) String MD5) {
		try {
			// DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			//
			// // NOW 시간이 5분이상 차이가 난다면 거부
			// if (Math.abs(dateUtil.getNowEpoch() - NOW) > (60 * 5)) {
			// logger.debug("Wrong NOW. RECV[{}]", NOW);
			// // JsonResponse errorResponse = new
			// // JsonResponse(ReturnCode.UNKNOWN_ERR);
			// // errorResponse.setException(new
			// // Exception("Withdraw.NOW is not correct. Check now time"));
			// // return errorResponse;
			// }
			//
			// Account account = accountService.getAccount(ID);
			//
			// if (!isValidSessionKey(account, SK)) {
			// return new WrongSessionKeyResponse();
			// }
			//
			// // MD5 맞는지 확인
			// if (!DebugOption.isValidWithdrawMD5(MD5, ID, NOW, SK)) {
			// // MD5 틀렸다
			// JsonResponse errorResponse = new JsonResponse(
			// ReturnCode.WRONG_MD5);
			// return errorResponse;
			// }
			//
			// WithdrawLog withdrawLog = new WithdrawLog(dateUtil,
			// account.getAppId());
			// withdrawLog.setNowGold(account.getGold());
			// withdrawLog.setNowBall(account.getBall());
			//
			// accountService.withdraw(dateUtil, account.getAppId());
			//
			// loggingService.insertGameLog(withdrawLog);

			return new JsonResponse(ReturnCode.SUCCESS);

		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_REGSITER_PUSH_TOKEN)
	@ResponseBody
	/**
	 * 푸시 토큰(등록ID) 등록
	 * @param ID
	 * @param TY
	 * @param VR
	 * @param CCODE
	 * @param TOKEN
	 * @return
	 */
	public JsonResponse registerPushToken(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "TY", required = true) String TY,
			@RequestParam(value = "VR", required = true) String VR,
			@RequestParam(value = "CCODE", required = false, defaultValue = "KR") String CCODE,
			@RequestParam(value = "TOKEN", required = true) String TOKEN) {
		try {
			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			accountService.registerPushToken(ID, TY, VR, CCODE, TOKEN,
					dateUtil.getNowEpoch());

			return new JsonResponse(ReturnCode.SUCCESS);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_EVENT_DIRECT_RW)
	@ResponseBody
	/**
	 * 이벤트 보상 요청. 인벤토리에 직접 추가<br>
	 * @param ID
	 * @param SK
	 * @param EIDX
	 * @return
	 */
	public JsonResponse rewardEventDirect(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "EIDX", required = true) int EIDX) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			// event : 이벤트 보상 리스너
			List<GameLog> eventRewardLogs = new ArrayList<GameLog>();

			EventDirectRwResponse directRwResponse = eventService
					.eventDirectRw(dateUtil, account, EIDX, eventRewardLogs);
			if (eventRewardLogs != null && !eventRewardLogs.isEmpty()) {
				loggingService.insertGameLog(eventRewardLogs);
			}

			return directRwResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GAMBLE)
	@ResponseBody
	/**
	 * 뽑기
	 * @param ID
	 * @param SK
	 * @param CHID
	 * @return
	 */
	public JsonResponse gamble(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			List<GambleProb> gambleProbList = new ArrayList<GambleProb>();
			gambleProbList.addAll(resourceService.getGambleProb());

			GambleLog gambleLog = new GambleLog(dateUtil, account.getAppId());

			GambleResponse gambleResponse = accountService.gamble(dateUtil,
					account, CHID, gambleProbList, gambleLog, SHOPVER);

			loggingService.insertGameLog(gambleLog);

			return gambleResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_CHANGENAME)
	@ResponseBody
	public JsonResponse changeNickname(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "NICKNAME", required = true) String NICKNAME) {

		try {
			logger.info("---------->changeNickname ID:{},SK:{},NICKNAME:{}",
					ID, SK, NICKNAME);
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			if (NICKNAME == null || NICKNAME == "" || NICKNAME.length() > 50) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			boolean isSuccess = accountService.changeNickname(NICKNAME, ID);
			if (!isSuccess) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
			}
			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GET_TUTO)
	@ResponseBody
	public JsonResponse getTuto(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {

		try {
			Account account = accountService.getAccount(ID);

			// if (!isValidSessionKey(account, SK)) {
			// return new WrongSessionKeyResponse();
			// }
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			int tuto = account.getTuto();
			jr.put("TUTO", tuto);
			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_UPDATE_TUTO)
	@ResponseBody
	public JsonResponse updateTuto(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "TUTO", required = true) int TUTO) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			account.setTuto(TUTO);
			accountMapper.updateTUTO(ID, TUTO);

			if (TUTO > account.getPlayGate()) {
				accountMapper.updatePlayGate(account.getAppId(), TUTO);
			}
			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GET_MONTH_CARD)
	@ResponseBody
	public JsonResponse getMonthCard(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);

			int lastCount = accountMapper.countOfMonthCardNotGetReward(ID);
			Month_card today = accountMapper
					.selectTodayMonthCardByAppId(account.getAppId());
			int state = -1;
			if (today == null) {
				state = 0;// 0不可领取
			} else {
				if (today.getState() == 0) {
					state = 1;// 1未领取
				} else {
					state = 2;// 2已经领取
				}

			}

			jr.put("todayState", state);
			jr.put("lastCount", lastCount);
			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@Transactional
	@RequestMapping(value = URL_GET_MONTH_CARD_REWARD)
	@ResponseBody
	public JsonResponse getMonthCardReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			BuyResponse buyResponse = new BuyResponse(ReturnCode.SUCCESS);
			Month_card today = accountMapper
					.selectTodayMonthCardByAppId(account.getAppId());
			if (today == null || today.getState() == 1) {
				buyResponse.setRC(ReturnCode.UNKNOWN_ERR);
				return buyResponse;
			}
			account.setBall(account.getBall() + DebugOption.MONTH_CARD_BALL);
			accountMapper.updateAccountItem(account);
			accountMapper.updateTodayMonthCardStateByAppid(account.getAppId());

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			buyResponse.setITID(0);
			buyResponse.setITCNT(0);
			buyResponse.setGD(account.getGold());
			buyResponse.setBL(account.getBall());
			buyResponse.setPN(account.getPunch());
			buyResponse
					.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
			buyResponse.setADDGP(0);
			return buyResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_UPDATE_TAGS)
	@ResponseBody
	public JsonResponse updateTags(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "TAGS", required = true) String TAGS) {

		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			// Month_card today =
			// accountMapper.selectTodayMonthCardByAppId(account.getAppId());
			// if(today == null){
			// jr.setRC(ReturnCode.UNKNOWN_ERR);
			// return jr;
			// }
			// System.out.println(today.getId());
			// account.setBall(account.getBall()+DebugOption.MONTH_CARD_BALL);
			// accountMapper.updateAccountItem(account);
			// accountMapper.updateTodayMonthCardStateByAppid(account.getAppId());
			// jr.put("ball", account.getBall());

			accountMapper.updateAccountTags(TAGS, ID);

			return jr;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	public void AddMonthCard(String appId) {
		// accountMapper.deleteMonthCardByAppid(appId);
		Date maxDate = accountMapper.selectMaxMonthCardDate(appId);
		if (maxDate == null) {
			maxDate = new Date();
		}
		Date dateNow = new Date();
		boolean isSameday = DebugOption.isSameDay(maxDate, dateNow);

		Date start = null;
		if ((maxDate.before(dateNow) && !isSameday) || maxDate == null) {
			start = dateNow;
		} else {
			long epochMilli = maxDate.toInstant()
					.plusMillis(24 * 60 * 60 * 1000).toEpochMilli();
			Date date = new Date(epochMilli);
			start = date;
		}

		// Instant instant = Instant.now();
		Instant instant = start.toInstant();
		for (int i = 0; i < 30; i++) {
			Date date = new Date(instant.toEpochMilli());
			Month_card mc = new Month_card();
			mc.setAppId(appId);
			mc.setGet_time(date);
			mc.setState(0);
			accountMapper.insertMonthCard(mc);
			instant = instant.plusMillis(24 * 60 * 60 * 1000);
		}

	}

	public void giveReward(Account account, String reward) {
		StringTokenizer st = new StringTokenizer(reward, ",");
		List<String> list = new ArrayList<String>();
		while (st.hasMoreElements()) {
			String nextElement = (String) st.nextElement();
			list.add(nextElement);
		}
		HashMap<Integer, Integer> rewardmap = new HashMap<Integer, Integer>();
		for (int i = 0; i < list.size(); i += 2) {
			int itemId = Integer.parseInt(list.get(i));
			int itemCount = Integer.parseInt(list.get(i + 1));
			rewardmap.put(itemId, itemCount);
		}
		if (rewardmap.size() > 0) {
			// 给奖励
			final DateUtil dt = new DateUtil(System.currentTimeMillis());
			Iterator<Entry<Integer, Integer>> iterator = rewardmap.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Integer> next = iterator.next();
				Shop rewardItem = resourceService.getShop(0, next.getKey());
				int rewardValue = next.getValue();
				int chId = 0;
				Inven selectMaxLvPet = invenMapper.selectMaxLvPet(account
						.getAppId());
				if (selectMaxLvPet != null) {
					chId = selectMaxLvPet.getChId();
				}

				RewardLog log = new RewardLog(dt, account.getAppId());
				taskService.giveItem(rewardItem, account, rewardValue, log,
						chId);
				loggingService.insertGameLog(log);
			}
		}

	}

	@RequestMapping(value = URL_GET_GLOBAL_RANK_REWARD)
	@ResponseBody
	public JsonResponse getGlobalRankReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {

		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			int rewardType = REWARD_TYPE.YESTERDAY_GLOBAL_REWARD.getValue();
			long rank = accountMapper.selectRankOfGamblePointBak(ID);
			// System.out.println(rank);
			return getRankRewardDetail(ID, rewardType, rank);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GET_UNLIMIT_RANK_REWARD)
	@ResponseBody
	public JsonResponse getUnlimitRankReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {

		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			int rewardType = REWARD_TYPE.UNLIMIT_RANK_REWARD.getValue();
			// int rank = accountMapper.selectRankOfGamblePointBak(ID);
			Jedis redis = DebugOption.getJedisPool().getResource();
			// Double zscore = redis.zscore(key, ID);
			Long zrank = redis.zrevrank(DebugOption.REDIS_UNLIMIT_RNANK_KEY, ID);
			// long longValue = zscore.longValue();
			DebugOption.getJedisPool().returnResource(redis);

			return getRankRewardDetail(ID, rewardType, zrank);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	public JsonResponse getRankRewardDetail(String ID, int rewardType, long rank) {
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		AddRankRewardToResponse(ID, rewardType, rank + 1, jr);

		return jr;
	}

	public void AddRankRewardToResponse(String ID, int rewardType, long rank,
			JsonResponse jr) {
		List<BaseReward> baseRewardList = accountMapper
				.selectBaseRewardByType(rewardType);
		String reward = null;
		BaseReward baseReward = null;
		Date now = new Date();
		// System.out.println(now);
		// System.out.println(DebugOption.OPEN_SERVER_TIME);
		if (!DebugOption.isSameDay(now, DebugOption.OPEN_SERVER_TIME)) {
			for (int i = 0; i < baseRewardList.size(); i++) {
				BaseReward br = baseRewardList.get(i);
				List<String> splitString = DebugOption.splitString(
						br.getCondition(), ",");
				long low = Long.parseLong(splitString.get(0));
				long high = Long.parseLong(splitString.get(1));
				if (low <= rank && rank <= high) {
					reward = br.getReward();
					baseReward = br;
					break;
				}
			}
		}

		jr.put("rank", rank);
		jr.put("reward", reward);
		RoleReward rr = accountMapper.selectRoleReward(ID, rewardType);
		if (rr == null || baseReward == null) {
			jr.put("isgetreward", false);
		} else {
			jr.put("isgetreward", true);
		}

		jr.put("rank", rank);
		jr.put("basereward", baseReward);
	}

	@RequestMapping(value = URL_RECEIVE_UNLIMIT_RANK_REWARD)
	@ResponseBody
	public JsonResponse receiveUnlimitRankReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "RANK", required = true) int RANK) {

		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			int rewardType = REWARD_TYPE.UNLIMIT_RANK_REWARD.getValue();
			// int rank = accountMapper.selectRankOfGamblePointBak(ID);
			// int rank = RANK;
			Jedis redis = DebugOption.getJedisPool().getResource();
			// Double zscore = redis.zscore(key, ID);
			Long zrank = redis.zrevrank(DebugOption.REDIS_UNLIMIT_RNANK_KEY, ID);
			
			logger.info("zrank:{}",zrank);
			
			
			
			DebugOption.getJedisPool().returnResource(redis);

			return giveGlobalRankAndUnlimitRankReward(ID, account, rewardType,
					zrank);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_RECEIVE_GRANK_REWARD)
	@ResponseBody
	public JsonResponse receiveGlobalRankReward(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "RANK", required = true) int RANK) {

		try {
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			int rewardType = REWARD_TYPE.YESTERDAY_GLOBAL_REWARD.getValue();
			long rank = accountMapper.selectRankOfGamblePointBak(ID);

			return giveGlobalRankAndUnlimitRankReward(ID, account, rewardType,
					rank);
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GET_ACTIVITY_FIGHT_RANK)
	@ResponseBody
	public JsonResponse getFightActivityRank(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "TYPE", required = true) int type) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			List<FightActivityRankVo> list = null;
			if (type == 1) {
				list = accountMapper.selectFightActivityRank();
			} else if (type == 2) {
				list = accountMapper.selectFightActivityRank_1();
			}

			int i = 1;
			boolean isContain = false;
			for (FightActivityRankVo fa : list) {
				String img = fa.getImg();
				if (img != null && !img.equals("")) {
					int indexOf = img.indexOf(",");
					if (indexOf != -1) {
						img = img.substring(0, indexOf);
						fa.setImg(img);
					}
				}
				fa.rank = i++;

				if (!isContain) {
					if (ID.equals(fa.getAppId())) {
						isContain = true;
					}
				}
			}
			if (!isContain) {
				FightActivityRankVo self = null;
				if (type == 1) {
					self = accountMapper.selectSelfFightActivityRank(ID);
				} else if (type == 2) {
					self = accountMapper.selectSelfFightActivityRank_1(ID);
				}

				if (self != null) {
					self.rank = -1;

				} else {
					self = new FightActivityRankVo();
					self.setAppId(account.getAppId());
					self.setImg(account.getImg());
					self.setNickname(account.getNickname());
					self.setPoint(0);
					self.rank = -1;
				}
				list.add(self);
			}

			jr.put("rank", list);

			// ObjectMapper objMapper = new ObjectMapper();
			// String writeValueAsString = objMapper.writeValueAsString(jr);
			// response.setHeader("Access-Control-Allow-Origin", "*");
			// response.setContentType("text/html;charset=UTF-8");

			// PrintWriter out = response.getWriter();
			// out.write(writeValueAsString);
			return jr;
		} catch (Exception e) {

		}
		return null;
	}

	@RequestMapping(value = URL_GET_GBP)
	@ResponseBody
	public JsonResponse getGoldBallPunch(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			logger.info("--------------------->get the gold,ball and punch!!");
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			jr.put("GOLD", account.getGold());
			jr.put("BALL", account.getBall());
			jr.put("PUNCH", account.getPunch());
			logger.info("--------------------->GOLD:{},BALL:{},PUNCH:{}",
					account.getGold(), account.getBall(), account.getPunch());
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_ACTIVE_CODE)
	@ResponseBody
	public JsonResponse getActiveCode(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CODE", required = true) String code) {
		try {
			code = code.trim();
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			code = code.replaceAll("\n", "");
			ActiveCode parseActive = ActiveCode.getInstance().parseActive(code);
			if (parseActive.isTimeOut()) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				jr.put("ERR", "激活码已经过期");
				return jr;
			}

			if (!parseActive.isMultiServer()) {
				String gameOption = resourceService
						.getGameOption(GAME_OPTION.ACTIVE_NAME);

				StringTokenizer tokenizer = new StringTokenizer(gameOption, ",");
				ArrayList<String> _l = new ArrayList<String>();
				while (tokenizer.hasMoreElements()) {
					_l.add((String) tokenizer.nextElement());
				}
				int count = 0;
				for (int i = 0; i < _l.size(); i++) {
					String string = _l.get(i);
					short parseShort = Short.parseShort(string);
					if (!parseActive.isValidName(parseShort)) {
						count++;
					}
				}
				if (count == _l.size()) {// 数据库都不包含提交过来的渠道
					jr.setRC(ReturnCode.UNKNOWN_ERR);
					jr.put("ERR", "激活码只能在对应的平台上使用");
					return jr;
				}
			}

			if (accountMapper.countOfActiveCode(code) > 0) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				jr.put("ERR", "激活码已经使用过了");
				return jr;
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			short[] items = parseActive.getItems();

			for (int i = 0; i < items.length; i += 2) {
				Mail mail = new Mail();
				mail.setSender(Mail.SENDER_ID_ADMIN);
				mail.setOwner(account.getAppId());
				mail.setItem(items[i]);
				mail.setCnt(items[i + 1]);
				mail.setDelDt(dateUtil.getNowEpoch() + DateUtil.ONE_DAY_EPOCH
						* 3);
				mail.setMsg("激活码物品");
				mailMapper.insertMail(mail);
			}

			accountMapper.insertActiveCode(account.getAppId(), code);

			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			errorResponse.put("ERR", "领取激活码出错");
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GENERATE_CODE)
	@ResponseBody
	public JsonResponse generateActiveCode(
			@RequestParam(value = "name", required = true) short name,
			@RequestParam(value = "start_date", required = true) String start_date,
			@RequestParam(value = "hour", required = true) short hour,
			@RequestParam(value = "ismulti", required = true) boolean ismulti,
			@RequestParam(value = "items", required = true) String items,
			@RequestParam(value = "id", required = true) int id,
			@RequestParam(value = "count", required = true) int count) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");
			Date startDate = format.parse(start_date);
			StringTokenizer strToken = new StringTokenizer(items, ",");
			List<Short> _list = new ArrayList<Short>();
			while (strToken.hasMoreElements()) {
				String cur = (String) strToken.nextElement();
				Short curShort = Short.parseShort(cur);
				_list.add(curShort);
			}
			short[] _list1 = new short[_list.size()];
			for (int i = 0; i < _list1.length; i++) {
				_list1[i] = _list.get(i);
			}

			List<String> _list2 = new ArrayList<String>();
			int _id = id;
			for (int i = 0; i < count; i++) {
				ActiveCode activeCode = ActiveCode.getInstance()
						.getActivityCode(name, startDate, hour, ismulti,
								_list1, _id);
				String strAc = activeCode.getActivityCode();
				_list2.add(strAc);
				_id++;
			}
			jr.put("codes", _list2);
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			errorResponse.put("ERR", "领取激活码出错");
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_ACTIVITY_BOARD)
	@ResponseBody
	public JsonResponse getActivityBoard(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			List<ActivityBoard> list = accountMapper.selectActivityBoard();
			jr.put("acboardlist", list);
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_CLIENT_INFO)
	@ResponseBody
	public JsonResponse addClientInfo(
			@RequestParam(value = "MAC", required = true) String mac,
			@RequestParam(value = "INFO", required = true) String info,
			@RequestParam(value = "UID", required = true) String uid) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			accountMapper.insertClientInfo(mac, info, uid);
			// throw new Exception("1111111");
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_GAME_VERSION)
	@ResponseBody
	public JsonResponse getGameVersion(
			@RequestParam(value = "NUMBER", required = true) String number) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			// accountMapper.insertClientInfo(mac, info,uid);
			// throw new Exception("1111111");
			// String gameVersion = resourceService
			// .getGameOption(GAME_OPTION.GAME_VERSION);
			// String gameDownloadUrl = resourceService
			// .getGameOption(GAME_OPTION.GAME_DOWNLOAD_URL);
			logger.info("------------------->NUMBER:{}", number);
			AdminChannel ac = accountMapper.selectAdminChannel(number);
			jr.put("version", ac.getVersion());
			jr.put("url", ac.getUrl());
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_IS_PAY_SUCCESS)
	@ResponseBody
	public JsonResponse isPaySuccess(
			@RequestParam(value = "NUMBER", required = true) String number) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			// accountMapper.insertClientInfo(mac, info,uid);
			// throw new Exception("1111111");
			// String gameVersion = resourceService
			// .getGameOption(GAME_OPTION.GAME_VERSION);
			// String gameDownloadUrl = resourceService
			// .getGameOption(GAME_OPTION.GAME_DOWNLOAD_URL);
			AdminChannel ac = accountMapper.selectAdminChannel(number);
			jr.put("version", ac.getVersion());
			jr.put("url", ac.getUrl());
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}
	
	
	@RequestMapping(value = URL_GET_NOTICE)
	@ResponseBody
	public JsonResponse getNotice(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Account account = accountService.getAccount(ID);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			List<Notice> selectNotice = accountMapper.selectNotice();
			List<Notice> result = new ArrayList<Notice>();
			for(Notice notice : selectNotice){
				if(notice.getIsMultiple() == 1){
					result.add(notice);
				}else{
					int count = accountMapper.countOfAccountNotice(ID, notice.getId());
					if(count == 0){
						result.add(notice);
						accountMapper.insertOrUpdateAccountNotice(ID, notice.getId());
					}
				}
			}
			jr.put("notice", result);
			return jr;
		} catch (Exception e) {
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}
	
	
	
	

	public JsonResponse giveGlobalRankAndUnlimitRankReward(String ID,
			Account account, int rewardType, long rank) {
		//logger.info("before gold:{},ball:{}",account.getGold(),account.getBall());
		BuyResponse jr = new BuyResponse(ReturnCode.SUCCESS);

		RoleReward rrr = accountMapper.selectRoleReward(ID, rewardType);
		if (rrr != null) {
			jr.setRC(ReturnCode.UNKNOWN_ERR);
			jr.put("error", "today haved get reward");
			return jr;
		}
		List<BaseReward> baseRewardList = accountMapper
				.selectBaseRewardByType(rewardType);
		BaseReward baseReward = null;
		for (int i = 0; i < baseRewardList.size(); i++) {
			BaseReward br = baseRewardList.get(i);
			List<String> splitString = DebugOption.splitString(
					br.getCondition(), ",");
			long low = Long.parseLong(splitString.get(0));
			long high = Long.parseLong(splitString.get(1));
			if (low <= rank && rank <= high) {
				baseReward = br;
				break;
			}
		}
		boolean isSuccess = giveBaseReward(account, baseReward);
		if (isSuccess) {
			RoleReward rr = new RoleReward();
			rr.setGettime(new Date());
			rr.setType(rewardType);
			rr.setAppId(account.getAppId());
			accountMapper.insertRoleReward(rr);
		} else {
			jr.setRC(ReturnCode.UNKNOWN_ERR);
		}

		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		jr.setITID(0);
		jr.setITCNT(0);
		jr.setGD(account.getGold());
		jr.setBL(account.getBall());
		jr.setPN(account.getPunch());
		jr.setPNDT(account.getPunchRemainDt(dateUtil.getNowEpoch()));
		jr.setADDGP(0);
		//logger.info("after gold:{},ball:{}",account.getGold(),account.getBall());
		return jr;
	}

	// 给奖励
	public boolean giveBaseReward(Account account, BaseReward baseReward) {
		List<String> splitString = DebugOption.splitString(
				baseReward.getReward(), ",");
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < splitString.size(); i++) {
			String string = splitString.get(i);
			int parseInt = Integer.parseInt(string);
			list.add(parseInt);
		}
		HashMap<Integer, Integer> rewardmap = new HashMap<Integer, Integer>();
		for (int i = 0; i < list.size(); i += 2) {
			rewardmap.put(list.get(i), list.get(i + 1));
		}
		if (rewardmap.size() > 0) {
			// 给奖励
			final DateUtil dt = new DateUtil(System.currentTimeMillis());
			Iterator<Entry<Integer, Integer>> iterator = rewardmap.entrySet()
					.iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Integer> next = iterator.next();
				Shop rewardItem = resourceService.getShop(0, next.getKey());
				int rewardValue = next.getValue();
				int chId = 0;
				Inven selectMaxLvPet = invenMapper.selectMaxLvPet(account
						.getAppId());
				if (selectMaxLvPet != null) {
					chId = selectMaxLvPet.getChId();
				}
				GrankRewardLog achieveLog = new GrankRewardLog(dt,
						account.getAppId());
				taskService.giveItem(rewardItem, account, rewardValue,
						achieveLog, chId);
				loggingService.insertGameLog(achieveLog);
			}

		} else {
			return false;
		}
		return true;
	}

	public void addTencentFirend(String ID, String accessToken) {

		try {
			// ID = ID.substring(6);
			if (ID.startsWith("999998")) {
				ID = ID.substring(6);
			}
			GetMethod get = new GetMethod(
					"https://graph.qq.com/user/get_app_friends");
			NameValuePair[] param = new NameValuePair[4];
			param[0] = new NameValuePair("access_token", accessToken);
			param[1] = new NameValuePair("oauth_consumer_key", "1102962464");
			param[2] = new NameValuePair("openid", ID);
			param[3] = new NameValuePair("format", "json");
			get.setQueryString(param);
			HttpClient client = new HttpClient();
			int returnCode = client.executeMethod(get);
			if (returnCode != 200) {
				get.releaseConnection();
				return;
			}
			String responseBodyAsString = get.getResponseBodyAsString();
			get.releaseConnection();
			// System.out.println("=====================" +
			// responseBodyAsString);

			ObjectMapper objMapper = new ObjectMapper();
			JsonNode readTree = objMapper.readTree(responseBodyAsString);
			int asInt = readTree.get("ret").asInt();
			List<String> qqfriendList = new ArrayList<String>();
			if (asInt == 0) {
				Iterator<JsonNode> elements = readTree.get("items").elements();
				while (elements.hasNext()) {
					JsonNode next = elements.next();
					String strOpenId = next.get("openid").asText();
					qqfriendList.add(strOpenId);
				}
			}

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

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
