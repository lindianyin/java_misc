cd ~/kfbpush

db=REAL
#db=TEST

java -Dlogback.configurationFile=config/logback.xml -classpath ./bin:./lib/json_simple-1.1.jar:./lib/junit.jar:./lib/mockito-all-1.8.5.jar:./lib/logback-classic-1.0.13.jar:./lib/logback-core-1.0.13.jar:./lib/slf4j-api-1.7.5.jar:./lib/commons-dbcp-1.4.jar:./lib/commons-pool-1.6.jar:./lib/mybatis-3.1.1.jar:./lib/mysql-connector-java-5.1.22-bin.jar com.nfl.kfb.push.sender.KfbScheduledPushSender "$db"

 