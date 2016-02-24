package com.nfl.kfb.task;

//import com.nfl.kfb.mapper.task.BaseTask;
//import com.nfl.kfb.mapper.task.RoleTask;

public class TaskInfo {
//	private BaseTask baseTask;
//	private RoleTask roleTask;
//	public BaseTask getBaseTask() {
//		return baseTask;
//	}
//	public void setBaseTask(BaseTask baseTask) {
//		this.baseTask = baseTask;
//	}
//	public RoleTask getRoleTask() {
//		return roleTask;
//	}
//	public void setRoleTask(RoleTask roleTask) {
//		this.roleTask = roleTask;
//	}
	private int taskId;
	private String taskName;
	private String taskDescribe;
	private int taskCategory;
	private int taskType;
	private int taskTarget;
	private String taskReward;
	private String taskImg;
	private int taskTarget1;
	
	private long id;
	private String appId;
	//private int taskId;
	private int taskProgess;
	private  int taskProgess1;
	private int taskState;
	
	
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
	public int getTaskState() {
		return taskState;
	}
	public void setTaskState(int taskState) {
		this.taskState = taskState;
	}
	
	
}
