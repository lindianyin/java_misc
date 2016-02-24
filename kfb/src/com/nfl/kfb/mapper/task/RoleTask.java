/**
 * 
 */
package com.nfl.kfb.mapper.task;

/**
 * @author KimSeongsu
 * @since 2013. 8. 12.
 *
 */
public class RoleTask {
	
	private long id;
	private String appId;
	private int taskId;
	private int taskProgess;
	private  int taskProgess1;
	private int taskState;
	
	public int getTaskState() {
		return taskState;
	}
	public void setTaskState(int taskState) {
		this.taskState = taskState;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public int getTaskProgess() {
		return taskProgess;
	}
	public void setTaskProgess(int taskProgess) {
		this.taskProgess = taskProgess;
	}
	public int getTaskProgess1() {
		return taskProgess1;
	}
	public void setTaskProgess1(int taskProgess1) {
		this.taskProgess1 = taskProgess1;
	}
	
	
	
}
