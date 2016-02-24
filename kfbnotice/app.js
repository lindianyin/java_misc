
/**
 * Module dependencies.
 */

var express = require('express')
  , fs = require('fs')
  , connectDomain = require('connect-domain')
  , http = require('http')
  , path = require('path')
  ;

var app = express();

// all environments
app.set('port', process.env.PORT || 3000);
app.set('views', __dirname + '/views');
app.set('view engine', 'jade');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
//app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));
app.use(connectDomain());

// development only
if ('development' == app.get('env')) {
//	app.use(express.errorHandler());
}


function getDateFormat() {
	var date = new Date();

	var year = date.getFullYear();

	var month = date.getMonth() + 1;
	month = (month < 10 ? "0" : "") + month;

	var day = date.getDate();
	day = (day < 10 ? "0" : "") + day;
	
	var hour = date.getHours();
	hour = (hour < 10 ? "0" : "") + hour;

	var min = date.getMinutes();
	min = (min < 10 ? "0" : "") + min;

	return year + "/" + month + "/" + day + " " + hour + ":" + min;
}

app.get('/elb_health.html', function(req, res) {
	res.end('okay');
});

app.post('/kfb/acc/notice.kfb', function(req, res) {
	var filepath = __dirname + '/notices.json';
	
	// read file
	fs.readFile(filepath, 'utf8', function(err, data) {
		if (err) {
			throw err;
		}
		
		var jsonObject = JSON.parse(data);
		var responseJsonObject = { RC:0 };
		
		var now = getDateFormat();
		
//		console.dir(jsonObject);
//		console.log("now = " + now);
		
		///////////////////////////////////////////////////////
		// maintenance
		///////////////////////////////////////////////////////
		responseJsonObject.MAIN = { JOB:false, TYP:0, TX:"" };
//		console.log(jsonObject.maintenance);
		var maintenanceStart = jsonObject.maintenance.start;
		var maintenanceEnd = jsonObject.maintenance.end;
		if (now >= maintenanceStart && now < maintenanceEnd) {
			responseJsonObject.MAIN.JOB = true;
			responseJsonObject.MAIN.TYP = jsonObject.maintenance.type;
			responseJsonObject.MAIN.TX = jsonObject.maintenance.text;
			responseJsonObject.MAIN.LINK = jsonObject.maintenance.link;
		}
		else {
			responseJsonObject.MAIN.JOB = false;
			responseJsonObject.MAIN.TYP = 0;
			responseJsonObject.MAIN.TX = "";
			responseJsonObject.MAIN.LINK = "";
		}
		
		///////////////////////////////////////////////////////
		// notices
		///////////////////////////////////////////////////////
		responseJsonObject.NT = [];
		var noticesLength = jsonObject.notices.length;
//		console.log("size = " + noticesLength);
		for (var i=0; i<noticesLength; i++) {
			var noticeStart = jsonObject.notices[i].start;
			var noticeEnd = jsonObject.notices[i].end;
			var noticeType =jsonObject.notices[i].type;
			var noticeText = jsonObject.notices[i].text;
			var noticeEidx = jsonObject.notices[i].eidx;
			var noticeLink = jsonObject.notices[i].link;
			
			if (now >= noticeStart && now < noticeEnd) {
				responseJsonObject.NT.push( {TYP:noticeType, TX:noticeText, EIDX:noticeEidx, LINK:noticeLink} );
			}
		}
		
		res.header('Content-Type', 'application/json; charset=utf-8');
		res.json(responseJsonObject);
	});
}).use(function(err, req, res, next) {
	res.json( {RC:1} );
});

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});
