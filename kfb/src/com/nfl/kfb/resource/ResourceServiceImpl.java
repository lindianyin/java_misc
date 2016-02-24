/**
 * 
 */
package com.nfl.kfb.resource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.googlecode.ehcache.annotations.Cacheable;
import com.nfl.kfb.mapper.appver.Appver;
import com.nfl.kfb.mapper.appver.AppverMapper;
import com.nfl.kfb.mapper.event.Event;
import com.nfl.kfb.mapper.event.EventMapper;
import com.nfl.kfb.mapper.gamble.GambleMapper;
import com.nfl.kfb.mapper.gamble.GambleProb;
import com.nfl.kfb.mapper.gameoption.GameOptionMapper;
import com.nfl.kfb.mapper.shop.Shop;
import com.nfl.kfb.mapper.shop.ShopMapper;
import com.nfl.kfb.mapper.task.BaseTask;
import com.nfl.kfb.mapper.task.BaseTaskMapper;
import com.nfl.kfb.mapper.week.WeekAchieve;
import com.nfl.kfb.mapper.week.WeekMapper;
import com.nfl.kfb.mapper.week.WeekMission;
import com.nfl.kfb.util.DateUtil;
import com.nfl.kfb.util.DebugOption;

/**
 * @author KimSeongsu
 * @since 2013. 11. 7.
 *
 */
@Service(value="ResourceServiceImpl")
public class ResourceServiceImpl implements ResourceService {

	private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);
	
	@Autowired
	private AppverMapper appverMapper;

	@Autowired
	private WeekMapper weekMapper;
	
	@Autowired
	private EventMapper eventMapper;
	
	@Autowired
	private ShopMapper shopMapper;
	
	@Autowired
	private GambleMapper gambleMapper;
	
	@Autowired
	private GameOptionMapper gameOptionMapper;
	
	
	@Autowired
	private BaseTaskMapper baseTaskMapper;

	@Override
	@Cacheable(cacheName = "appVerCache")
	public Appver getAppver(String app, String ver) {
		Appver appver = appverMapper.selectAppver(app, ver);
		if (appver != null) {
			logger.debug("Appver, app={}, ver={}, needUpdate={}, ableSendPunch={}", appver.getApp(), appver.getVer(), appver.getNeedUpdate(), appver.getAbleSendPunch());
		}
		return appver;
	}

	@Override
	@Cacheable(cacheName = "weekResourceCache")
	public WeekResource getWeekResource(int week) {
		logger.debug("WeekResource loaded");
		
		// DB에서 이번주의 WEEK데이터 읽음
		// mission
		List<WeekMission> missionList = weekMapper.selectWeekMission(week);
		// 이번주 데이터 입력이 안되어있으면 ratation data에서 찾음
		if (missionList == null || missionList.isEmpty()) {
			missionList = weekMapper.selectWeekMission( DebugOption.WEEK_MS_ROATATION_WEEK[week % DebugOption.WEEK_MS_ROATATION_WEEK.length] );
			if (missionList == null || missionList.isEmpty()) {
				throw new RuntimeException("Not found any WEEK_MS. Input Week Data");
			}
		}
		
		// achieve
		List<WeekAchieve> achieveList = weekMapper.selectWeekAchieve(week);
		// 이번주 데이터 입력이 안되어있으면 ratation data에서 찾음
		if (achieveList == null || achieveList.isEmpty()) {
			achieveList = weekMapper.selectWeekAchieve( DebugOption.WEEK_ACH_ROATATION_WEEK[week % DebugOption.WEEK_ACH_ROATATION_WEEK.length] );
			if (achieveList == null || achieveList.isEmpty()) {
				throw new RuntimeException("Not found any WEEK_ACH. Input Week Data");
			}
		}
		
		// 읽은 WEEK데이터를 이후에 쉽게 사용할 수 있도록 파싱해서 리소스로 캐시해놓음
		WeekResource weekResource = new WeekResource(missionList, achieveList);
		
		return weekResource;
	}

	@Cacheable(cacheName = "eventCache")
	@Override
	public List<Event> getEvents() {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		return eventMapper.selectEvents(dateUtil.getNowEpoch());
	}
	
	@Override
	@Cacheable(cacheName = "shopCache")
	public Shop getShop(int shopVer, int itemId) {
		if (itemId == 0)
			return null;
		return shopMapper.selectShop(shopVer, itemId);
	}
	
	@Override
	@Cacheable(cacheName = "gameProbCache")
	public List<GambleProb> getGambleProb() {
		return gambleMapper.selectGambleProb();
	}

	@Override
	@Cacheable(cacheName = "gameOptionCache")
	public String getGameOption(GAME_OPTION gameOption) {
		return gameOptionMapper.selectGameOption(gameOption.name());
	}

	@Override
	@Cacheable(cacheName = "shopListCache")
	public List<Shop> getShopList(int shopVer) {
		return shopMapper.selectShopList(shopVer);
	}
	
	
	@Override
	@Cacheable(cacheName = "baseTaskListCache")
	public List<BaseTask> getBaseTaskList() {
		DateUtil dateUtil = new DateUtil(System.currentTimeMillis());
		final int nowEpoch = dateUtil.getNowEpoch();
		List<Event> events = eventMapper.selectOverTimeEvents(nowEpoch);
		final List<Integer> list = new ArrayList<Integer>();
		for (Event event : events) {
			if (nowEpoch < event.getStart_dt() || nowEpoch > event.getEnd_dt())
				list.add(event.getEvent_key());
		}
		List<BaseTask> selectBaseTask = baseTaskMapper.selectBaseTask();
		Iterator<BaseTask> iterator = selectBaseTask.iterator();
		while(iterator.hasNext()){
			BaseTask next = iterator.next();
			if(list.contains(next.getTaskId())){
				iterator.remove();
			}
		}
		return selectBaseTask;
	}
	
	
	public long getArenaResultId(){
		return baseTaskMapper.getMaxArenaId();
	}
	
}
