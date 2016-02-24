<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
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
	$(document).ready(function() {
		$("#start").click(function() {
			$.post("http://localhost:8080/rm/app/1999.gy", {
			}, function(data, status) {
				console.log(data);
				//alert("Data: " + data + "\nStatus: " + status);
			});
		});
		
		$("#run").click(function() {
        	for (var j = 0; j < 11; j++) {
        		for (var i = 0; i < 100; i++) {
	        		$.post("http://localhost:8080/rm/app/2001.gy",
		            {
		                rm_camp_id:j+1,
		                score:100
		            },
		            function(data,status){
		                console.log(data);
		                //alert("Data: " + data + "\nStatus: " + status);

		            });
	        	};

        	};
		});
		
		$("#continue").click(function() {
			$.post("http://localhost:8080/rm/app/2000.gy", {
			}, function(data, status) {
				console.log(data);
				//alert("Data: " + data + "\nStatus: " + status);
			});
		});
		
		$("#new").click(function() {
			$.post("http://localhost:8080/rm/app/1998.gy", {
			}, function(data, status) {
				console.log(data);
				//alert("Data: " + data + "\nStatus: " + status);
			});
		});
		

	});
</script>
</head>

<body>
	<button id="start">begin</button>
	<button id="run">run</button>
	<button id="continue">continue</button>
	<button id="new">new_round</button>
</body>
</html>
