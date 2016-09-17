package com.github.dexecutor.executor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dexecutor.core.DefaultDependentTasksExecutor;
import com.github.dexecutor.core.DefaultExecutionEngine;
import com.github.dexecutor.core.DependentTasksExecutor;
import com.github.dexecutor.core.DependentTasksExecutor.ExecutionBehavior;
import com.github.dexecutor.core.task.Task;
import com.github.dexecutor.core.task.TaskProvider;
import com.github.dexecutor.oxm.MigrationTask;
import com.github.dexecutor.oxm.MigrationTasks;
import com.github.dexecutor.parser.CompositeTableNameProvider;
import com.github.dexecutor.parser.SQLTableNameProvider;
import com.github.dexecutor.parser.TableNameProvider;
import com.github.dexecutor.support.Lists;

public class MigrationTasksExecutor {

	private static final Logger logger = LoggerFactory.getLogger(MigrationTasksExecutor.class);

	private final DependentTasksExecutor<String> executor;
	private final Map<String, List<String>> tableToTasksMap = new LinkedHashMap<String, List<String>>();
	private TableNameProvider tableNameProvider;

	public MigrationTasksExecutor(final MigrationTasks tasks, final ExecutorService executorService) {
		this.executor = new DefaultDependentTasksExecutor<String, String>(new DefaultExecutionEngine<String, String>(executorService), newTaskProvider(tasks));
		this.tableNameProvider = newTableNameProvider();
		buildGraph(tasks);		
	}
 
	private TableNameProvider newTableNameProvider() {
		List<TableNameProvider> newProviders = new ArrayList<TableNameProvider>();
		newProviders.add(new SQLTableNameProvider());
		return new CompositeTableNameProvider(newProviders);
	}

	private void buildGraph(MigrationTasks tasks) {
		for (MigrationTask migrationTask : tasks.getTasks()) {
			List<String> tables = tables(migrationTask);
			constructTaskNode(migrationTask, tables);
			recordTaskDetails(migrationTask, tables);
		}
	}

	private void constructTaskNode(MigrationTask migrationTask, List<String> tables) {
		List<String> taskIds = dependentTaskIds(migrationTask, tables);
		if (isDependentTask(taskIds)) {
			processDependentTasks(taskIds, migrationTask.getTaskId());
		} else {
			this.executor.addIndependent(migrationTask.getTaskId());
		}
	}

	private List<String> dependentTaskIds(MigrationTask migrationTask, List<String> tables) {
		List<String> result = new ArrayList<String>();

		for (String table : tables) {
			if (this.tableToTasksMap.containsKey(table)) {
				String lastTask = Lists.getLast(this.tableToTasksMap.get(table));
				if (lastTask != null && !lastTask.isEmpty()) {
					result.add(lastTask);					
				}
			}
		}
		return result;
	}

	private boolean isDependentTask(final List<String> taskIds) {
		return !taskIds.isEmpty();
	}

	private void processDependentTasks(final List<String> dependentTaskIds, final String currentTaskId) {
		for (String dependentTaskId : dependentTaskIds) {
			this.executor.addDependency(dependentTaskId, currentTaskId);
		}
	}

	private void recordTaskDetails(final MigrationTask migrationTask, final List<String> tables) {
		for (String table : tables) {
			List<String> tasks = processedTasks(table);
			tasks.add(migrationTask.getTaskId());
			this.tableToTasksMap.put(table, tasks);			
		}		
	}

	private List<String> processedTasks(final String table) {
		if (this.tableToTasksMap.containsKey(table)) {
			return this.tableToTasksMap.get(table);
		} else {
			return new ArrayList<String>();			
		}
	}

	private List<String> tables(final MigrationTask migrationTask) {
		return this.tableNameProvider.provideTableNames(migrationTask.getTask());
	}

	public void execute(boolean stopOnError) {
		this.executor.execute(ExecutionBehavior.RETRY_ONCE_TERMINATING);
	}

	private TaskProvider<String, String> newTaskProvider(MigrationTasks tasks) {
		return new DataMigrationTaskProvider(tasks);
	}

	private class DataMigrationTaskProvider implements TaskProvider<String, String> {

		public DataMigrationTaskProvider(MigrationTasks tasks) {

		}

		public Task<String, String> provideTask(String id) {
			return new DummyTask(id);
		}		
	}

	private static class DummyTask extends Task<String, String> {

		private static final long serialVersionUID = 1L;

		public DummyTask(final String id) {
			setId(id);
		}

		@Override
		public String execute() {
			logger.info("Executing Task {}", getId());
			return getId();
		}		
	}
}
