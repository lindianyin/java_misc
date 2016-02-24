package com.nfl.kfb.task;

import com.nfl.kfb.mapper.task.BaseTask;
import com.nfl.kfb.mapper.task.RoleTask;

public class TaskInfoAll {
	private BaseTask baseTask;
	private RoleTask roleTask;
	private TaskDetail taskDetail;
	
	public TaskDetail getTaskDetail() {
		return taskDetail;
	}
	public void setTaskDetail(TaskDetail taskDetail) {
		this.taskDetail = taskDetail;
	}
	public BaseTask getBaseTask() {
		return baseTask;
	}
	public void setBaseTask(BaseTask baseTask) {
		this.baseTask = baseTask;
	}
	public RoleTask getRoleTask() {
		return roleTask;
	}
	public void setRoleTask(RoleTask roleTask) {
		this.roleTask = roleTask;
	}
	
	
	
}
