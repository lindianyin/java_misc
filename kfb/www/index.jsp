<%@page import="com.nfl.kfb.util.DateUtil"%>
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="com.nfl.kfb.util.DebugOption"%>
<%@ page import="redis.clients.jedis.*"%>
redis url:<%=DebugOption.REDIS_URL%>
<%
	Long ttl = -1234L;
	String ping = null;
	try{
		Jedis jedis = new Jedis(DebugOption.REDIS_URL);
		ping = jedis.ping();
		ttl = jedis.ttl(DebugOption.REDIS_RANK_KEY);
		jedis.close();
	}catch(Exception e){
		response.getWriter().write(e.toString());
	}

%>
<br>
redis ttl:<%=ttl%><br>


<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String testDbPath = basePath + "test/testdb.kfb";
%>
<%=path %><br>
<%=basePath %><br>
<a href="<%=testDbPath%>">点我测试数据库是否连接</a><br>
week:<%
DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
%>
<%=dateUtil.getThisWeek() %><br>


<%=ping %>

okay
