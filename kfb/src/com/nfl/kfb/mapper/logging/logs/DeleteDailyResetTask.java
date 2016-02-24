package com.nfl.kfb.mapper.logging.logs;

import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;

public class DeleteDailyResetTask extends GameLog {

	public DeleteDailyResetTask(DateUtil dateUtil) {
		super(dateUtil, DebugOption.CONFIG_TASK_APPID,  GAMELOG_TYPE.DELETE_DAILY_RESET_TASK);
	}

}
