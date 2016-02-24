<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
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

<title>running man</title>
<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">
<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
<script src="http://libs.baidu.com/jquery/2.1.1/jquery.min.js"></script>
<script>
	var baseUrl = "<%=basePath%>";
	//var baseUrl = "http://182.254.215.98:18080";
	
	$(document).ready(function() {
		$("#start").click(function() {
			$.post(baseUrl + "app/1999.gy", {
			}, function(data, status) {
				console.log(data);
				console.log(data.RC)
				console.log(data.msg)
				$("#text").text(data.msg)
				alert("开始奔跑");
			});
		});
		
		$("#run").click(function() {
        	for (var j = 0; j < 11; j++) {
        		for (var i = 0; i < 100; i++) {
	        		$.post("http://192.168.2.254:18080/running_man/app/2001.gy",
		            {
		                rm_camp_id:j+1,
		                score:100
		            },
		            function(data,status){
		                console.log(data);
		            });
	        	};

        	};
		});
		
		$("#continue").click(function() {
			$.post(baseUrl + "app/2000.gy", {
			}, function(data, status) {
				console.log(data);
				alert("开始冲刺");
			});
		});
		
		$("#new").click(function() {
			$.post(baseUrl + "app/1998.gy", {
			}, function(data, status) {
				console.log(data);
				alert("开始新的游戏");
			});
		});
		$("#submit_reward").click(function(){
			var campid = $("#camp").val();
			var time = $("#time").val();
			//alert(campid +":"+time);
			
			$.post(baseUrl + "app/1997.gy", 
			{"campid":campid,"time":time}, function(data, status) {
				console.log(data);
				alert(data.msg);
			});
			
			
			
		});
		
		
		$("#total").click(function(){
			$.post(baseUrl + "app/1996.gy", 
			{}, function(data, status) {
				console.log(data);
				alert(data.msg);
			});
		});
		
		
		$("#submit_type").click(function(){
			var game_type = $("#game_type").val();
			var game_type_value = $("#game_type_value").val();
			
			$.post(baseUrl + "app/2004.gy", 
			{"game_type":game_type,"value":game_type_value}, function(data, status) {
				console.log(data);
				alert(data.msg);
				$("#text").text(data.msg);
			});
		});		

	});
</script>
</head>

<body>
<div width="50">
	<button id="start">开始游戏</button><br>
	<!-- 
	<button id="run">run</button>
	-->
	<button id="continue">开始冲刺</button><br>
	<button id="new">新的一局</button><br>
	<select id= "camp">
		<option value ="1">ZTV1</option>
		<option value ="2">ZTV2</option>
		<option value ="3">ZTV3</option>
		<option value ="4">ZTV4</option>
		<option value ="5">ZTV5</option>
		<option value ="6" selected="selected" >ZTV6</option>
		<option value ="7">ZTV7</option>
		<option value ="8">ZTV8</option>
		<option value ="9">ZTV9</option>
		<option value ="10">ZTV10</option>
		<option value ="11">ZTV11</option>
		<option value ="12">美羊羊</option>
		<option value ="13">喜羊羊</option>  

	</select>
	
	
	<select id = "time">
		<option value ="-4">奖4秒</option>  
  		<option value ="-3">奖3秒</option>  
  		<option value="-2">奖2秒</option>  
  		<option value="-1" selected="selected">奖1秒</option>
		<option value ="1">罚1秒</option>  
  		<option value ="2">罚2秒</option>  
  		<option value="3">罚3秒</option>  
  		<option value="4">罚4秒</option>
	</select>
	<button id="submit_reward">提交奖惩</button>
	<br><br>
	<button id="total">总体结算</button>
	<br>
	点击类型:<br>
	<select id = "game_type">
	  	<option value="0" selected="selected">摇一摇</option>
		<option value ="1">划一划</option>  
  		<option value ="2">点一点</option>  

	</select>
	
	<select id = "game_type_value">
		<option value ="40">加40</option>  
  		<option value ="30">加30</option>  
  		<option value="20">加20</option>  
  		<option value="10" selected="selected">加10</option>
		<option value ="-10">减10</option>  
  		<option value ="-20">减20</option>  
  		<option value="-30">减30</option>  
  		<option value="-40">减40</option>
	</select>
	<button id="submit_type">确定修改</button>
	<p id="text"></p>
	
</div>
	
	
	
	
	
</body>
</html>
