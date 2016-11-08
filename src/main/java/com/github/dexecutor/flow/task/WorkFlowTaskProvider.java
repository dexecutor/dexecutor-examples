package com.github.dexecutor.flow.task;

import java.util.HashMap;
import java.util.Map;

import com.github.dexecutor.core.task.Task;
import com.github.dexecutor.core.task.TaskProvider;

public class WorkFlowTaskProvider implements TaskProvider<String, Boolean> {

	private final Map<String, Task<String, Boolean>> tasks = new HashMap<String, Task<String, Boolean>>() {

		private static final long serialVersionUID = 1L;
		{
			put(TaskOne.NAME, new TaskOne());
			put(TaskTwo.NAME, new TaskTwo());
			put(TaskThree.NAME, new TaskThree());
			put(TaskFour.NAME, new TaskFour());
			put(TaskFive.NAME, new TaskFive());
			put(TaskSix.NAME, new TaskSix());
			put(TaskSeven.NAME, new TaskSeven());
		}
	};

	@Override
	public Task<String, Boolean> provideTask(final String id) {
		return this.tasks.get(id);
	}
}
