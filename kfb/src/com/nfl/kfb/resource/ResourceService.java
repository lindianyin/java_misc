/**
 * 
 */
package com.nfl.kfb.resource;

import java.util.List;

import com.googlecode.ehcache.annotations.Cacheable;
import com.nfl.kfb.mapper.appver.Appver;
import com.nfl.kfb.mapper.event.Event;
import com.nfl.kfb.mapper.gamble.GambleProb;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.task.BaseTask;

/**
 * @author KimSeongsu
 * @since 2013. 11. 7.
 *
 */
public interface ResourceService {
	
	public enum GAME_OPTION {
		DUNGEON_DAILY_LIMIT
		, DUNGEON_REQ_PUNCH
		, LATEST_SHOP_VER
		, ANYSDK_PRIVATE_KEY
		,REDIS_URL
		,OPEN_SERVER_TIME
		,OPEN_ARENA
		,ACTIVE_NAME
		,GAME_VERSION
		,GAME_DOWNLOAD_URL
		,GIFT_REWARD
	}

	@Cacheable(cacheName = "appVerCache")
	Appver getAppver(String app, String ver);

	@Cacheable(cacheName="weekResourceCache")
	public WeekResource getWeekResource(int week);
	
	@Cacheable(cacheName = "eventCache")
	List<Event> getEvents();
	
	@Cacheable(cacheName = "shopCache")
	public Shop getShop(int shopVer, int itemId);

	@Cacheable(cacheName = "gameProbCache")
	public List<GambleProb> getGambleProb();
	
	@Cacheable(cacheName = "gameOptionCache")
	public String getGameOption(GAME_OPTION gameOption);

	@Cacheable(cacheName = "shopListCache")
	List<Shop> getShopList(int shopVer);
	
	@Cacheable(cacheName = "baseTaskListCache")
	List<BaseTask> getBaseTaskList();
	
	
	long getArenaResultId();
	
	
	
	
}
