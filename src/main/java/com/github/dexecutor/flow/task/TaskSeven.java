package com.github.dexecutor.flow.task;

import com.github.dexecutor.core.task.Task;

public class TaskSeven extends Task<String, Boolean> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "TaskSeven";

	public TaskSeven() {
		setId(NAME);
	}

	@Override
	public Boolean execute() {
		boolean result = true;
		System.out.println("Executing " + getId() + " , result : " + result);
		return result;
	}
}