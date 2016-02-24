/**
 * 
 */
package com.nfl.kfb.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nfl.kfb.AbstractKfbController;
import com.nfl.kfb.account.AccountService;
import com.nfl.kfb.logging.LoggingService;
import com.nfl.kfb.mail.MailService;
import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.inven.InvenMapper;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.GameLog.GAMELOG_TYPE;
import com.nfl.kfb.mapper.logging.LoggingMapper;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.DeleteDailyResetTask;
import com.nfl.kfb.mapper.logging.logs.DeleteWeeklyResetTask;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.task.BaseTask;
import com.nfl.kfb.mapper.task.BaseTaskMapper;
import com.nfl.kfb.mapper.task.RoleTask;
import com.nfl.kfb.model.JsonResponse;
import com.nfl.kfb.model.ReturnCode;
import com.nfl.kfb.model.WrongSessionKeyResponse;
import com.nfl.kfb.resource.ResourceService;
import com.nfl.kfb.shop.BuyResponse;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;
import com.nfl.kfb.util.DebugOption.TASK_CATEGORY;
import com.nfl.kfb.util.DebugOption.TASK_STATE;
import com.nfl.kfb.util.DebugOption.TASK_TYPE;

/**
 * @author KimSeongsu
 * @since 2013. 7. 5.
 * 
 */
@Controller
@RequestMapping(value = "/test", method = { RequestMethod.POST,RequestMethod.GET})
public class ResourceController extends AbstractKfbController {

	private static final Logger logger = LoggerFactory
			.getLogger(ResourceController.class);

	
	@Autowired
	@Qualifier("ResourceServiceImpl")
	private ResourceService resourceService;
	
	
	@RequestMapping(value = "/testdb")
	@ResponseBody
	public JsonResponse testdb() {
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		List<BaseTask> baseTaskList = resourceService.getBaseTaskList();
		jr.put("baseTaskList", baseTaskList);
		return jr;
	}

}
