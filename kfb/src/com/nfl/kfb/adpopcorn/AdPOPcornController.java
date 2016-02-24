/**
 * 
 */
package com.nfl.kfb.adpopcorn;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.logging.logs.AdPOPcornLog;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.model.AdPOPcornResponse;
import com.nfl.kfb.model.AdPOPcornResponse.ADPOPCORN_RESULT_CODE;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 9. 25.
 *
 */
@Controller
@RequestMapping(value="/addpopcorn", method={RequestMethod.GET})
public class AdPOPcornController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory.getLogger(AdPOPcornController.class);

	private static final String URL_REWARD_CALLBACK_AND = "/reward_callback";
	private static final String URL_REWARD_CALLBACK_IOS = "/reward_callback_ios";
	
	@Autowired
	@Qualifier("AdPOPcornServiceImpl")
	private AdPOPcornService adPOPcornService;
	
	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;
	
	@Autowired
	@Qualifier("LoggingServiceImpl")
	private LoggingService loggingService;
	
	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;
	
	@RequestMapping(value=URL_REWARD_CALLBACK_AND)
	@ResponseBody
	public AdPOPcornResponse rewardAdPOPcornAndroid(
			@RequestParam(value="usn", required=true) String usn
			, @RequestParam(value="rewardkey", required=true) String rewardkey
			, @RequestParam(value="itemkey", required=true) String itemkey
			, @RequestParam(value="quantity", required=true) int quantity
			, @RequestParam(value="mediakey", required=false) String mediakey
			, @RequestParam(value="campaignkey", required=false) String campaignkey
			, @RequestHeader("IGAWORKS-HMAC-MD5") String expectedHash
			, HttpServletRequest request
			) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		
		try {
			// check hashkey
			String queryString = request.getQueryString();
			boolean isValidHashKey = DebugOption.isValidAdPOPcornHashKeyAndroid(expectedHash.toLowerCase(), queryString);
			if (!isValidHashKey) {
				return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.INVALID_HASH_KEY);
			}
			
			// check ACCOUNT exists
			Account account = accountService.getAccount(usn);
			if (account == null) {
				logger.error("ADPOPCORN Invalid usn=" + usn, new RuntimeException("ADPOPCORN Invalid usn=" + usn));
				return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.INVALID_USER_KEY);
			}
			
			// not exist itemkey
			if (!DebugOption.ADPOPCORN_ITEMKEY_AND.equals(itemkey)) {
				return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.INVALID_ITEM_KEY);
			}
			
			Shop rewardItem = resourceService.getShop(0, DebugOption.ADPOPCORN_ITEMID_AND);		// default shopVer = 0
			if (rewardItem == null) {
				return new AdPOPcornResponse(false, ADPOPCORN_RESULT_CODE.UNDEFINED_ERROR.getResultCode(), "Not exists itemId=" + DebugOption.ADPOPCORN_ITEMID_AND);
			}
			
			if (quantity < 1) {
				return new AdPOPcornResponse(false, ADPOPCORN_RESULT_CODE.UNDEFINED_ERROR.getResultCode(), "quantity=" + quantity);
			}
			
			// give item
			AdPOPcornResponse adPOPcornResponse = adPOPcornService.rewardAdPOPcorn(dateUtil, usn, rewardkey, itemkey, rewardItem.getItemId(), quantity, mediakey, campaignkey);
			
			AdPOPcornLog adPOPcornLog = new AdPOPcornLog(dateUtil, account.getAppId());
			adPOPcornLog.setItemId(rewardItem.getItemId());
			adPOPcornLog.setItemCnt(quantity);
			adPOPcornLog.setNowGold(account.getGold());
			adPOPcornLog.setNowBall(account.getBall());
			adPOPcornLog.setNowPunch(account.getPunch());
			loggingService.insertGameLog(adPOPcornLog);
			
			return adPOPcornResponse;
		} catch (DuplicateKeyException dupliKey) {
			String tmp = String.format("usn[%s], rewardkey[%s], itemkey[%s], quantity[%d], mediakey[%s], campaignkey[%s], expectedHash[%s], queryString[%s]", 
					usn, rewardkey, itemkey, quantity, mediakey, campaignkey, expectedHash, request.getQueryString());
			logger.error("ADPOPCORN " + tmp, dupliKey);
			return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.DUPLICATE_TRANSACTION);
		} catch (Exception e) {
			String tmp = String.format("usn[%s], rewardkey[%s], itemkey[%s], quantity[%d], mediakey[%s], campaignkey[%s], expectedHash[%s], queryString[%s]", 
					usn, rewardkey, itemkey, quantity, mediakey, campaignkey, expectedHash, request.getQueryString());
			logger.error("ADPOPCORN " + tmp, e);
			return new AdPOPcornResponse(false, ADPOPCORN_RESULT_CODE.UNDEFINED_ERROR.getResultCode(), e.getMessage());
		}
	}
	
	@RequestMapping(value=URL_REWARD_CALLBACK_IOS)
	@ResponseBody
	public AdPOPcornResponse rewardAdPOPcornIos(
			@RequestParam(value="usn", required=true) String usn
			, @RequestParam(value="rewardkey", required=true) String rewardkey
			, @RequestParam(value="itemkey", required=true) String itemkey
			, @RequestParam(value="quantity", required=true) int quantity
			, @RequestParam(value="mediakey", required=false) String mediakey
			, @RequestParam(value="campaignkey", required=false) String campaignkey
			, @RequestHeader("IGAWORKS-HMAC-MD5") String expectedHash
			, HttpServletRequest request
			) {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		
		try {
			// check hashkey
			String queryString = request.getQueryString();
			boolean isValidHashKey = DebugOption.isValidAdPOPcornHashKeyIos(expectedHash.toLowerCase(), queryString);
			if (!isValidHashKey) {
				return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.INVALID_HASH_KEY);
			}
			
			// check ACCOUNT exists
			Account account = accountService.getAccount(usn);
			if (account == null) {
				logger.error("ADPOPCORN Invalid usn=" + usn, new RuntimeException("ADPOPCORN Invalid usn=" + usn));
				return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.INVALID_USER_KEY);
			}
			
			// not exist itemkey
			if (!DebugOption.ADPOPCORN_ITEMKEY_IOS.equals(itemkey)) {
				return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.INVALID_ITEM_KEY);
			}
			
			Shop rewardItem = resourceService.getShop(0, DebugOption.ADPOPCORN_ITEMID_IOS);		// default shopVer = 0
			if (rewardItem == null) {
				return new AdPOPcornResponse(false, ADPOPCORN_RESULT_CODE.UNDEFINED_ERROR.getResultCode(), "Not exists itemId=" + DebugOption.ADPOPCORN_ITEMID_IOS);
			}
			
			if (quantity < 1) {
				return new AdPOPcornResponse(false, ADPOPCORN_RESULT_CODE.UNDEFINED_ERROR.getResultCode(), "quantity=" + quantity);
			}
			
			// give item
			AdPOPcornResponse adPOPcornResponse = adPOPcornService.rewardAdPOPcorn(dateUtil, usn, rewardkey, itemkey, rewardItem.getItemId(), quantity, mediakey, campaignkey);
			
			AdPOPcornLog adPOPcornLog = new AdPOPcornLog(dateUtil, account.getAppId());
			adPOPcornLog.setItemId(rewardItem.getItemId());
			adPOPcornLog.setItemCnt(quantity);
			adPOPcornLog.setNowGold(account.getGold());
			adPOPcornLog.setNowBall(account.getBall());
			adPOPcornLog.setNowPunch(account.getPunch());
			loggingService.insertGameLog(adPOPcornLog);
			
			return adPOPcornResponse;
		} catch (DuplicateKeyException dupliKey) {
			String tmp = String.format("usn[%s], rewardkey[%s], itemkey[%s], quantity[%d], mediakey[%s], campaignkey[%s], expectedHash[%s], queryString[%s]", 
					usn, rewardkey, itemkey, quantity, mediakey, campaignkey, expectedHash, request.getQueryString());
			logger.error("ADPOPCORN " + tmp, dupliKey);
			return new AdPOPcornResponse(ADPOPCORN_RESULT_CODE.DUPLICATE_TRANSACTION);
		} catch (Exception e) {
			String tmp = String.format("usn[%s], rewardkey[%s], itemkey[%s], quantity[%d], mediakey[%s], campaignkey[%s], expectedHash[%s], queryString[%s]", 
					usn, rewardkey, itemkey, quantity, mediakey, campaignkey, expectedHash, request.getQueryString());
			logger.error("ADPOPCORN " + tmp, e);
			return new AdPOPcornResponse(false, ADPOPCORN_RESULT_CODE.UNDEFINED_ERROR.getResultCode(), e.getMessage());
		}
	}
	
}
