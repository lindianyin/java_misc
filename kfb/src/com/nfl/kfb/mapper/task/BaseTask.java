/**
 * 
 */
package com.nfl.kfb.mapper.task;

/**
 * @author KimSeongsu
 * @since 2013. 8. 12.
 * 
 */
public class BaseTask {
	private int taskId;
	private String taskName;
	private String taskDescribe;
	private int taskCategory;
	private int taskType;
	private int taskTarget;
	private String taskReward;
	private String taskImg;
	private int taskTarget1;
	
	
	public int getTaskCategory() {
		return taskCategory;
	}
	public void setTaskCategory(int taskCategory) {
		this.taskCategory = taskCategory;
	}
	public int getTaskId() {
		return taskId;
	}
	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public String getTaskDescribe() {
		return taskDescribe;
	}
	public void setTaskDescribe(String taskDescribe) {
		this.taskDescribe = taskDescribe;
	}
	public int getTaskType() {
		return taskType;
	}
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}
	public int getTaskTarget() {
		return taskTarget;
	}
	public void setTaskTarget(int taskTarget) {
		this.taskTarget = taskTarget;
	}
	public String getTaskReward() {
		return taskReward;
	}
	public void setTaskReward(String taskReward) {
		this.taskReward = taskReward;
	}
	public String getTaskImg() {
		return taskImg;
	}
	public void setTaskImg(String taskImg) {
		this.taskImg = taskImg;
	}
	public int getTaskTarget1() {
		return taskTarget1;
	}
	public void setTaskTarget1(int taskTarget1) {
		this.taskTarget1 = taskTarget1;
	}
	


}
