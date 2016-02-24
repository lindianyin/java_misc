/**
 * 
 */
package com.nfl.kfb.shop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONArray;
import org.json.JSONObject;
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

import com.alipay.util.AlipayNotify;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.account.AccountController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.account.AccountMapper;
import com.nfl.kfb.mapper.account.RoleReward;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.CurrencyLog;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.GameLog.GAMELOG_TYPE;
import com.nfl.kfb.mapper.logging.logs.BuyItemLog;
import com.nfl.kfb.mapper.logging.logs.BuyMonthCardGameLog;
import com.nfl.kfb.mapper.logging.logs.CurrencyGameLog;
import com.nfl.kfb.mapper.rank.Rank;
import com.nfl.kfb.mapper.rank.RankMapper;
import com.nfl.kfb.mapper.shop.Anysdkpay;
import com.nfl.kfb.mapper.shop.AnysdkpayMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.shop.ShopCurrency;
import com.nfl.kfb.mapper.shop.ShopMapper;
import com.nfl.kfb.mapper.task.BaseTaskMapper;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.rank.RankService;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.ResourceService.GAME_OPTION;
import com.nfl.kfb.task.TaskController;
import com.nfl.kfb.task.TaskDetail;
import com.nfl.kfb.tcp.ArenaMgr;
import com.nfl.kfb.uc.AccessProxy;
import com.nfl.kfb.uc.ConfigHelper;
import com.nfl.kfb.uc.Util;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.KAKAO_PAYMENT_PARAM_OS;
import com.nfl.kfb.util.DebugOption.KAKAO_PAYMENT_PARAM_PALTFORM;
import com.nfl.kfb.util.DebugOption.REWARD_TYPE;
import com.nfl.kfb.util.DebugOption.STORE_TYPE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;
import com.qq.open.SnsSigCheck;
import com.sun.org.apache.xml.internal.resolver.helpers.Debug;

class Foo {
	@Autowired
	private BaseTaskMapper baseTaskMapper;
	@Autowired
	private TaskController taskController;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private RankService rankService;

	@Autowired
	AccountMapper accountMapper;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	
	
	@PostConstruct
	public void testStarpUp() throws ParseException, JsonProcessingException,
			IOException {
		String REDIS_URL = resourceService.getGameOption(GAME_OPTION.REDIS_URL);
		DebugOption.REDIS_URL = REDIS_URL;
		String OPEN_SERVER_TIME = resourceService
				.getGameOption(GAME_OPTION.OPEN_SERVER_TIME);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date openServerDate = sdf.parse(OPEN_SERVER_TIME);
		DebugOption.OPEN_SERVER_TIME = openServerDate;
		System.out.println("########" + DebugOption.REDIS_URL);
		System.out.println("testStarpUp");
		System.out.println(REDIS_URL);
		Jedis jedis = new Jedis(REDIS_URL);
		// RedisUtil.gRedis = jedis;
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

		// Jedis resource = DebugOption.jedisPool.getResource();
		// resource.del(DebugOption)

		long id = resourceService.getArenaResultId();
		if (id > DebugOption.maxArenaId.get()) {
			DebugOption.maxArenaId.set(id);
		}

		ArenaMgr.getInstance().setBaseTaskMapper(baseTaskMapper);
		ArenaMgr.getInstance().setAccountMapper(accountMapper);
		ArenaMgr.getInstance().setResourceService(resourceService);
		ArenaMgr.getInstance().setLoggingService(loggingService);

		/* 竞技场启动 */
		// DebugOption.timerThread.execute(new Runnable() {
		//
		// @Override
		// public void run() {
		// while(true){
		// ArenaMgr.getInstance().loop();
		// try {
		// Thread.sleep(0);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		//
		// }
		// });
		//
		//
		//
		// try {
		// TcpServer.startTcpServer(null);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}
}

class TestBean {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

/**
 * @author KimSeongsu
 * @since 2013. 7. 17.
 * 
 */
@Controller
@RequestMapping(value = "/shop", method = { RequestMethod.POST,
		RequestMethod.GET })
public class ShopController extends AbstractKfbController {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final Logger logger = LoggerFactory
			.getLogger(ShopController.class);

	@Autowired
	@Qualifier("ShopServiceImpl")
	private ShopService shopService;

	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;

	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;

	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;

	@Autowired
	private AnysdkpayMapper mapper;

	@Autowired
	private RankMapper rankMapper;

	@Autowired
	private TaskController taskController;

	@Autowired
	private AccountController accountController;

	@Autowired
	AccountMapper accountMapper;

	@Autowired
	private AnysdkpayMapper anySdkMapper;

	private static final String URL_SHOP_BUY = "/buy";
	private static final String URL_SHOP_BUY_APPSTORE_PRODUCT = "/buyAppProd"; // for
																				// AppStore
	private static final String URL_SHOP_BUY_GOOGLE_PRODUCT = "/buyGoogleProd"; // for
																				// google
																				// play
	private static final String URL_SHOP_DATA = "/shopdata";
	private static final String URL_SHOP_BUY_ANYSDK_PRODUCT = "/buyAnysdkProd"; // for
																				// Anysdk
																				// //不给客户端调用

	private static final String URL_MYTEST = "/mytest";

	private static final String URL_ALIPAY_NOTIFY = "/buyAliProd";
	private static final String URL_ALIPAY_NOTIFY_ANDROID = "/buyAliProdAndroid";

	private static final String URL_QQ_PAY = "/qqpay";

	private static final String URL_CMCC_PAY = "/cmcc";

	private static final String URL_UC_PAY = "/ucpay";

	private static final String URL_ALIPAY_CHECK = "/alipay";

	@Autowired
	private ShopMapper shopMapper;
	
	
	@Autowired
	private InvenMapper invenMapper;

	@RequestMapping(value = URL_MYTEST)
	@ResponseBody
	public JsonResponse mytest() {
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		// taskController.resetDailyRoleTask(TASK_CATEGORY.DAILY_TASK);
		// taskController.resetDailyRoleTask(TASK_CATEGORY.DAILY_ACTIVITY);
		// taskController.configNotResetTask("26143");
		// List<TaskDetail> taskDetailList = new ArrayList();
		// TaskDetail taskDetail = new TaskDetail();
		// taskDetail.setTaskType(TASK_TYPE.FIRST_RECHARGE.getValue());
		// taskDetail.setCount(1);
		// taskDetailList.add(taskDetail);
		// taskController.submitTaskDetailInfo("26143",taskDetailList);
		// taskController.configAchieveTask("35159");
		// accountController.AddMonthCard("115677");
		// Account account = accountService.getAccount("122077");
		try {
			// String json = DebugOption.toJson(account);
			// jr.put("json", json);

			// String beanListString = "[{\"name\":\"A\"},{\"name\":\"B\"}]";
			// ObjectMapper objectMapper = new ObjectMapper();
			// List<TestBean> beanList = objectMapper.readValue(beanListString,
			// new TypeReference<List<TestBean>>(){});
			// System.out.println("Bean List:");
			// for (TestBean element : beanList) {
			// System.out.println(element);
			// }

			String str = "{\n"
					+ "   \"ret\":0,\n"
					+ "   \"msg\":\"\",\n"
					+ "   \"is_lost\":0,\n"
					+ "   \"items\":[\n"
					+ "{\n"
					+ "   \"openid\":\"833011F877046499FC5F0EE06562F2DD\",\n"
					+ "   \"nickname\":\"?\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/833011F877046499FC5F0EE06562F2DD/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/833011F877046499FC5F0EE06562F2DD/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/833011F877046499FC5F0EE06562F2DD/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/833011F877046499FC5F0EE06562F2DD/100\"\n"
					+ "},\n"
					+ "{\n"
					+ "   \"openid\":\"127A598FED2EAEAD7DF30B76DAE172EF\",\n"
					+ "   \"nickname\":\"??\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/127A598FED2EAEAD7DF30B76DAE172EF/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/127A598FED2EAEAD7DF30B76DAE172EF/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/127A598FED2EAEAD7DF30B76DAE172EF/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/127A598FED2EAEAD7DF30B76DAE172EF/100\"\n"
					+ "},\n"
					+ "{\n"
					+ "   \"openid\":\"45D58283E7066CEB3C72818873DDC3EA\",\n"
					+ "   \"nickname\":\"      ????\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/45D58283E7066CEB3C72818873DDC3EA/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/45D58283E7066CEB3C72818873DDC3EA/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/45D58283E7066CEB3C72818873DDC3EA/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/45D58283E7066CEB3C72818873DDC3EA/100\"\n"
					+ "},\n"
					+ "{\n"
					+ "   \"openid\":\"E5DC1BD9A0437CDF7BFF92602AAF7CAE\",\n"
					+ "   \"nickname\":\"?????\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/E5DC1BD9A0437CDF7BFF92602AAF7CAE/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/E5DC1BD9A0437CDF7BFF92602AAF7CAE/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/E5DC1BD9A0437CDF7BFF92602AAF7CAE/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/E5DC1BD9A0437CDF7BFF92602AAF7CAE/100\"\n"
					+ "},\n"
					+ "{\n"
					+ "   \"openid\":\"7097148CB6FAF1FFC1AA7F363FF292E6\",\n"
					+ "   \"nickname\":\"???\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/7097148CB6FAF1FFC1AA7F363FF292E6/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/7097148CB6FAF1FFC1AA7F363FF292E6/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/7097148CB6FAF1FFC1AA7F363FF292E6/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/7097148CB6FAF1FFC1AA7F363FF292E6/100\"\n"
					+ "},\n"
					+ "{\n"
					+ "   \"openid\":\"43DFA0FCA5FA89EF0C11748EC73EB6BA\",\n"
					+ "   \"nickname\":\"???\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/43DFA0FCA5FA89EF0C11748EC73EB6BA/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/43DFA0FCA5FA89EF0C11748EC73EB6BA/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/43DFA0FCA5FA89EF0C11748EC73EB6BA/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/43DFA0FCA5FA89EF0C11748EC73EB6BA/100\"\n"
					+ "},\n"
					+ "{\n"
					+ "   \"openid\":\"6EE044B640EED987CC2B81E114800852\",\n"
					+ "   \"nickname\":\"???????\",\n"
					+ "   \"figureurl\":\"http://qzapp.qlogo.cn/qzapp/1102962464/6EE044B640EED987CC2B81E114800852/30\",\n"
					+ "   \"figureurl_1\":\"http://qzapp.qlogo.cn/qzapp/1102962464/6EE044B640EED987CC2B81E114800852/50\",\n"
					+ "   \"figureurl_2\":\"http://qzapp.qlogo.cn/qzapp/1102962464/6EE044B640EED987CC2B81E114800852/100\",\n"
					+ "   \"figureurl_qq\":\"http://q.qlogo.cn/qqapp/1102962464/6EE044B640EED987CC2B81E114800852/100\"\n"
					+ "}]}";

			ObjectMapper objMapper = new ObjectMapper();
			JsonNode readTree = objMapper.readTree(str);
			int asInt = readTree.get("ret").asInt();
			List<String> appIdList = new ArrayList<String>();
			if (asInt == 0) {
				Iterator<JsonNode> elements = readTree.get("items").elements();
				while (elements.hasNext()) {
					JsonNode next = elements.next();
					String strOpenId = next.get("openid").asText();
					appIdList.add(strOpenId);
				}

			}
			String string = Arrays.toString(appIdList.toArray(new String[0]));
			System.out.println(string);

			List<ShopCurrency> selectShopCurrencyByStore = shopMapper
					.selectShopCurrencyByStore(DebugOption.STORE_TYPE.ANYSDK
							.name());
			jr.put("shopCurrency", selectShopCurrencyByStore);
			Jedis resource = DebugOption.getJedisPool().getResource();
			Long zcard = resource.zcard(DebugOption.REDIS_UNLIMIT_RNANK_KEY);
			jr.put("unlimit_rank_count", zcard);
			DebugOption.getJedisPool().returnResourceObject(resource);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jr;
	}

	
	@Transactional
	@RequestMapping(value = URL_ALIPAY_NOTIFY_ANDROID)
	public void buyAliProdAndroid(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PrintWriter out = response.getWriter();
		try {
			logger.info("############buyAliProdAndroid:{buyAliProd}");

			BuyResponse buyResponse = new BuyResponse(ReturnCode.SUCCESS);
			// 获取支付宝POST过来反馈信息
			Map<String, String> params = new HashMap<String, String>();
			Map requestParams = request.getParameterMap();
			for (Iterator iter = requestParams.keySet().iterator(); iter
					.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//				 valueStr = new String(valueStr.getBytes("ISO-8859-1"),
//				 "gbk");
				params.put(name, valueStr);
			}

			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			// 商户订单号

			String out_trade_no = new String(request.getParameter(
					"out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

			// 支付宝交易号

			String trade_no = new String(request.getParameter("trade_no")
					.getBytes("ISO-8859-1"), "UTF-8");

			// 交易状态
			String trade_status = new String(request.getParameter(
					"trade_status").getBytes("ISO-8859-1"), "UTF-8");

			// 商品名称
			String subject = new String(request.getParameter("subject")
					.getBytes("ISO-8859-1"), "UTF-8");

			// 交易金额
			String total_fee = new String(request.getParameter("total_fee")
					.getBytes("ISO-8859-1"), "UTF-8");

			Double dTotal_fee = Double.parseDouble(total_fee);

			Integer iTotal_fee = dTotal_fee.intValue();

			// 测试
			if (iTotal_fee == 0) {
				iTotal_fee = 1;
			}

			// 商品描述
			String body = new String(request.getParameter("body").getBytes(
					"ISO-8859-1"), "UTF-8");// 这里表示角色的ID

			if (shopMapper.countOfOrder(trade_no) > 0) {
				throw new Exception("该订单已经处理");
			}

			logger.info(
					"------------------->out_trade_no:{},trade_no:{},trade_status:{},subject:{},total_fee:{},body:{}",
					out_trade_no, trade_no, trade_status, subject, total_fee,
					body);

			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

			 if (true) {
			//if (AlipayNotify.verify(params)) {// 验证成功
				// ////////////////////////////////////////////////////////////////////////////////////////
				// 请在这里加上商户的业务逻辑程序代码

				// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

				if (trade_status.equals("TRADE_FINISHED")) {
					// 判断该笔订单是否在商户网站中已经做过处理
					// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					// 如果有做过处理，不执行商户的业务程序
					logger.info("------------------->trade_status:{}",
							"TRADE_FINISHED");

					_alipay(iTotal_fee, body,"001724");//支付宝  android
					fistRechargeGiveUnlock701(body);
					// 注意：
					// 该种交易状态只在两种情况下出现
					// 1、开通了普通即时到账，买家付款成功后。
					// 2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
				} else if (trade_status.equals("TRADE_SUCCESS")) {
					// 判断该笔订单是否在商户网站中已经做过处理
					// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					// 如果有做过处理，不执行商户的业务程序
					logger.info("------------------->trade_status:{}",
							"TRADE_SUCCESS");
					// 注意：
					// 该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。

					_alipay(iTotal_fee, body,"001724");//支付宝  android
					fistRechargeGiveUnlock701(body);
				}

				// ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——

				// System.out.println("success"); // 请不要修改或删除
				logger.info("------------------->{}", "success");
				out.write("success");
				
				// ////////////////////////////////////////////////////////////////////////////////////////
			} else {// 验证失败
				// System.out.println("fail");
				logger.info("------------------->{}", "verify fail!!");
				throw new Exception("alipay fail");
			}
		} catch (Exception e) {
			// JsonResponse errorResponse = new
			// JsonResponse(ReturnCode.UNKNOWN_ERR);
			// errorResponse.setException(e);
			// return "error";
			e.printStackTrace();
			logger.info("------------------->{}", "exception error");
			out.write("error");
		} finally {
			out.flush();
			out.close();
		}
	}
	
	
	
	
	@Transactional
	@RequestMapping(value = URL_ALIPAY_NOTIFY)
	public void buyAliProd(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PrintWriter out = response.getWriter();
		try {
			logger.info("############alipay:{buyAliProd}");

			BuyResponse buyResponse = new BuyResponse(ReturnCode.SUCCESS);
			// 获取支付宝POST过来反馈信息
			Map<String, String> params = new HashMap<String, String>();
			Map requestParams = request.getParameterMap();
			for (Iterator iter = requestParams.keySet().iterator(); iter
					.hasNext();) {
				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
//				 valueStr = new String(valueStr.getBytes("ISO-8859-1"),
//				 "gbk");
				params.put(name, valueStr);
			}

			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以下仅供参考)//
			// 商户订单号

			String out_trade_no = new String(request.getParameter(
					"out_trade_no").getBytes("ISO-8859-1"), "UTF-8");

			// 支付宝交易号

			String trade_no = new String(request.getParameter("trade_no")
					.getBytes("ISO-8859-1"), "UTF-8");

			// 交易状态
			String trade_status = new String(request.getParameter(
					"trade_status").getBytes("ISO-8859-1"), "UTF-8");

			// 商品名称
			String subject = new String(request.getParameter("subject")
					.getBytes("ISO-8859-1"), "UTF-8");

			// 交易金额
			String total_fee = new String(request.getParameter("total_fee")
					.getBytes("ISO-8859-1"), "UTF-8");

			Double dTotal_fee = Double.parseDouble(total_fee);

			Integer iTotal_fee = dTotal_fee.intValue();

			// 测试
			if (iTotal_fee == 0) {
				iTotal_fee = 1;
			}

			// 商品描述
			String body = new String(request.getParameter("body").getBytes(
					"ISO-8859-1"), "UTF-8");// 这里表示角色的ID

			if (shopMapper.countOfOrder(trade_no) > 0) {
				throw new Exception("该订单已经处理");
			}

			logger.info(
					"------------------->out_trade_no:{},trade_no:{},trade_status:{},subject:{},total_fee:{},body:{}",
					out_trade_no, trade_no, trade_status, subject, total_fee,
					body);

			// 获取支付宝的通知返回参数，可参考技术文档中页面跳转同步通知参数列表(以上仅供参考)//

			 if (true) {
			//if (AlipayNotify.verify(params)) {// 验证成功
				// ////////////////////////////////////////////////////////////////////////////////////////
				// 请在这里加上商户的业务逻辑程序代码

				// ——请根据您的业务逻辑来编写程序（以下代码仅作参考）——

				if (trade_status.equals("TRADE_FINISHED")) {
					// 判断该笔订单是否在商户网站中已经做过处理
					// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					// 如果有做过处理，不执行商户的业务程序
					logger.info("------------------->trade_status:{}",
							"TRADE_FINISHED");

					_alipay(iTotal_fee, body,"000724");//支付宝  ios
					fistRechargeGiveUnlock701(body);
					// 注意：
					// 该种交易状态只在两种情况下出现
					// 1、开通了普通即时到账，买家付款成功后。
					// 2、开通了高级即时到账，从该笔交易成功时间算起，过了签约时的可退款时限（如：三个月以内可退款、一年以内可退款等）后。
				} else if (trade_status.equals("TRADE_SUCCESS")) {
					// 判断该笔订单是否在商户网站中已经做过处理
					// 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
					// 如果有做过处理，不执行商户的业务程序
					logger.info("------------------->trade_status:{}",
							"TRADE_SUCCESS");
					// 注意：
					// 该种交易状态只在一种情况下出现——开通了高级即时到账，买家付款成功后。

					_alipay(iTotal_fee, body,"000724");//支付宝  ios
					fistRechargeGiveUnlock701(body);
				}

				// ——请根据您的业务逻辑来编写程序（以上代码仅作参考）——

				// System.out.println("success"); // 请不要修改或删除
				logger.info("------------------->{}", "success");
				out.write("success");
				
				// ////////////////////////////////////////////////////////////////////////////////////////
			} else {// 验证失败
				// System.out.println("fail");
				logger.info("------------------->{}", "verify fail!!");
				throw new Exception("alipay fail");
			}
		} catch (Exception e) {
			// JsonResponse errorResponse = new
			// JsonResponse(ReturnCode.UNKNOWN_ERR);
			// errorResponse.setException(e);
			// return "error";
			e.printStackTrace();
			logger.info("------------------->{}", "exception error");
			out.write("error");
		} finally {
			out.flush();
			out.close();
		}
	}

	public void _alipay(Integer iTotal_fee, String body,String channel) throws Exception {
		String prod_id = "";
		int product_id = -1;
		int addCurrency = iTotal_fee;
		int count = 1;

		List<ShopCurrency> scList = shopMapper
				.selectShopCurrencyByStore(DebugOption.STORE_TYPE.ANYSDK.name());
		for (int i = 0; i < scList.size(); i++) {
			float currency = scList.get(i).getCurrency();
			int intValue = new Float(currency).intValue();
			if (addCurrency == intValue) {
				prod_id = scList.get(i).getProdId();
				product_id = scList.get(i).getItemId();
				break;
			}
		}
		// 大礼包
		if (prod_id.equals("com.nfl.game.kr.kungfubird_08")) {
			int rr = accountMapper.selectRoleRewardByTypeAndID(body,
					REWARD_TYPE.GIFT_REWARD.getValue());
			int rr1 = accountMapper.selectRoleRewardByTypeAndID(body,
					REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
			if (rr == 0 && rr1 == 0) {
				RoleReward roleReward = new RoleReward();
				roleReward.setAppId(body);
				roleReward.setGettime(new Date());
				roleReward.setType(REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
				accountMapper.insertRoleReward(roleReward);
				GameLog gameLog = new GameLog(new DateUtil(System.currentTimeMillis()),body,GAMELOG_TYPE.BIG_GIFT);
				gameLog.setCurrency(25);
				loggingService.insertGameLog(gameLog);
				if (addCurrency - 25 <= 0) {
					
					
					return;
				} else {
					prod_id = "";
					product_id = 100;
					count = addCurrency;
					addCurrency = addCurrency - 25;// 剩下的钱充值成如意珠
				}
			} else {
				prod_id = "com.nfl.game.kr.kungfubird_00";
				product_id = 100;
				//count = addCurrency * 10;
			}
		}

		if (prod_id.equals("") || prod_id.equals("com.nfl.game.kr.kungfubird_00")) {
			prod_id = "com.nfl.game.kr.kungfubird_00";
			product_id = 100;
			count = addCurrency * 10;
		}

		Date pay_time = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String format = sdf.format(pay_time);
		String orderId = UUID.randomUUID().toString();

		HashMap<String, String> _param = new HashMap<>();
		_param.put("order_id", orderId);
		_param.put("product_count", count + "");
		_param.put("amount", addCurrency + "");
		_param.put("pay_status", "1");
		_param.put("pay_time", format);
		_param.put("user_id", body);
		_param.put("order_type", "1");
		_param.put("game_user_id", "1");
		_param.put("game_id", "1");
		_param.put("server_id", "1");
		_param.put("product_name", prod_id);
		_param.put("product_id", product_id + "");
		_param.put("private_data", prod_id);// 这里填写产品ID
		_param.put("channel_number", channel);// 支付宝//000724 ios
		_param.put("sign", "");
		_param.put("source", "");
		int ball = pay(_param,false);
		logger.info("------------->ball:{}", ball);
	}

	@RequestMapping(value = URL_SHOP_BUY)
	@ResponseBody
	public JsonResponse buyItem(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "ITID", required = true) int ITID,
			@RequestParam(value = "STORE", required = true) int STORE,
			@RequestParam(value = "INST", required = true) boolean INST,// true
																		// 如意珠
			@RequestParam(value = "DC", required = true) int DC,// 折扣
			@RequestParam(value = "DCP", required = true) int DCP,// 单价
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER,
			@RequestParam(value = "CNT", required = false, defaultValue = "-1") int CNT) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());

			Shop shop = resourceService.getShop(SHOPVER, ITID);
			if (shop == null) {
				BuyResponse errorResponse = new BuyResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Not found ITID="
						+ ITID));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			STORE_TYPE storeType = STORE_TYPE.valueOf(STORE);
			if (storeType == null) {
				BuyResponse errorResponse = new BuyResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Unknown STORE="
						+ STORE));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			boolean hasCharacter = shopService.hasCharacter(account.getAppId(),
					CHID);
			if (!hasCharacter) {
				BuyResponse errorResponse = new BuyResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"You have no character. CHID=" + CHID));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			BuyItemLog buyItemLog = new BuyItemLog(dateUtil, account.getAppId());

			BuyResponse buyResponse = shopService.buyItem(dateUtil, account,
					CHID, shop, storeType, INST, DC, DCP, buyItemLog, GP, CNT);
			// System.out.println("buy");
			if (buyItemLog != null && buyItemLog.isSuccess()) {
				loggingService.insertGameLog(buyItemLog);
			}

			return buyResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_SHOP_BUY_APPSTORE_PRODUCT)
	@ResponseBody
	public JsonResponse buyAppstoreProduct(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "RECEIPT", required = true) String RECEIPT,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		Account account = null;
		try {
			logger.info("-------------buyAppstoreProduct------>");
			account = accountService.getAccount(ID);

			logger.debug("ID:{},CHID:{},RECEIPT:{}", ID, CHID, RECEIPT);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			AppleVerify appleVerify = shopService.verfifyAppleReceipt(RECEIPT);

			// 알려진 해킹툴에 의한 영수증인지 먼저 체크해보자
			ApplePurchaseInfo applePurchaseInfo;
			try {
				applePurchaseInfo = new ApplePurchaseInfo(
						appleVerify.receiptString);
				appleVerify.getReceipt().setProduct_id(
						applePurchaseInfo.getProductId());
				appleVerify.getReceipt().setTransaction_id(
						applePurchaseInfo.getTransactionId());

			} catch (Exception e) {
				logger.error("ID=" + ID, e);
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Invalid apple receipt format");

				BuyAppleProdResponse errorResponse = new BuyAppleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Invalid apple receipt format"));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			if (!applePurchaseInfo.getProductId().startsWith("com.tencent.")) {
				loggingService.insertCurrencyErr(
						dateUtil,
						account.getAppId(),
						"Invalid product-id, "
								+ applePurchaseInfo.getProductId());

				BuyAppleProdResponse errorResponse = new BuyAppleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Invalid product-id"));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			// 영수증 validation
			// AppleVerify appleVerify =
			// shopService.verfifyAppleReceipt(RECEIPT);

			if (appleVerify.getStatus() != 0) {
				loggingService.insertCurrencyErr(
						dateUtil,
						account.getAppId(),
						"Verify apple receipt status="
								+ appleVerify.getStatus());

				BuyAppleProdResponse errorResponse = new BuyAppleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Couldnot verify apple receipt status="
								+ appleVerify.getStatus()));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			// 애플상품 select
			String applePID = appleVerify.getReceipt().getProduct_id();
			String appleTransactionId = appleVerify.getReceipt()
					.getTransaction_id();

			ShopCurrency appleCurrency = shopService.getAppleCurrency(applePID);
			if (appleCurrency == null) {
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Not found apple currency from PROD_ID=" + applePID);

				BuyAppleProdResponse errorResponse = new BuyAppleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Not found apple currency from PROD_ID=" + applePID));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			Shop appleItem = resourceService.getShop(SHOPVER,
					appleCurrency.getItemId());
			if (appleItem == null) {
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Not found item from apple currency itemId="
								+ appleCurrency.getItemId());

				BuyAppleProdResponse errorResponse = new BuyAppleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Not found item from apple currency itemId="
								+ appleCurrency.getItemId()));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			CurrencyGameLog currencyGameLog = new CurrencyGameLog(dateUtil,
					account.getAppId());
			currencyGameLog.setCurrency(appleCurrency.getCurrency());

			CurrencyLog currencyLog = new CurrencyLog(dateUtil,
					account.getAppId());
			currencyLog.setStore(STORE_TYPE.APPLE);
			currencyLog.setCurrency(appleCurrency.getCurrency());
			currencyLog.setProductId(appleCurrency.getProdId());
			currencyLog.setCurrencyType(appleCurrency.getCurrencyType());
			currencyLog.setCcode(account.getCcode());

			// 구입 적용
			BuyAppleProdResponse buyAppleProdResponse;
			try {
				buyAppleProdResponse = shopService.buyAppleItem(dateUtil,
						account, CHID, appleItem, appleTransactionId,
						currencyGameLog, GP);
			} catch (DuplicateKeyException e) {
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Duplicated apple receipt=" + appleTransactionId);

				BuyAppleProdResponse errorResponse = new BuyAppleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Duplicated apple receipt=" + appleTransactionId));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			// 게임 로깅
			loggingService.insertGameLog(currencyGameLog);

			// 결제 로깅
			loggingService.insertCurrencyLog(currencyLog);

			fistRechargeGiveUnlock701(ID);
			
			
//			try {
//				shopService.sendPaymentInfoToKakao(dateUtil,
//						account.getAppId(), KAKAO_PAYMENT_PARAM_PALTFORM.apple,
//						appleCurrency.getCurrency(),
//						appleCurrency.getCurrencyType(),
//						KAKAO_PAYMENT_PARAM_OS.apple, account.getCcode(),
//						currencyLog.getLogKey());
//			} catch (Exception e) {
//				logger.error("ID=" + ID, e);
//			}
			return buyAppleProdResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			loggingService.insertCurrencyErr(dateUtil, account == null ? "0"
					: account.getAppId(), e.getMessage());
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_ALIPAY_CHECK)
	@ResponseBody
	public JsonResponse checkAlipay(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "RECEIPT", required = true) String RECEIPT,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		Account account = null;
		try {
			logger.info("-------------->checkAlipay");
			account = accountService.getAccount(ID);
			logger.info("ID:{},CHID:{},RECEIPT:{}", ID, CHID, RECEIPT);
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			BuyAppleProdResponse buyAppleProdResponse = new BuyAppleProdResponse(
					ReturnCode.SUCCESS);
			// buyAppleProdResponse.setITID(appleItem.getItemId());
			// buyAppleProdResponse.setITCNT(itemCnt);
			buyAppleProdResponse.setGD(account.getGold());
			buyAppleProdResponse.setBL(account.getBall());
			buyAppleProdResponse.setPN(account.getPunch());
			buyAppleProdResponse.setPNDT(account.getPunchRemainDt(dateUtil
					.getNowEpoch()));
			// buyAppleProdResponse.setADDGP(gamblePoint);
			logger.info("====alipay.kfb:{}",
					DebugOption.toJson(buyAppleProdResponse));
			return buyAppleProdResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			loggingService.insertCurrencyErr(dateUtil, account == null ? "0"
					: account.getAppId(), e.getMessage());
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_SHOP_BUY_GOOGLE_PRODUCT)
	@ResponseBody
	public JsonResponse buyGooglestoreProduct(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "CHID", required = true) int CHID,
			@RequestParam(value = "PURCHASE", required = true) String PURCHASE,
			@RequestParam(value = "SIGNATURE", required = true) String SIGNATURE,
			@RequestParam(value = "GP", required = false, defaultValue = "false") boolean GP,
			@RequestParam(value = "SHOPVER", required = false, defaultValue = "0") int SHOPVER) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		Account account = null;
		try {
			account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			GooglePurchaseInfo googlePurchaseInfo;

			try {
				googlePurchaseInfo = shopService.verifyGooglePurchase(PURCHASE);
			} catch (Exception e) {
				logger.error("ID=" + ID, e);
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Error verify google purchase");

				BuyGoogleProdResponse errorResponse = new BuyGoogleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Error verify google purchase"));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			// 복호화
			try {
				boolean verified = shopService.verfifyGoogleReceiptSignature(
						PURCHASE, SIGNATURE);
				if (!verified) {
					loggingService.insertCurrencyErr(dateUtil,
							account.getAppId(), "Invalid google signature, "
									+ PURCHASE);

					BuyGoogleProdResponse errorResponse = new BuyGoogleProdResponse(
							ReturnCode.UNKNOWN_ERR);
					errorResponse.setException(new Exception(
							"Invalid google signature"));
					errorResponse.setITID(0);
					errorResponse.setITCNT(0);
					errorResponse.setGD(account.getGold());
					errorResponse.setBL(account.getBall());
					errorResponse.setPN(account.getPunch());
					errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
							.getNowEpoch()));
					errorResponse.setADDGP(0);
					return errorResponse;
				}
			} catch (Exception e) {
				logger.error("ID=" + ID, e);
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Error verify google signature, " + e.getMessage());

				BuyGoogleProdResponse errorResponse = new BuyGoogleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Error verify google signature"));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			// 구글상품 select
			String googlePID = googlePurchaseInfo.getProductId();
			String googleOrderId = googlePurchaseInfo.getOrderId();

			ShopCurrency googleCurrency = shopService
					.getGoogleCurrency(googlePID);
			if (googleCurrency == null) {
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Not found google currency from PROD_ID=" + googlePID);

				BuyGoogleProdResponse errorResponse = new BuyGoogleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Not found google currency from PROD_ID=" + googlePID));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			Shop googleItem = resourceService.getShop(SHOPVER,
					googleCurrency.getItemId());
			if (googleItem == null) {
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Not found item from google currency itemId="
								+ googleCurrency.getItemId());

				BuyGoogleProdResponse errorResponse = new BuyGoogleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Not found item from google currency itemId="
								+ googleCurrency.getItemId()));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			CurrencyGameLog currencyGameLog = new CurrencyGameLog(dateUtil,
					account.getAppId());
			currencyGameLog.setCurrency(googleCurrency.getCurrency());

			CurrencyLog currencyLog = new CurrencyLog(dateUtil,
					account.getAppId());
			currencyLog.setStore(STORE_TYPE.GOOGLE);
			currencyLog.setCurrency(googleCurrency.getCurrency());
			currencyLog.setProductId(googleCurrency.getProdId());
			currencyLog.setCurrencyType(googleCurrency.getCurrencyType());
			currencyLog.setCcode(account.getCcode());

			// 구입 적용
			BuyGoogleProdResponse buyGoogleProdResponse;
			try {
				buyGoogleProdResponse = shopService.buyGoogleItem(dateUtil,
						account, CHID, googleItem, googleOrderId,
						currencyGameLog, GP);
			} catch (DuplicateKeyException e) {
				logger.error("ID=" + ID, e);
				loggingService.insertCurrencyErr(dateUtil, account.getAppId(),
						"Duplicated google orderId=" + googleOrderId);

				BuyGoogleProdResponse errorResponse = new BuyGoogleProdResponse(
						ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception(
						"Duplicated google orderId=" + googleOrderId));
				errorResponse.setITID(0);
				errorResponse.setITCNT(0);
				errorResponse.setGD(account.getGold());
				errorResponse.setBL(account.getBall());
				errorResponse.setPN(account.getPunch());
				errorResponse.setPNDT(account.getPunchRemainDt(dateUtil
						.getNowEpoch()));
				errorResponse.setADDGP(0);
				return errorResponse;
			}

			// 게임 로깅
			loggingService.insertGameLog(currencyGameLog);

			// 결제 로깅
			loggingService.insertCurrencyLog(currencyLog);

			try {
				shopService.sendPaymentInfoToKakao(dateUtil,
						account.getAppId(),
						KAKAO_PAYMENT_PARAM_PALTFORM.google,
						googleCurrency.getCurrency(),
						googleCurrency.getCurrencyType(),
						KAKAO_PAYMENT_PARAM_OS.android, account.getCcode(),
						currencyLog.getLogKey());
			} catch (Exception e) {
				logger.error("ID=" + ID, e);
			}

			return buyGoogleProdResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			loggingService.insertCurrencyErr(dateUtil, account == null ? "0"
					: account.getAppId(), e.getMessage());
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_SHOP_BUY_ANYSDK_PRODUCT)
	@ResponseBody
	public void buyAnysdkProduct(HttpServletRequest request,
			HttpServletResponse resp) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		params.put("order_id", request.getParameter("order_id"));
		params.put("product_count", "".equals(request.getParameter("product_count")) ? "1" : request.getParameter("product_count"));
		params.put("amount", request.getParameter("amount"));
		params.put("pay_status", request.getParameter("pay_status"));
		params.put("pay_time", request.getParameter("pay_time"));
		params.put("user_id", request.getParameter("user_id"));
		params.put("order_type", request.getParameter("order_type"));
		params.put("game_user_id", request.getParameter("game_user_id"));
		params.put("game_id", "1");
		params.put("server_id", request.getParameter("server_id"));
		params.put("product_name", request.getParameter("product_name"));
		params.put("product_id", request.getParameter("product_id"));
		params.put("private_data", request.getParameter("private_data"));
		params.put("channel_number", request.getParameter("channel_number"));
		params.put("sign", request.getParameter("sign"));
		params.put("source", request.getParameter("source"));

		logger.info("------------------>buyAnysdkProd.kfb");
		String str  = DebugOption.toJson(params);
		logger.info("------------------>params:{}",str);
		
		Iterator<String> iterator = params.keySet().iterator();
		List<String> params1 = new ArrayList<String>();
		while (iterator.hasNext()) {
			String next = iterator.next();
			params1.add(next);
		}

		sortParamNames(params1);// 将参数名从小到大排序，结果如：adfd,bcdr,bff,zx

		String paramValues = "";
		for (String param : params1) {// 拼接参数值
			if (param.equals("sign")) {
				continue;
			}
			String paramValue = params.get(param);
			if (paramValue != null) {
				paramValues += paramValue;
			}
		}
		String gameOption = resourceService
				.getGameOption(GAME_OPTION.ANYSDK_PRIVATE_KEY);
		logger.info("-------------------->params: " + paramValues);
		String md5Values = null;
		try {
			md5Values = DebugOption.encodeMD5(paramValues);
			md5Values = DebugOption.encodeMD5(
					md5Values.toLowerCase() + gameOption).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// logger.error("sign != mdVAlue " + sign + " " + md5Values);
		// if (isanysdk) {
		// if (sign != md5Values) {
		// logger.error("sign != mdVAlue "+sign+" "+md5Values);
		// return "error";
		// }
		// }

		int ball = pay(params,false);
		fistRechargeGiveUnlock701(params.get("user_id"));
		logger.info("---------------->anysdk ball:{}",ball);
		
		try {
			PrintWriter writer = resp.getWriter();
			if (ball > 0) {
				writer.write("ok");
			} else {
				writer.write("error");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public int pay(Map<String, String> params,boolean isTencent) {
		Anysdkpay anysdkPay = new Anysdkpay();
		anysdkPay.setAmount(new BigDecimal(params.get("amount")));
		anysdkPay.setChannel_number(params.get("channel_number"));
		// anysdkPay.setGame_id(game_id);
		anysdkPay.setGame_user_id(params.get("game_user_id"));
		anysdkPay.setOrder_id(params.get("order_id"));
		anysdkPay.setOrder_type(params.get("order_type"));
		anysdkPay.setPay_status(new Integer(params.get("pay_status")));
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		Date parse = null;
		try {
			parse = sdf.parse(params.get("pay_time"));
		} catch (ParseException e) {
			//return -1;
			parse = new Date();
		}
		anysdkPay.setPay_time(parse);
		anysdkPay.setPrivate_data(params.get("private_data"));
		anysdkPay.setProduct_count(new Integer(params.get("product_count")));
		int product_id = Integer.parseInt(params.get("product_id"));
		anysdkPay.setProduct_id(product_id);
		anysdkPay.setProduct_name(params.get("private_data"));// PROD_ID 产品ID
		anysdkPay.setRealPaytime(new java.util.Date());
		anysdkPay.setServer_id(params.get("server_id"));
		anysdkPay.setSign(params.get("sign"));
		anysdkPay.setSource(params.get("source"));
		anysdkPay.setUser_id(params.get("user_id"));
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		Account account = accountService.getAccount(params.get("user_id"));
		if (account == null) {
			try {
				throw new Exception("account not exists");
			} catch (Exception e) {
				return -1;
			}
		}
		logger.info("------------------------->anysdkPay.getProduct_name()===>{}",anysdkPay.getProduct_name());
		if (anysdkPay.getProduct_name().equals(DebugOption.MONTH_CARD_ID)) {
			accountController.AddMonthCard(account.getAppId());
			BuyMonthCardGameLog lmcg = new BuyMonthCardGameLog(dateUtil,
					account.getAppId());
			loggingService.insertGameLog(lmcg);
		} else if (anysdkPay.getProduct_name().equals(DebugOption.BIG_GIFTS_ID)) {
			logger.info("------------------------->1");
			if(!isTencent){
				int rr = accountMapper.selectRoleRewardByTypeAndID(account.getAppId(),
						REWARD_TYPE.GIFT_REWARD.getValue());
				
				int rr1 = accountMapper.selectRoleRewardByTypeAndID(account.getAppId(),
						REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
				
				logger.info("------------------------->rr:{}",rr);
				logger.info("------------------------->rr1:{}",rr1);
				if (rr == 0 && rr1 == 0) {
					RoleReward roleReward = new RoleReward();
					roleReward.setAppId(account.getAppId());
					roleReward.setGettime(new Date());
					roleReward.setType(REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
					accountMapper.insertRoleReward(roleReward);
					logger.info("------------------------->insert role reward!!}",rr);
				}else{
					logger.info("------------------------->already buy big gifts!!!");
					return -1;
				}
			}
			
		} else {
			ShopCurrency anysdkCurrency = shopService
					.getAnysdkCurrency(anysdkPay.getProduct_name());
			Shop anysdkItemShop = resourceService.getShop(0,
					anysdkCurrency.getItemId());
			if (anysdkItemShop.getItemId() == 100) {
				anysdkItemShop.setCnt(Integer.parseInt(params
						.get("product_count")));
			}
			CurrencyGameLog cl = new CurrencyGameLog(dateUtil,
					anysdkPay.getUser_id());
			// 购买Anysdk Item
			boolean isSuccess = shopService.buyAnysdkItem(account,
					anysdkItemShop, cl, anysdkPay, true);
			if (!isSuccess) {
				try {
					throw new Exception("##buyAnyskdItem fail!!!");
				} catch (Exception e) {
					// e.printStackTrace();
					return -1;
				}
			}
			loggingService.insertGameLog(cl);
		}

		{
			// tasktask
			List<TaskDetail> newTaskDetailList = taskController
					.newTaskDetailList(TASK_TYPE.FIRST_RECHARGE, 1);
			taskController.submitTaskDetailInfo(account.getAppId(),
					newTaskDetailList);
		}

		{

			Double parseDouble = Double.parseDouble(params.get("amount"));
			int intValue = parseDouble.intValue();
			if (intValue == 0) {
				intValue = 1;
			}

			{
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.RECHARGE, intValue);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}

			{
				// tasktask
				List<TaskDetail> newTaskDetailList = taskController
						.newTaskDetailList(TASK_TYPE.ONE_RECHARGE, intValue);
				taskController.submitTaskDetailInfo(account.getAppId(),
						newTaskDetailList);
			}

		}
		return account.getBall();
	}

	private void sortParamNames(List<String> params1) {
		// TODO Auto-generated method stub
		// 将参数名从小到大排序，结果如：adfd,bcdr,bff,zx
		Collections.sort(params1, new Comparator<String>() {
			public int compare(String str1, String str2) {
				return str1.compareTo(str2);
			}
		});
	}

	@RequestMapping(value = URL_SHOP_DATA)
	@ResponseBody
	public JsonResponse getShopData(
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "SK", required = true) int SK,
			@RequestParam(value = "SHOPVER", required = true) int SHOPVER) {
		try {
			Account account = accountService.getAccount(ID);

			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}

			ShopDataResponse shopDataResponse = new ShopDataResponse(
					ReturnCode.SUCCESS);

			List<Shop> shopList = resourceService.getShopList(SHOPVER);
			for (Shop shop : shopList) {
				shopDataResponse.addIT(shop.getItemId(), shop.getCnt(),
						shop.getInstant(), shop.getPriceType(),
						(int) shop.getPrice(), shop.getLvupBase(),
						shop.getLvupPrice());
			}
			return shopDataResponse;
		} catch (Exception e) {
			logger.error("ID=" + ID, e);
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}

	@RequestMapping(value = URL_QQ_PAY)
	@ResponseBody
	public JsonResponse qqPay(
			@RequestParam(value = "openid", required = false) String openid,
			@RequestParam(value = "openkey", required = false) String openkey,
			@RequestParam(value = "pay_token", required = false) String pay_token,
			@RequestParam(value = "appid", required = false) String appid,
			@RequestParam(value = "ts", required = false) String ts,
			@RequestParam(value = "pf", required = false) String pf,
			@RequestParam(value = "pfkey", required = false) String pfkey,
			@RequestParam(value = "zoneid", required = false) String zoneid,
			@RequestParam(value = "PayType", required = false, defaultValue = "Common") String PayType) {
		try {

			logger.info(
					"-------------->qqPay#openid:{},openkey:{},pay_token:{},appid:{},ts:{},PayType:{}",
					openid, openkey, pay_token, appid, ts, PayType);

			Long tsl = Long.parseLong(ts) / 1000;
			ts = tsl.toString();
			String userId = openid;
			// openid = openid.substring(6);
			if (openid.startsWith("999998")) {
				openid = openid.substring(6);
			}
			Account account = accountMapper.selectAccount(userId);
			if (account == null) {
				logger.info("-------------->account not exists!!");
				throw new Exception("account not exists!!");
			}
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			logger.info("openid:" + openid);//角色编号
			logger.info("openkey" + openkey);
			logger.info("pay_token:" + pay_token);
			logger.info("appid" + appid);
			logger.info("ts:" + ts);
			logger.info("pf:" + pf);
			logger.info("pfkey:" + pfkey);
			logger.info("zoneid:" + zoneid);
			logger.info("PayType:" + PayType);
			// String url =
			// "http://opensdktest.tencent.com/mpay/get_balance_m";// 沙箱
			String url = "http://msdk.qq.com/mpay/get_balance_m";// 现网
			String url_path = "/mpay/get_balance_m";
			logger.info("----------->url",url);

			HashMap<String, String> params = new HashMap<String, String>();
			params.put("openid", openid);
			params.put("openkey", openkey);
			params.put("pay_token", pay_token);
			params.put("appid", appid);
			params.put("ts", ts);
			params.put("pf", pf);
			params.put("pfkey", pfkey);
			params.put("zoneid", zoneid);
			String makeSig = SnsSigCheck.makeSig("GET", url_path, params,
					"RgW58OysB4serF68" + "&");
			params.put("sig", makeSig);

			HashMap<String, String> cookies = new HashMap<String, String>();
			cookies.put("session_id", SnsSigCheck.encodeUrl("openid"));
			cookies.put("session_type", SnsSigCheck.encodeUrl("kp_actoken"));
			cookies.put("org_loc", SnsSigCheck.encodeUrl("/mpay/get_balance_m"));
			cookies.put("appip", SnsSigCheck.encodeUrl("127.0.0.1"));

			org.apache.commons.httpclient.HttpClient httpClient = new org.apache.commons.httpclient.HttpClient();
			GetMethod getMethod = new GetMethod(url);
			List<NameValuePair> list = new ArrayList<NameValuePair>();

			Set<Entry<String, String>> entrySet = params.entrySet();
			for (Iterator<Entry<String, String>> iterator = entrySet.iterator(); iterator
					.hasNext();) {
				Entry<String, String> next = iterator.next();
				list.add(new NameValuePair(next.getKey(), next.getValue()));
			}

			NameValuePair[] listp = new NameValuePair[list.size()];
			NameValuePair[] array = list.toArray(listp);
			getMethod.setQueryString(array);

			// 设置cookie
			if (cookies != null && !cookies.isEmpty()) {
				Iterator iter = cookies.entrySet().iterator();
				StringBuilder buffer = new StringBuilder(128);
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					buffer.append((String) entry.getKey()).append("=")
							.append((String) entry.getValue()).append("; ");
				}
				// 设置cookie策略
				getMethod.getParams().setCookiePolicy(
						CookiePolicy.IGNORE_COOKIES);

				// 设置cookie内容
				getMethod.setRequestHeader("Cookie", buffer.toString());
			}

			String queryString = getMethod.getQueryString();
			logger.info("------------->queryString:{}", queryString);
			// System.out.println(queryString);

			httpClient.executeMethod(getMethod);
			byte[] responseBody = getMethod.getResponseBody();

			String ret = new String(responseBody, "UTF-8");

			logger.info("------------->query_response:{}", ret);

			getMethod.releaseConnection();

			JSONObject json = new JSONObject(ret);
			int retCode = json.getInt("ret");// 0 成功
			if (retCode != 0) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}
			int balance = json.getInt("balance");// balance 游戏币个数（包含了赠送游戏币）
			int gen_balance = json.getInt("gen_balance");// gen_balance 赠送游戏币个数
			int first_save = json.getInt("first_save");// first_save
														// 是否满足首次充值，1：满足，0：不满足
			int save_amt = json.getInt("save_amt");// save_amt 累计充值金额
			int gen_expire = json.getInt("gen_expire");
			JSONArray jsonArray = json.getJSONArray("tss_list");

			if (jsonArray.length() == 1) {
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				String innerproductid = jsonObject.getString("innerproductid");
				String begintime = jsonObject.getString("begintime");
				String endtime = jsonObject.getString("endtime");

			}

			logger.info("-------->balance:{},gen_balance:{}", balance,
					gen_balance);

			int sum = anySdkMapper.sumOfQQPay(userId);
			String prod_id = "";
			int product_id = -1;
			int addCurrency = 0;
			if (save_amt > sum) {
				addCurrency = save_amt - sum;
				List<ShopCurrency> scList = shopMapper
						.selectShopCurrencyByStore(DebugOption.STORE_TYPE.ANYSDK
								.name());
				for (int i = 0; i < scList.size(); i++) {
					float currency = scList.get(i).getCurrency();
					int intValue = new Float(currency).intValue();
					if (addCurrency == intValue) {
						prod_id = scList.get(i).getProdId();
						product_id = scList.get(i).getItemId();
						break;
					}
				}
			} else {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				logger.info("------------->invalid invoke recharge!!");
				return jr;
			}

			logger.info("------------->addCurrency:{}", addCurrency);

			int giftPrice = 25;
			if (addCurrency < giftPrice
					&& PayType.equals(DebugOption.PAY_TYPE_GIFTS)) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				logger.info("------------->recharge currency less than 25");
				return jr;
			}

			int count = 1;
			// 剩下的钱为零不处理
			if (PayType.equals(DebugOption.PAY_TYPE_GIFTS)) {
				if (addCurrency - giftPrice < 0) {
					jr.put("ball", account.getBall());
					logger.info("------------->addCurrency - giftPrice <= 0");
					return jr;
				} else {
					prod_id = "com.nfl.game.kr.kungfubird_00";
					product_id = 100;
					//count = addCurrency - giftPrice;// 将剩下的钱充值到账户中
					addCurrency = addCurrency - giftPrice;
				}

				int rr = accountMapper.selectRoleRewardByTypeAndID(userId,
						REWARD_TYPE.GIFT_REWARD.getValue());
				int rr1 = accountMapper.selectRoleRewardByTypeAndID(userId,
						REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
				
				if (rr > 0 || rr1 > 0 ) {
					logger.info("gift had baught!!");
					throw new Exception("gift had baught!!");
				}

				RoleReward roleReward = new RoleReward();
				roleReward.setAppId(userId);
				roleReward.setGettime(new Date());
				roleReward.setType(REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
				accountMapper.insertRoleReward(roleReward);

			}

			if (prod_id.equals("") || prod_id.equals("com.nfl.game.kr.kungfubird_00")) {
				prod_id = "com.nfl.game.kr.kungfubird_00";
				product_id = 100;
				count = addCurrency * 10;
			}

			Date pay_time = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String format = sdf.format(pay_time);
			String orderId = UUID.randomUUID().toString();

			HashMap<String, String> _param = new HashMap<>();
			_param.put("order_id", orderId);
			_param.put("product_count", count + "");
			_param.put("amount", addCurrency + "");
			_param.put("pay_status", "1");
			_param.put("pay_time", format);
			_param.put("user_id", userId);
			_param.put("order_type", "1");
			_param.put("game_user_id", "1");
			_param.put("game_id", "1");
			_param.put("server_id", "1");
			_param.put("product_name", prod_id);
			_param.put("product_id", product_id + "");
			_param.put("private_data", prod_id);// 这里填写产品ID
			_param.put("channel_number", "999999");
			_param.put("sign", "");
			_param.put("source", "");

			int ball = pay(_param,true);

			// String postRequest = SnsNetwork.postRequest(url, params, cookies,
			// "http");

			// System.out.println("##################"+postRequest);
			logger.info("------------->ball:{}", ball);
			jr.put("BALL", ball + "");
			return jr;
		} catch (Exception e) {
			e.printStackTrace();
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}

	}

	@RequestMapping(value = URL_CMCC_PAY)
	@ResponseBody
	public JsonResponse cmccPay(
			@RequestParam(value = "AppID", required = true) String AppID,
			@RequestParam(value = "OrderID", required = true) String OrderID,
			@RequestParam(value = "TradeID", required = true) String TradeID,
			@RequestParam(value = "OrderType", required = true) Integer OrderType,
			@RequestParam(value = "ID", required = true) String ID,
			@RequestParam(value = "PayType", required = true) String PayType) {
		try {
			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			Trusted2ServQueryReq treq = new Trusted2ServQueryReq();
			treq.AppID = AppID;// "300008665326";
			treq.OrderID = OrderID;// "11141029153212015970";
			treq.TradeID = TradeID;// "9B885B5064133477FA1504FE79F54969";
			treq.OrderType = OrderType;
			Trusted2ServQueryResp queryOrder = CmccPay.queryOrder(treq);
			logger.info("-------------------->PayType:{}",PayType);
			/* 测试数据 */
			// Trusted2ServQueryResp queryOrder = new Trusted2ServQueryResp();
			// queryOrder.ChannelID = "010086";
			// queryOrder.OrderID = UUID.randomUUID().toString();
			// queryOrder.ReturnCode = 0;
			// queryOrder.TotalPrice = 30.0;
			// queryOrder.TradeID = queryOrder.OrderID +"trade_id";
			// queryOrder.Version = "1.0";
			// queryOrder.StartDate="20141205134959";

			if (queryOrder.ReturnCode != 0) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}

			Account account = accountMapper.selectAccount(ID);
			if (account == null) {
				jr.setRC(ReturnCode.UNKNOWN_ERR);
				return jr;
			}

			String prod_id = "";
			int product_id = -1;
			int addCurrency = queryOrder.TotalPrice.intValue();
			List<ShopCurrency> scList = shopMapper
					.selectShopCurrencyByStore(DebugOption.STORE_TYPE.ANYSDK
							.name());
			for (int i = 0; i < scList.size(); i++) {
				float currency = scList.get(i).getCurrency();
				int intValue = new Float(currency).intValue();
				if (addCurrency == intValue) {
					prod_id = scList.get(i).getProdId();
					product_id = scList.get(i).getItemId();
					break;
				}
			}
			int count = 1;
			if (PayType.equals(DebugOption.PAY_TYPE_MONTHCARD)) {
				prod_id = DebugOption.MONTH_CARD_ID;
				product_id = 107;
			} else if (PayType.equals(DebugOption.PAY_TYPE_GIFTS)) {
				int giftPrice = 25;
				if (addCurrency < giftPrice) {
					jr.setRC(ReturnCode.UNKNOWN_ERR);
					return jr;
				}

				int rr = accountMapper.selectRoleRewardByTypeAndID(ID,
						REWARD_TYPE.GIFT_REWARD.getValue());
				int rr1 = accountMapper.selectRoleRewardByTypeAndID(ID,
						REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
				if (rr > 0 || rr1 > 0) {
					logger.info("------cmcc-------->{}","gift had baught!!");
					throw new Exception("gift had baught!!");
				}
				RoleReward roleReward = new RoleReward();
				roleReward.setAppId(ID);
				roleReward.setGettime(new Date());
				roleReward.setType(REWARD_TYPE.GIFT_REWARD_UNGET.getValue());
				accountMapper.insertRoleReward(roleReward);
				// 剩下的钱为零不处理
				if (addCurrency - giftPrice == 0) {
					jr.put("ball", account.getBall());
					return jr;
				}

				prod_id = "com.nfl.game.kr.kungfubird_00";
				product_id = 100;
				count = (addCurrency - giftPrice) * 10;// 将剩下的钱充值到账户中
			}
			//充值任意金额
			if (prod_id == "") {
				prod_id = "com.nfl.game.kr.kungfubird_00";
				product_id = 100;
				count = addCurrency * 10;
			}

			SimpleDateFormat simpleDataFormat = new SimpleDateFormat(
					"yyyyMMddHHmmss");
			Date startDate = null;
			try {
				startDate = simpleDataFormat.parse(queryOrder.StartDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
			String format = sdf.format(startDate);
			String orderId = queryOrder.OrderID;

			HashMap<String, String> _param = new HashMap<>();
			_param.put("order_id", orderId);
			_param.put("product_count", count + "");
			_param.put("amount", addCurrency + "");
			_param.put("pay_status", "1");
			_param.put("pay_time", format);
			_param.put("user_id", ID);
			_param.put("order_type", "1");
			_param.put("game_user_id", "1");
			_param.put("game_id", "1");
			_param.put("server_id", "1");
			_param.put("product_name", prod_id);
			_param.put("product_id", product_id + "");
			_param.put("private_data", prod_id);// 产品ID
			_param.put("channel_number", "010086");
			_param.put("sign", "");
			_param.put("source", "");

			int ball = pay(_param,false);

			jr.put("BALL", ball + "");
			fistRechargeGiveUnlock701(ID);
			return jr;
		} catch (Exception e) {
			e.printStackTrace();
			JsonResponse errorResponse = new JsonResponse(
					ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}

	}

	/**
	 * 支付回调通知。
	 * 注：1.通过HttpServletRequest的对象只是其中一种获取http请求参数方法，具体由游戏合作商根据自身系统而定。或使用jsp
	 * ,aspx等 2.sdk server通过http post请求cp server的接口，cp
	 * server都要有一个响应，响应内容是payCallback函数的返回值
	 * @throws IOException 
	 */
	@RequestMapping(value = URL_UC_PAY)
	public void ucpay(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.info("---------------------->[ucpay]");
		BufferedReader in = null;
		PrintWriter writer = response.getWriter();
		try {
			
			/***
			 * 方法一 ByteArrayOutputStream out = new ByteArrayOutputStream();
			 * byte[] b = new byte[1024]; int i = 0; while((i =
			 * request.getInputStream().read(b,0,1024))>0) out.write(b,0,i);
			 * byte[] bytes =out.toByteArray(); String jsonString=new
			 * String(bytes,"UTF-8"); out.close();
			 */

			// 方法二
			in = new BufferedReader(new InputStreamReader(
					request.getInputStream(), "utf-8"));
			String ln;
			StringBuffer stringBuffer = new StringBuffer();
			while ((ln = in.readLine()) != null) {
				stringBuffer.append(ln);
				stringBuffer.append("\r\n");
			}
			/**
			 * 测试数据 stringBuffer.append(
			 * "{\"sign\":\"281812485f8ddfe5437ce479883bb8ff\",\"data\":{\"failedDesc\":\"\",\"amount\":0.10,\"callbackInfo\":\"\",\"ucid\":\"56920\",\"gameId\":\"119474\",\"payWay\":\"1022\",\"serverId\":\"1333\",\"orderStatus\":\"S\",\"orderId\":\"20130507145444656713\"}}"
			 * );
			 */

			logger.debug("[接收到的参数]" + stringBuffer.toString());
			PayCallbackResponse rsp = (PayCallbackResponse) Util.decodeJson(
					stringBuffer.toString(), PayCallbackResponse.class);// 反序列化
			if (rsp != null) {
				// 输出获取到的参数
				logger.info("[sign]" + rsp.getSign());
				logger.info("[orderId]" + rsp.getData().getOrderId());
				logger.info("[gameId]" + rsp.getData().getGameId());
				logger.info("[serverId]" + rsp.getData().getServerId());
				logger.info("[accountId]" + rsp.getData().getAccountId());
				logger.info("[creator]" + rsp.getData().getCreator());
				logger.info("[payWay]" + rsp.getData().getPayWay());
				logger.info("[amount]" + rsp.getData().getAmount());
				logger.info("[callbackInfo]" + rsp.getData().getCallbackInfo());
				logger.info("[orderStatus]" + rsp.getData().getOrderStatus());
				logger.info("[failedDesc]" + rsp.getData().getFailedDesc());
				logger.info("[cpOrderId]" + rsp.getData().getCpOrderId());

				String orderId = rsp.getData().getOrderId();
				String uid = rsp.getData().getAccountId();
				Double _amount = Double.parseDouble(rsp.getData().getAmount());
				Integer amount = _amount.intValue();
//				if (amount == 0) {
//					amount = 25;
//				}
				/*
				 * 假定apiKey=202cb962234w4ers2aaa
				 * 注意：cpOrderId仅当回调时具备该参数时，才需加入签名，如无，则不需要加入签名
				 * 
				 * sign的签名规则:data的所有子参数按key升序，key=value串接即
				 * MD5(accountId=...+amount
				 * =...+callbackInfo=...+cpOrderId=...+creator
				 * =...+failedDesc=...+gameId=...+orderId=...
				 * +orderStatus=...+payWay
				 * =...+serverId=...+apiKey)（去掉+；替换...为实际值） 签名原文：
				 * accountId=123456789
				 * amount=100callbackInfo=aaacpOrderId=987654321
				 * creator=JYfailedDesc
				 * =gameId=123orderId=abcf1330orderStatus=SpayWay
				 * =1serverId=654ucid=123456202cb962234w4ers2aaa
				 */
				HashMap<String, String> dataMap = new HashMap<String, String>();
				dataMap.put("failedDesc", rsp.getData().getFailedDesc());

				String signSource = "accountId=" + rsp.getData().getAccountId()
						+ "amount=" + rsp.getData().getAmount()
						+ "callbackInfo=" + rsp.getData().getCallbackInfo()
						+ "cpOrderId=" + rsp.getData().getCpOrderId()
						+ "creator=" + rsp.getData().getCreator()
						+ "failedDesc=" + rsp.getData().getFailedDesc()
						+ "gameId=" + rsp.getData().getGameId() + "orderId="
						+ rsp.getData().getOrderId() + "orderStatus="
						+ rsp.getData().getOrderStatus() + "payWay="
						+ rsp.getData().getPayWay() + "serverId="
						+ rsp.getData().getServerId()
						+ ConfigHelper.getApiKey();
				String sign = Util.getMD5Str(signSource);

				logger.info("[签名原文]" + signSource);
				logger.info("[签名结果]" + sign);

				String orderStatus = rsp.getData().getOrderStatus();
				if (!orderStatus.equals("S")) {
					logger.info("recharge failure reason: sign not valid");
					writer.write("SUCCESS");
					return;
				}

				// if (sign == rsp.getSign()) {
				if (true) {// 去掉签名验证（以后加上）

					/*
					 * 游戏服务器需要处理给玩家充值代码,由游戏合作商开发完成。
					 */
					String prod_id = "";
					int product_id = -1;
					int addCurrency = amount;
					String callBackInfo = rsp.getData().getCallbackInfo();
					String[] ret = callBackInfo.split(",");
					
					if(ret[1].equals("com.nfl.game.kr.kungfubird_08")){
						addCurrency = 25;
					}
					
					
					logger.info("------------->ret[0]:{}",ret[0]);
					logger.info("------------->ret[1]:{}",ret[1]);
					logger.info("------------->addCurrency:{}",addCurrency);

					List<ShopCurrency> scList = shopMapper
							.selectShopCurrencyByStore(DebugOption.STORE_TYPE.ANYSDK
									.name());
					for (int i = 0; i < scList.size(); i++) {
						float currency = scList.get(i).getCurrency();
						int intValue = new Float(currency).intValue();
						if (addCurrency == intValue) {
							prod_id = scList.get(i).getProdId();
							product_id = scList.get(i).getItemId();
							break;
						}
					}
					int count = 1;
					logger.info("------------>prod_id:{}",prod_id);
					if (prod_id == "") {
						prod_id = "com.nfl.game.kr.kungfubird_00";
						product_id = 100;
						count = addCurrency;
					}
					Date pay_time = new Date();
					SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
					String format = sdf.format(pay_time);

					HashMap<String, String> _param = new HashMap<>();
					_param.put("order_id", orderId);
					_param.put("product_count", count + "");
					_param.put("amount", addCurrency + "");
					_param.put("pay_status", "1");
					_param.put("pay_time", format);
					_param.put("user_id", uid);
					_param.put("order_type", "1");
					_param.put("game_user_id", "1");
					_param.put("game_id", "1");
					_param.put("server_id", "1");
					_param.put("product_name", prod_id);
					_param.put("product_id", product_id + "");
					_param.put("private_data", prod_id);// 这里填写产品ID
					_param.put("channel_number", "000255");
					_param.put("sign", sign);
					_param.put("source", signSource);

					int ball = pay(_param,false);
					logger.info("------------->ball=>" + ball);
					logger.info("------------->recharge success");
					writer.write("SUCCESS");
					fistRechargeGiveUnlock701(uid);
					return;
					// return "SUCCESS";//返回给sdk server的响应内容
				} else {
					// return "FAILURE";//返回给sdk server的响应内容
					// ,对于重复多次通知失败的订单,请参考文档中通知机制。
					writer.write("FAILURE");
					logger.info("recharge failer");
					return;
				}
			} else {
				logger.info("------------->error");
			}
		} catch (Exception e) {
			e.printStackTrace();
			//logger.error("接收支付回调通知的参数失败");
			logger.error("------------->exception");
		} finally {
			try {
				if (null != in)
					in.close();
				in = null;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		writer.write("FAILURE");
		logger.error("------------->failure!!");
		writer.close();
	}

	/**
	 * sid用户会话验证。
	 * 
	 * @param sid
	 *            从游戏客户端的请求中获取的sid值
	 */
	@RequestMapping("/ucverify")
	public void verifySession(HttpServletRequest req, HttpServletResponse resp) {
		logger.debug("开始调用verifySession接口");
		String sid = req.getParameter("sid");
		logger.debug("sid:" + sid);
		// 新建访问代理类对象
		AccessProxy ap = new AccessProxy();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("sid", sid);// 在uc sdk登录成功时，游戏客户端通过uc
								// sdk的api获取到sid，再游戏客户端由传到游戏服务器
		String result = ap.doPost(ServiceName.VERIFYSESSION, data);// http
																	// post方式调用服务器接口,请求的body内容是参数json格式字符串
		logger.debug("[响应结果]" + result);// 结果也是一个json格式字符串
		byte[] result1 = result.getBytes(Charset.forName("UTF-8"));
		logger.debug("[响应结果]" + new String(result1));
		VerifySessionResponse rsp = null;
		try {
			rsp = (VerifySessionResponse) Util.decodeJson(result,
					VerifySessionResponse.class);// 反序列化
		} catch (Exception e) {
			logger.error("反序列化成为VerifySessionResponse对象失败,字符串为" + result);
		}
		if (rsp != null) {// 反序列化成功，输出其对象内容
			logger.debug("[id]" + rsp.getId());
			logger.debug("[code]" + rsp.getState().getCode());
			logger.debug("[msg]" + rsp.getState().getMsg());
			logger.debug("[accountId]" + rsp.getData().getAccountId());// 账号标识,最长为32个字符
			logger.debug("[creator]" + rsp.getData().getCreator());// 角色创建者,JY：九游;PP：PP助手
			logger.debug("[nickName]" + rsp.getData().getNickName());
		} else {
			logger.debug("接口返回异常");
		}
		logger.debug("调用verifySession接口结束");
		try {
			PrintWriter writer = resp.getWriter();

			JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
			jr.put("uid", rsp.getData().getAccountId());
			jr.put("nickname", (rsp.getData().getCreator() + rsp.getData()
					.getAccountId()).substring(0, 8));
			ObjectMapper om = new ObjectMapper();
			try {
				writer.write(DebugOption.toJson(jr));
			} catch (Exception e) {

				e.printStackTrace();
			}

			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//首充送仙鹤
	private void fistRechargeGiveUnlock701(String appId){
		int anysdkCount = invenMapper.countOfAnysdkPay(appId);
		int appleCount = invenMapper.countOfApplePay(appId);
		if(anysdkCount > 0 || appleCount > 0){
			return;
		}
		int count = invenMapper.countItem(appId, 0, 701);
		if(count > 0 ){
			return;
		}
		invenMapper.insertIgnoreItem(appId, 0, 701, 1, 1);
	}
}
