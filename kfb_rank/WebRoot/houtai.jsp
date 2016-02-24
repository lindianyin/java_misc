<%@page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@page import="com.gy.app.MapperMgr"%>
<%@page import="com.gy.model.ItemCount"%>


<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
	+ request.getServerName() + ":" + request.getServerPort()
	+ path + "/";
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>小鸟单机版后台</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->

</head>

<%
	int user_account_count = MapperMgr.getInstance().getMapper()
	.select_user_account_count();
	user_account_count = user_account_count == 0 ? 1 : user_account_count;
	Integer total_amount = MapperMgr.getInstance().getMapper().select_total_amount();
	if(total_amount == null){
		total_amount = 0;
	}
	
	int recharge_user_count= MapperMgr.getInstance().getMapper().select_recharge_user_count();
	
	int recharge_count = MapperMgr.getInstance().getMapper().select_recharge_count();
	
	int open_game_count = MapperMgr.getInstance().getMapper().select_open_game_count();
	
	List<Integer> list =  MapperMgr.getInstance().getMapper().select_recharge_id_list();
	
	int allTime = MapperMgr.getInstance().getMapper().select_all_play_time();
	
	List<ItemCount> list1 =  MapperMgr.getInstance().getMapper().select_user_statistics();
	List<ItemCount> list2 =  MapperMgr.getInstance().getMapper().select_user_statistics_progress();
%>



<body>
	<h1>小鸟单机版后台</h1>
	<p>总计:</p>
	<table border="1">
		<tr>
			<td>用户总数</td>
			<td>付费总额</td>
			<td>付费用户数</td>
			<td>付费率</td>
			<td>ARPU</td>
			<td>付费ARPU</td>
			<td>平均付费次数</td>
		</tr>
		<tr>
			<td><%=user_account_count%></td>
			<td><%=total_amount%></td>
			<td><%=recharge_user_count%></td>
			<td><%=recharge_user_count * 1.0 / user_account_count%></td>
			<td><%=total_amount * 1.0 / user_account_count%></td>
			<td><%=total_amount * 1.0 / (recharge_user_count+1)%></td>
			<td><%=recharge_count * 1.0 / (recharge_user_count+1)%></td>
		</tr>
	</table>
	<p>留存数据:</p>
	<table border="1">
		<tr>
			<td>平均打开游戏次数</td>
			<td>平均游戏时长(second)</td>
		</tr>
		<tr>
			<td><%=open_game_count * 1.0 /  user_account_count%></td>
			<td><%=allTime * 1.0 / user_account_count%></td>
		</tr>
	</table>

	<p>付费用户数据:</p>
	<table border="1">
		<tr>
			<td>用户名称</td>
			<td>付费金额</td>
			<td>付费次数</td>
			<td>打开游戏次数</td>
			<td>平均游戏时长(second)</td>
		</tr>
		<%
			for(int i=0;i<list.size();i++){ 
			String nickname = MapperMgr.getInstance().getMapper().select_user_account_nickname(list.get(i));
			int amount = MapperMgr.getInstance().getMapper().select_user_amount(list.get(i));
			int rechargeCount = MapperMgr.getInstance().getMapper().select_user_recharge_count(list.get(i));
			int openTimes = MapperMgr.getInstance().getMapper().select_user_open_game_time(list.get(i));
			int playTime = MapperMgr.getInstance().getMapper().select_total_play_time(list.get(i));
		%>
		<tr>
			<td><%=nickname%></td>
			<td><%=amount%></td>
			<td><%=rechargeCount%></td>
			<td><%=openTimes%></td>
			<td><%=playTime*1.0/(openTimes+1)%></td>
		</tr>
		<%
			}
		%>
	</table>


	<p>物品购买统计:</p>
	<table border="1">
		<tr>
			<td>物品名称</td>
			<td>数量</td>
		</tr>
		<%
			for(int i=0;i<list1.size();i++){
		%>
		<tr>
			<td><%=list1.get(i).getName()%></td>
			<td><%=list1.get(i).getCount()%></td>
		</tr>
		<%
			}
		%>
	</table>

	<p>游戏进度统计点:</p>
	<table border="1">
		<tr>
			<td>编号</td>
			<td>统计点名称</td>
			<td>到达该点的人数</td>
			<td>流失率</td>
		</tr>
		<%
			for(int i=0;i<list2.size();i++){
		%>
		<tr>
			<td><%=list2.get(i).getId()%></td>
			<td><%=list2.get(i).getName()%></td>
			<td><%=list2.get(i).getCount()%></td>
			<td><%=list2.get(i).getCount() / (user_account_count * 1.0 )%></td>
		</tr>
		<%
			}
		%>
	</table>


</body>
</html>
