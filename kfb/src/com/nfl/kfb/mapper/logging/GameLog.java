/**
 * 
 */
package com.nfl.kfb.mapper.logging;

import java.util.Calendar;

import com.nfl.kfb.util.DateUtil;

/**
 * @author KimSeongsu
 * @since 2013. 8. 9.
 * 
 */
public  class GameLog {

	public static enum GAMELOG_TYPE {
		LOGIN(1) // 登录
		, ATTENDANCE(2) // 签到
		, TUTORIAL(3) // 教程
		, REVIEW(4) // 评论奖励
		, ACHIEVE(5) // 成就奖励
		, RECEIVE(6) // 领取邮件中物品
		, INV_FRIEND(7) //邀请好友
		, LAST_RANK(8) // 获得上周排行奖励
		, BUY_ITEM(9) // 购买物品
		, FINISH_PLAY(10) // 结束游戏
		, USE_ITEM_PLAY(11) // 使用物品玩游戏
		, PLAY_STAR(12) // 开始玩
		, PLAY_MISSION(13) // 任务完成后的奖励
		, PLAY_ALL_CLEAR(14) //清除所有奖励
		, CURRENCY(15) // 付款
		, WITHDRAW(16) // 提款
		, NEW_REGISTER_USER(17) // 新用户
		, ADPOPCORN(18) // 广告付费
		, RANDOM_INV_RESURRECTION(19) // 随机复活
		, EVENT_REWARD(20) // 事件补偿
		, GAMBLE(21) // 积分抽奖
		, DELETE_DAILY_RESET_TASK(22) // 删除每日重置任务
		, DELETE_WEEKLY_RESET_TASK(23)// 删除每周重置任务
		, BUY_MONTH_CARD(24) // 购买月卡
		, REWARD(25) // 获得奖励
		, GRANK_REWARD(26)//全服排行奖励
		, ADMIN_SEND_MAIL(100) //发送系统邮件
		, ARENA_COST(27)//进竞技场消耗
		, ARENA_GET(28) //竞技场获得
		, BIG_GIFT(28) //购买大礼包
		;

		private final int value;

		private GAMELOG_TYPE(int value) {
			this.value = value;
		}

		public final int getValue() {
			return value;
		}
	}

	private int logKey; // auto_increse
	private int month; // 201308, 201309, ...
	private int epoch; // created time
	private String appId;
	private int logType;
	private float currency = 0.0f;
	private int itemId = 0;
	private int itemCnt = 0;
	private int addGold = 0;
	private int nowGold = 0;
	private int addBall = 0;
	private int nowBall = 0;
	private int addPunch = 0;
	private int nowPunch = 0;
	private long reserved0 = 0;
	private long reserved1 = 0;

	public GameLog() {

	}

	public GameLog(DateUtil dateUtil, String appId, GAMELOG_TYPE gameLogType) {
		this.epoch = dateUtil.getNowEpoch();

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dateUtil.getNow());
		this.month = cal.get(Calendar.YEAR) * 100
				+ (cal.get(Calendar.MONTH) + 1);

		this.appId = appId;
		this.logType = gameLogType.getValue();
	}

	public int getLogKey() {
		return logKey;
	}

	public void setLogKey(int logKey) {
		this.logKey = logKey;
	}

	public int getMonth() {
		return month;
	}

	public int getEpoch() {
		return epoch;
	}

	public String getAppId() {
		return appId;
	}

	public int getLogType() {
		return logType;
	}

	public float getCurrency() {
		return currency;
	}

	public void setCurrency(float currency) {
		this.currency = currency;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public int getAddGold() {
		return addGold;
	}

	public void setAddGold(int addGold) {
		this.addGold = addGold;
	}

	public int getNowGold() {
		return nowGold;
	}

	public void setNowGold(int nowGold) {
		this.nowGold = nowGold;
	}

	public int getAddBall() {
		return addBall;
	}

	public void setAddBall(int addBall) {
		this.addBall = addBall;
	}

	public int getNowBall() {
		return nowBall;
	}

	public void setNowBall(int nowBall) {
		this.nowBall = nowBall;
	}

	public int getAddPunch() {
		return addPunch;
	}

	public void setAddPunch(int addPunch) {
		this.addPunch = addPunch;
	}

	public int getNowPunch() {
		return nowPunch;
	}

	public void setNowPunch(int nowPunch) {
		this.nowPunch = nowPunch;
	}

	public long getReserved0() {
		return reserved0;
	}

	public void setReserved0(long reserved0) {
		this.reserved0 = reserved0;
	}

	public long getReserved1() {
		return reserved1;
	}

	public void setReserved1(long reserved1) {
		this.reserved1 = reserved1;
	}

	public int getItemCnt() {
		return itemCnt;
	}

	public void setItemCnt(int itemCnt) {
		this.itemCnt = itemCnt;
	}

}
