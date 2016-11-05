package com.github.dexecutor.migration.oxm;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="migration-tasks")
public class MigrationTasks {

	private List<MigrationTask> tasks;

	public MigrationTasks() {
		this.tasks = new ArrayList<MigrationTask>();
	}

	public List<MigrationTask> getTasks() {
		return tasks;
	}

	@XmlElement(name = "task")
	public void setTasks(List<MigrationTask> tasks) {
		this.tasks = tasks;
	}
	
	public void addTask(MigrationTask task) {
		this.tasks.add(task);
	}

	@Override
	public String toString() {
		return String.format("MigrationTasks [tasks=%s]", tasks);
	}	
}
