/**
 * 
 */
package com.gy.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gy.model.JsonResponse;
import com.gy.model.ReturnCode;
import com.gy.model.User_account;
import com.gy.model.User_account_Cfg_game;
import com.gy.model.User_account_Cfg_game_VO;
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
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/json;charset=UTF-8");
		Map<String, String[]> parameterMap = request.getParameterMap();
		String address = request.getRemoteAddr();
		int port = request.getRemotePort();
		String host = request.getRemoteHost();
		String user = request.getRemoteUser();
		String str = String.format(
				"address:{%s},port:{%s},host:{%s},user:{%s}", address, port,
				host, user);
		System.out.println(str);

		ObjectMapper om = new ObjectMapper();
		String value = om.writeValueAsString(address);

		int type = Integer.parseInt(parameterMap.get("type")[0]);
		switch (type) {
		case 1:
			System.out.println(type);
			break;
		default:
			break;
		}

		int countOf_cfg_game = MapperMgr.getInstance().getMapper()
				.countOf_cfg_game();

		System.out.println("----------------->" + countOf_cfg_game);

		PrintWriter out = response.getWriter();
		try {
			out.write(value);
		} catch (Exception e) {

		} finally {
			out.flush();
			out.close();
		}
	}

	// id, openid, nickname, sex, province, city, country, headimgurl
	@RequestMapping(value = Command.LOGIN)
	public void login(@RequestParam("openid") String openid,
			@RequestParam("nickname") String nickname,
			@RequestParam("sex") String sex,
			@RequestParam("province") String province,
			@RequestParam("city") String city,
			@RequestParam("country") String country,
			@RequestParam("headimgurl") String headimgurl) {
		logger.info("{},{},{},{},{},{},{}", openid, nickname, sex, province,
				city, country, headimgurl);

		int count = MapperMgr.getInstance().getMapper()
				.countOf_user_account_by_openid(openid);
		if (count > 0) {
			MapperMgr.getInstance().getMapper()
					.update_user_account(nickname, headimgurl, openid);
			return;
		}
		User_account ua = new User_account();
		ua.setCity(city);
		ua.setCountry(country);
		ua.setGold(0);
		ua.setHeadimgurl(headimgurl);
		ua.setNickname(nickname);
		ua.setOpenid(openid);
		ua.setProvince(province);
		ua.setSex(sex);
		MapperMgr.getInstance().getMapper().insert_user_account(ua);
	}

	@RequestMapping(value = Command.PREPUT)
	@ResponseBody
	public void incrementGameRepute(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/json;charset=UTF-8");

		int gameid = Integer.parseInt(request.getParameter("gameid"));
		String openid = request.getParameter("openid");

		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		System.out.println(openid);
		logger.info("Command.PREPUT:{}", openid);
		if (openid != null) {
			Integer id = MapperMgr.getInstance().getMapper()
					.select_user_account(openid);
//			if (id != null) {
//				MapperMgr.getInstance().getMapper()
//						.insertOrUpdate_user_account_cfg_game(id, gameid, 0);
//			}
		}
		MapperMgr.getInstance().getMapper().incrementGameRepute(gameid);
		ObjectMapper objectMapper = new ObjectMapper();
		String ret = objectMapper.writeValueAsString(jr);
		logger.info(ret);
		PrintWriter writer = response.getWriter();
		writer.write(ret);
		writer.flush();
		writer.close();
	}

	@RequestMapping(value = Command.SCORE)
	@ResponseBody
	public void submitScore(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/json;charset=UTF-8");

		int gameid = Integer.parseInt(request.getParameter("gameid"));
		String openid = request.getParameter("openid");
		double score = Double.parseDouble(request.getParameter("score"));
		//int order = Integer.parseInt(request.getParameter("order"));// order大于零,逆序排行
		
		boolean isDESC = MapperMgr.getInstance().getMapper().select_cfg_game_order(gameid) == 0 ? true : false;//0表示逆序排行 1表示正序排行
		if(!isDESC){
			score = -score;
		}
		
		logger.info("Command.SCORE:gameid:{},openid:{},score:{}", gameid,
				openid, score);
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		System.out.println(openid);

		System.out.println(openid);
		int rank = -1;
		double scoree = -1;
		if (openid != null) {
			Integer id = MapperMgr.getInstance().getMapper()
					.select_user_account(openid);
			if (id != null) {
				MapperMgr
				.getInstance()
				.getMapper()
				.insertOrUpdate_user_account_cfg_game(id, gameid,
						score);
			}

			User_account_Cfg_game_VO user = MapperMgr
					.getInstance()
					.getMapper()
					.select_user_account_cfg_game_by_user_account_id(gameid, id);
			double _score = user.getScore();
			scoree = _score;
			rank = MapperMgr.getInstance().getMapper()
					.select_User_account_cfg_game_rank(gameid, _score);

		}

		if(scoree < 0){
			scoree =-scoree;
		}
		jr.put("self_rank", rank + 1);
		jr.put("self_score", scoree);
		ObjectMapper objectMapper = new ObjectMapper();
		String ret = objectMapper.writeValueAsString(jr);
		logger.info(ret);
		PrintWriter writer = response.getWriter();
		writer.write(ret);
		writer.flush();
		writer.close();
	}

	@RequestMapping(value = Command.RANK)
	@ResponseBody
	public void getRank(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("text/json;charset=UTF-8");
		JsonResponse jr = new JsonResponse(ReturnCode.SUCCESS);
		int gameid = Integer.parseInt(request.getParameter("gameid"));
		String openid = request.getParameter("openid");
		System.out.println(openid);
		logger.info("Command.RANK:openid:{},gameid:{}", openid, gameid);
		System.out.println(openid);
		if (openid != null) {
			Integer id = MapperMgr.getInstance().getMapper()
					.select_user_account(openid);
			if (id != null) {
				List<User_account_Cfg_game_VO> list = MapperMgr.getInstance()
						.getMapper().select_user_account_cfg_game(gameid);
				boolean isContain = false;
				int rank = -1;
				double score = -1;
				for (int i = 0; i < list.size(); i++) {
					if (id == list.get(i).getUser_account_id()) {
						isContain = true;
						rank = i;
						score = list.get(i).getScore();
						break;
					}
				}

				if (!isContain) {
					if (list.size() >= 10) {
						list = list.subList(0, 9);
					}
					User_account_Cfg_game_VO self = MapperMgr
							.getInstance()
							.getMapper()
							.select_user_account_cfg_game_by_user_account_id(
									gameid, id);
					if (self != null) {
						list.add(self);
						rank = MapperMgr
								.getInstance()
								.getMapper()
								.select_User_account_cfg_game_rank(gameid,
										self.getScore());
						score = self.getScore();
					}
				}
				for (int i = 0; i < list.size(); i++) {
					if (id == list.get(i).getUser_account_id()) {
						list.get(i).setIndex(rank + 1);
					} else {
						list.get(i).setIndex(i + 1);
					}
					if(list.get(i).getScore() < 0){
						list.get(i).setScore(-list.get(i).getScore());
					}
				}
				
				if(score < 0){
					score = -1;
				}
				
				
				jr.put("self_id", id);
				if(isContain){
					jr.put("self_score", score);
					jr.put("self_rank", rank + 1);
				}
				jr.put("list", list);
			}
		}

		ObjectMapper objectMapper = new ObjectMapper();
		String ret = objectMapper.writeValueAsString(jr);
		logger.info(ret);
		PrintWriter writer = response.getWriter();
		writer.write(ret);
		writer.flush();
		writer.close();
	}

}
