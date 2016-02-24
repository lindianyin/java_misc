/**
 * 
 */
package com.nfl.kfb.logging;

import java.util.Collection;

import com.nfl.kfb.mapper.logging.CurrencyLog;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 7. 29.
 *
 */
public interface LoggingService {

	void insertGameLog(GameLog gameLog);
	
	void insertGameLog(Collection<GameLog> gameLogs);
	
	void insertCurrencyLog(CurrencyLog currencyLog);

	void insertCurrencyErr(DateUtil dateUtil, String appId, String string);

	GameLog  selectGameLog(String appId,int log_type);
}
