/**
 * 
 */
package com.nfl.kfb.week;

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
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.resource.WeekResource;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 6. 26.
 *
 */
@Controller
@RequestMapping(value="/week", method={RequestMethod.POST /*, RequestMethod.GET*/})
public class WeekController extends AbstractKfbController {
	
	private static final Logger logger = LoggerFactory.getLogger(WeekController.class);
	
	private static final String URL_WEEK_DATA = "/getWeek";
	
	@Autowired
	@Qualifier("AccountServiceImpl")
	private AccountService accountService;
	
	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;
	
	@RequestMapping(value=URL_WEEK_DATA)
	@ResponseBody
	public JsonResponse getWeekData(
			@RequestParam(value="ID", required=true) String ID
			, @RequestParam(value="SK", required=true) int SK
			, @RequestParam(value="WK", required=true) int WK
			) {
		
		try {
			Account account = accountService.getAccount(ID);
			
			if (!isValidSessionKey(account, SK)) {
				return new WrongSessionKeyResponse();
			}
			
			DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
			
			int thisWeek = dateUtil.getThisWeek();
			
			if (WK != thisWeek) {
				JsonResponse errorResponse = new JsonResponse(ReturnCode.WRONG_WEEK);
				errorResponse.put("WK", thisWeek);
				return errorResponse;
			}
			
			WeekResource weekResource = resourceService.getWeekResource(WK);
			if (weekResource == null) {
				JsonResponse errorResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
				errorResponse.setException(new Exception("Not found WeekData for WK=" + WK));
				return errorResponse;
			}
			
			return weekResource.getCachedWeekDataResponse();
		} catch (Exception e) {
			logger.error("ID="+ID, e);
			JsonResponse errorResponse = new JsonResponse(ReturnCode.UNKNOWN_ERR);
			errorResponse.setException(e);
			return errorResponse;
		}
	}
	
	
}
