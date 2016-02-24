package com.nfl.kfb.shop;

import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
//import me.gall.ten.control.BaseController;
//import me.gall.ten.control.api.bean.SyncAppOrderReq;
//import me.gall.ten.control.api.bean.SyncAppOrderResp;
//import me.gall.ten.model.bean.MmiapResult;
//import me.gall.ten.service.exception.InvalidMsgTypeException;
//import me.gall.ten.service.exception.MsgTypeNotFoundException;
//import net.sf.json.JSONObject;
//
//import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * MMIAP计费结果回调通知处理类
 * 
 * @author kimi
 * @dateTime 2013-4-28 下午4:10:26
 */
@Controller
@RequestMapping(value = "/cmcc", method = { RequestMethod.POST,
		RequestMethod.GET })
public class MMIAPApiController {

	private Logger log = Logger.getLogger(this.getClass().getName());

	private Logger mmiap_log = Logger.getLogger("mmiap");

	/**
	 * MM计费订单结果通知接口
	 * 
	 * @author kimi
	 * @dateTime 2012-6-18 下午8:21:33
	 * @param result
	 * @param request
	 * @param response
	 * @param model
	 * @return 开发者服务器 -> M-Market平台 应答结果
	 * @throws Exception
	 */
	@RequestMapping(value = "mmiap", method = RequestMethod.POST)
	@ResponseBody
	protected SyncAppOrderResp mmiap(@RequestBody SyncAppOrderReq result,
			HttpServletRequest request, HttpServletResponse response,
			Map<String, Object> model) throws Exception {
		if (null == result || "".equals(result)) {
			response.setStatus(400);
			return null;
		}
		log.info(JSONObject.valueToString(result).toString());
		SyncAppOrderResp syncAppOrderResp = new SyncAppOrderResp();
		syncAppOrderResp.setMsgType("SyncAppOrderResp");
		syncAppOrderResp.setVersion("1.0.0");
		try {
			// 根据自身项目需要处理计费结果通知信息
			// 校验参数...
			// 处理过程中，根据MM方提供的响应文档，对hRet设置不同的值进行返回。
			// ......
			// 可以自定义一些异常，进行捕获返回响应的值。
			// } catch (InvalidMsgTypeException e) {
			// mmiap_log.info("消息类型异常。异常信息是：" + e.getMessage());
			// syncAppOrderResp.sethRet(4000);
			// } catch (MsgTypeNotFoundException e) {
			// mmiap_log.info("没有找到消息类型。" + e.getMessage());
			// syncAppOrderResp.sethRet(9015);
		} catch (Exception e) {
			e.printStackTrace();
			syncAppOrderResp.sethRet(2);
		}
		return syncAppOrderResp;
	}
}
