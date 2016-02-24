/**
 * 
 */
package com.nfl.kfb.logging;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nfl.kfb.mapper.logging.CurrencyLog;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.LoggingMapper;
import com.nfl.kfb.util.DateUtil;


/**
 * @author KimSeongsu
 * @since 2013. 7. 29.
 *
 */
@Service(value="LoggingServiceImpl")
public class LoggingServiceImpl implements LoggingService {
	
	private static final Logger logger = LoggerFactory.getLogger(LoggingServiceImpl.class);
	
	@Autowired
	private LoggingMapper loggingMapper;
	
	@Override
	public void insertGameLog(GameLog gameLog) {
		try {
			if (gameLog == null)
				return;
//			logger.debug("insert gameLog, appId={}, logType={}", gameLog.getAppId(), gameLog.getLogType());
			
			loggingMapper.insertGameLog(gameLog);
		} catch (Exception e) {
			logger.error(this.toString(), e);
		}
	}
	
	@Override
	public void insertGameLog(Collection<GameLog> gameLogs) {
		try {
			if (gameLogs.isEmpty())
				return;
//			logger.debug("insert gameLogs, size={}", gameLogs.size());
			
			loggingMapper.insertGameLogs(gameLogs);
		} catch (Exception e) {
			logger.error(this.toString(), e);
		}
	}
	
	@Override
	public void insertCurrencyLog(CurrencyLog currencyLog) {
		try {
			if (currencyLog == null)
				return;
//			logger.debug("insert currencyLog, appId={}, currency={}", currencyLog.getAppId(), currencyLog.getCurrency());
			
			loggingMapper.insertCurrencyLog(currencyLog);
		} catch (Exception e) {
			logger.error(this.toString(), e);
		}
	}

	@Override
	public void insertCurrencyErr(DateUtil dateUtil, String appId, String errMsg) {
		try {
			if (errMsg == null)
				errMsg = "";
			if (errMsg.length() > 250) {		// limit string length for VARCHAR(255)
				errMsg = errMsg.substring(0, 250);
			}
			loggingMapper.insertCurrncyErrLog(dateUtil.getNowEpoch(), appId, errMsg);
		} catch (Exception e) {
			logger.error(this.toString(), e);
		}
	}
	
	@Override
	public GameLog selectGameLog(String appId, int log_type) {
		GameLog selectGameLog = loggingMapper.selectGameLog(appId, log_type);
		return selectGameLog;
	}
}
