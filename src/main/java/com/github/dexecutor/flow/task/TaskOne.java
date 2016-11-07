package com.github.dexecutor.flow.task;

import com.github.dexecutor.core.task.Task;

public class TaskOne extends Task<String, Boolean> {

	public static final String NAME = "TaskOne";

	public TaskOne() {
		setId(NAME);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Boolean execute() {
		boolean result = true;
		System.out.println("Executing " + getId() + " , result : " + result);
		return result;
	}
}
