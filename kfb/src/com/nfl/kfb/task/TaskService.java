/**
 * 
 */
package com.nfl.kfb.task;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import com.nfl.kfb.mapper.account.Account;
import com.nfl.kfb.mapper.dungeon.Dungeon;
import com.nfl.kfb.mapper.gamble.GambleProb;
import com.nfl.kfb.mapper.inv.Inv;
import com.nfl.kfb.mapper.inven.Inven;
import com.nfl.kfb.mapper.logging.GameLog;
import com.nfl.kfb.mapper.logging.logs.AchieveLog;
import com.nfl.kfb.mapper.logging.logs.AttendanceLog;
import com.nfl.kfb.mapper.logging.logs.GambleLog;
import com.nfl.kfb.mapper.logging.logs.LoginLog;
import com.nfl.kfb.mapper.logging.logs.ReviewLog;
import com.nfl.kfb.mapper.logging.logs.TutorialLog;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.task.RoleTask;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption.TASK_CATEGORY;

/**
 * @author KimSeongsu
 * @since 2013. 6. 24.
 *
 */
public interface TaskService {

	List<RoleTask> getTaskList(String appId);

	void giveItem(Shop rewardItem, Account account, int rewardValue,
			GameLog achieveLog, int chId);

	void deleteTask(TASK_CATEGORY task);
	
}
