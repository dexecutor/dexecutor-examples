package com.github.dexecutor.migration.oxm;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MigrationTask {
	
	private String release;
	private String taskId;
	private String version;
	private String module;
	private int order;
	private String task;

	public String getTaskId() {
		return taskId;
	}

	@XmlAttribute
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getVersion() {
		return version;
	}

	@XmlAttribute
	public void setVersion(String version) {
		this.version = version;
	}

	public String getModule() {
		return module;
	}

	@XmlAttribute
	public void setModule(String module) {
		this.module = module;
	}

	public int getOrder() {
		return order;
	}

	@XmlAttribute
	public void setOrder(int order) {
		this.order = order;
	}
	
	
	public String getRelease() {
		return release;
	}

	@XmlAttribute
	public void setRelease(String release) {
		this.release = release;
	}

	public String getTask() {
		return task;
	}

	@XmlElement
	public void setTask(String task) {
		this.task = task;
	}

	@Override
	public String toString() {
		return String.format("MigrationTask [taskId=%s, version=%s, module=%s]", taskId, version, module);
	}
}
