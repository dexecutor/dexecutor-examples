package com.github.dexecutor.flow.task;

import java.util.Random;

import com.github.dexecutor.core.task.Task;

public class TaskTwo extends Task<String, Boolean> {
	
	public static final String NAME = "TaskTwo";

	public TaskTwo() {
		setId(NAME);
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Boolean execute() {
		boolean result = new Random().nextBoolean();
		System.out.println("Executing " + getId() + " , result : " + result);
		return result;
	}
}
