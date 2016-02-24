/**
 * 
 */
package com.nfl.kfb.util;

import java.util.Calendar;


/**
 * @author KimSeongsu
 * @since 2013. 7. 2.
 *
 */
public class DateUtil {
	
	private final long now;
	
	public DateUtil(long now) {
		this.now = now;
	}
	
	public long getNow() {
		return now;
	}
	
	public static final int ONE_MINUTE_EPOCH = 60;
	
	public static final int ONE_HOUR_EPOCH = ONE_MINUTE_EPOCH * 60;

	/**
	 * 1일을 나타내는 TimeStamp<br>
	 */
	public static final long ONE_DAY_TIMESTAMP = 1000L * 60 * 60 * 24;
	
	public static final int ONE_DAY_EPOCH = ONE_HOUR_EPOCH * 24;
	
	/**
	 * 1주일을 나타내는 TimeStamp<br>
	 */
	public static final long ONE_WEEK_TIMESTAMP = ONE_DAY_TIMESTAMP * 7;
	
	public static final int ONE_WEEK_EPOCH = ONE_DAY_EPOCH * 7;
	
	
	public long getTodayStart() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
	}
	
	public long getHourStart() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		return cal.getTimeInMillis();
	}
	
	public int getNowEpoch() {
		return (int) (now / 1000);
	}

	public long getTomorrowStart() {
		return getTodayStart() + ONE_DAY_TIMESTAMP;
	}
	
	public int getTomorrowStartEpoch() {
		return (int) (getTomorrowStart() / 1000);
	}
	
	public int getTodayStartEpoch() {
		return (int) (getTodayStart() / 1000);
	}
	
	public int getHourStartEpoch() {
		return (int) (getHourStart() / 1000);
	}
	
	public int getThisWeek() {
		return (int) ((now - DebugOption.WEEK_START_TIMESTAMP) / DateUtil.ONE_WEEK_TIMESTAMP) + DebugOption.WEEK_OFFSET;
	}

	public long getWeekStartTimestamp(int week) {
		return DebugOption.WEEK_START_TIMESTAMP + (DateUtil.ONE_WEEK_TIMESTAMP * week);
	}

	public int getWeekStartEpoch(int week) {
		return DebugOption.WEEK_START_EPOCH + (DateUtil.ONE_WEEK_EPOCH * week);
	}
	
}
