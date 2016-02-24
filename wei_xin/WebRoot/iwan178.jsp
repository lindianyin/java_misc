<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="com.gy.app.MapperMgr"%>
<%@page import="com.gy.mapper.gyp_app.Mapper"%>
<%@page import="com.gy.model.*"%>
<%@page import="javax.servlet.http.*"%>
<%@page import="org.apache.tomcat.util.http.Cookies"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
	+ request.getServerName() + ":" + request.getServerPort()
	+ path + "/";
%>


<%
response.setHeader("Pragma","No-cache"); 
response.setHeader("Cache-Control","No-cache"); 
response.setDateHeader("Expires", -1); 
response.setHeader("Cache-Control", "No-store");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type">
<title>HTML5小游戏</title>
<meta name="viewport"
	content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<!-- 
    <base href="<%=basePath%>">
    
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	 -->
	 
	<link rel="stylesheet" type="text/css" href="styles.css">
	
</head>
<script>
	var appid = "wxadba5000a286e4c8";
	var redirect_uri = "http://1.iwan178.sinaapp.com/iwan178.php";
	var empowerUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
			+ appid
			+ "&redirect_uri="
			+ redirect_uri
			+ "&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect";
	window.open(empowerUrl);
	
	function checkWeiXin() {//检测是否是微信打开
		var userAgent = window.navigator.userAgent.toLowerCase();
		if (userAgent.match(/MicroMessenger/i) == "micromessenger") {//微信
			return true;
		} else {
			return false;
		}
	}

	function getRequest() {//获取url传参
		var parameterStr = location.search;
		parameterStr = decodeURI(parameterStr);
		var theRequest = new Object();
		if (parameterStr.indexOf("?") != -1) {
			var str = parameterStr.substr(1);
			strs = str.split("&");
			for (var i = 0; i < strs.length; i++) {
				theRequest[strs[i].split("=")[0]] = (strs[i].split("=")[1]);
			}
		}
		return theRequest;
	}

	if (!checkWeiXin()) {//非微信打开

	} else {

	}
	var request = new Object();
	request = getRequest();
	//    request["openid"];
	//    request["nickname"];
	//    request["sex"];
	//    request["headimgurl"];
	//    request["country"];
	//    request["province"];
	//    request["city"];
	//if (request["openid"]) {//已拉取到用户信息
	//
	//} else {//无用户信息:授权
	//var appid = "wxadba5000a286e4c8";
	//var redirect_uri = "http://1.iwan178.sinaapp.com/iwan178.php";
	//var empowerUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
	//		+ appid
	//		+ "&redirect_uri="
	//		+ redirect_uri
	//		+ "&response_type=code&scope=snsapi_userinfo&state=1#wechat_redirect";
	//	window.open(empowerUrl);
	//}
</script>

<body>
	<div id="userDiv" style="text-align: center"><%=session.getAttribute("nickname")%></div>
	<div id="listDiv" class="gl_list"></div>
	<%
		List<Cfg_game> list = MapperMgr.getInstance().getMapper().select_cfg_game_list();
		ObjectMapper mapp = new ObjectMapper();
		String str = mapp.writeValueAsString(list);
		//out.print(str);
	%>
</body>

<script>
	var gameList =
<%=str%>
	function createGameList()//创建游戏列表
	{
		var listDiv = document.getElementById("listDiv");
		for (var i = 0; i < gameList.length; i++) {
			var oneDiv = document.createElement("div");
			oneDiv.className = "gl_list_one";
			listDiv.appendChild(oneDiv);

			var oneA = document.createElement("a");
			oneA.id = gameList[i].id;
			oneA.href = gameList[i].url + "?gameid=" + gameList[i].id;
			oneA.target = "_self";
			oneA.onclick = enterHandler;
			oneDiv.appendChild(oneA);

			var oneImg = document.createElement("img");
			oneImg.className = "gl_img";
			oneImg.src = gameList[i].logo;
			oneA.appendChild(oneImg);

			var oneCenterDiv = document.createElement("div");
			oneCenterDiv.className = "gl_list_one_center";
			oneA.appendChild(oneCenterDiv);

			var nameP = document.createElement("p");
			nameP.style.fontWeight = "bold";
			nameP.innerHTML = gameList[i].name;
			oneCenterDiv.appendChild(nameP);

			var memoP = document.createElement("p");
			memoP.style.color = "#999999";
			memoP.innerHTML = gameList[i].memo;
			oneCenterDiv.appendChild(memoP);

			var reputeP = document.createElement("p");
			reputeP.style.color = "#ff6556";
			reputeP.innerHTML = gameList[i].repute + "人玩过";
			oneCenterDiv.appendChild(reputeP);

			var rightDiv = document.createElement("div");
			rightDiv.className = "gl_list_one_right";
			oneA.appendChild(rightDiv);

			var buttonDiv = document.createElement("div");
			buttonDiv.className = "fl_button fl_button_grey";
			buttonDiv.innerHTML = "开始";
			rightDiv.appendChild(buttonDiv);
		}
	}

	function httpRequest(url) {//http请求
		var xmlHttp;
		try {
			xmlHttp = new XMLHttpRequest();
		} catch (e) {
			try {
				xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
			} catch (e) {
				try {
					xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
				} catch (e) {
					return false;
				}
			}
		}
		xmlHttp.open("POST", url, true);
		xmlHttp.send();
	}

	function enterHandler(event)//点击进入游戏
	{
		var gameid = event.currentTarget.id;
		httpRequest("http://192.168.2.254:8080/wei_xin/app/1003.gy?gameid="
				+ gameid);
		return true;
	}

	createGameList();
</script>


</html>
