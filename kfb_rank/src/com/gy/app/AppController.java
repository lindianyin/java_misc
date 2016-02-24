/**
 * 
 */
package com.gy.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.model.JsonResponse;
import com.gy.model.ReturnCode;
import com.gy.model.User_account;
import com.gy.model.User_statistics;
import com.nfl.kfb.util.Command;

@Controller
@RequestMapping(value = "/app", method = { RequestMethod.POST,
		RequestMethod.GET })
public class AppController {
	private static final Logger logger = LoggerFactory
			.getLogger(AppController.class);

	@RequestMapping(value = Command.STATISTICS)
	public void statistics(HttpServletRequest request,
			HttpServletResponse response) throws JsonGenerationException,
			JsonMappingException, IOException {
		PrintWriter out = response.getWriter();
		JsonResponse jr = new JsonResponse();
		ObjectMapper om = new ObjectMapper();
		try {
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setContentType("text/json;charset=UTF-8");
			Map<String, String[]> parameterMap = request.getParameterMap();
			String address = request.getRemoteAddr();
			int port = request.getRemotePort();
			String host = request.getRemoteHost();
			String user = request.getRemoteUser();
			String str = String.format(
					"address:{%s},port:{%s},host:{%s},user:{%s}", address,
					port, host, user);
			System.out.println(str);

			Integer id = Integer.parseInt(request.getParameter("id"));
			int number = Integer.parseInt(request.getParameter("number"));
			String ext = request.getParameter("ext");
			long time = Long.parseLong(request.getParameter("time"));

			java.util.Date date = new Date(time * 1000);

			// String value = om.writeValueAsString(address);

			int type = Integer.parseInt(parameterMap.get("type")[0]);
			switch (type) {
			case 1:
				System.out.println(type);
				break;
			default:
				break;
			}

			User_statistics us = new User_statistics();
			us.setBase_statistics_id(type);
			us.setExt(ext);
			us.setIp_port(host + ":" + port);
			us.setNumber(number);
			us.setTime(date);
			us.setUser_account_id(id);

			if (null != ext && !ext.equals("") && us.getBase_statistics_id() != 1100) {
				User_account select_user_account = MapperMgr.getInstance()
						.getMapper().select_user_account(ext);
				if (select_user_account == null) {
					JsonResponse login = login(ext);
					int uaid = (int)login.get("id");
					us.setUser_account_id(uaid);
				}else{
					us.setUser_account_id(select_user_account.getId());
				}
			}

			MapperMgr.getInstance().getMapper().insert_user_statistics(us);

			out.write(om.writeValueAsString(jr));

		} catch (Exception e) {
			jr.setRC(ReturnCode.UNKNOWN_ERR);
			out.write(om.writeValueAsString(jr));
			e.printStackTrace();
		} finally {
			out.flush();
			out.close();
		}
	}

	@RequestMapping(value = Command.LOGIN)
	@ResponseBody
	public JsonResponse login(@RequestParam("mac") String mac) {
		try {
			logger.info("mac:{}", mac);
			JsonResponse jr = new JsonResponse();
			User_account user_account = MapperMgr.getInstance().getMapper()
					.select_user_account(mac);
			if (null != user_account) {
				jr.put("id", user_account.getId());
				jr.put("nickname", user_account.getNickname());
				jr.put("isplayed", true);
				return jr;
			}
			String nickname = RankMgr.getInstance().getRandomName();
			User_account ua = new User_account();
			ua.setMac_addr(mac);
			ua.setNickname(nickname);
			MapperMgr.getInstance().getMapper().insert_user_account(ua);
			MapperMgr.getInstance().getMapper().inset_user_rank(ua.getId(), 0);
			RankMgr.getInstance().updateRankScore(ua.getId(), 0);
			jr.put("id", ua.getId());
			jr.put("nickname", ua.getNickname());
			jr.put("isplayed", false);
			return jr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.RANKLIST)
	@ResponseBody
	public JsonResponse getRankList(@RequestParam("id") int id) {
		try {
			JsonResponse jr = new JsonResponse();
			jr.put("rank_list", RankMgr.getInstance().getRankList(id));
			return jr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.SCORE)
	@ResponseBody
	public JsonResponse updateScore(@RequestParam("id") int id,
			@RequestParam("score") int score) {
		try {
			JsonResponse jr = new JsonResponse();
			RankMgr.getInstance().updateRankScore(id, score);
			return jr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@RequestMapping(value = Command.CHANGE_NICKNAME)
	@ResponseBody
	public JsonResponse changeNickname(@RequestParam("id") int id,
			@RequestParam("nickname") String nickname) {
		try {
			JsonResponse jr = new JsonResponse();
			MapperMgr.getInstance().getMapper()
					.update_user_account_nickname(nickname, id);
			return jr;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
