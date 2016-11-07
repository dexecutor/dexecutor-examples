package com.github.dexecutor.flow;

import java.util.concurrent.ExecutorService;

import com.github.dexecutor.core.DefaultDexecutor;
import com.github.dexecutor.core.Dexecutor;
import com.github.dexecutor.core.DexecutorConfig;
import com.github.dexecutor.core.ExecutionConfig;
import com.github.dexecutor.flow.task.TaskFive;
import com.github.dexecutor.flow.task.TaskFour;
import com.github.dexecutor.flow.task.TaskOne;
import com.github.dexecutor.flow.task.TaskSeven;
import com.github.dexecutor.flow.task.TaskSix;
import com.github.dexecutor.flow.task.TaskThree;
import com.github.dexecutor.flow.task.TaskTwo;
import com.github.dexecutor.flow.task.WorkFlowTaskProvider;

public class WorkFlowManager {

	private Dexecutor<String, Boolean> dexecutor;

	public WorkFlowManager(ExecutorService executorService) {
		DexecutorConfig<String, Boolean> config = new DexecutorConfig<>(executorService, new WorkFlowTaskProvider());
		this.dexecutor = new DefaultDexecutor<>(config);

		buildGraph();
	}

	private void buildGraph() {
		this.dexecutor.addDependency(TaskOne.NAME, TaskTwo.NAME);
		this.dexecutor.addDependency(TaskTwo.NAME, TaskThree.NAME);
		this.dexecutor.addDependency(TaskTwo.NAME, TaskFour.NAME);
		this.dexecutor.addDependency(TaskTwo.NAME, TaskFive.NAME);
		this.dexecutor.addDependency(TaskFive.NAME, TaskSix.NAME);
		this.dexecutor.addAsDependentOnAllLeafNodes(TaskSeven.NAME);
	}

	public void execute() {
		this.dexecutor.execute(ExecutionConfig.TERMINATING);
	}
}
