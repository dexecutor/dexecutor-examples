package com.github.dexecutor.main;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.dexecutor.core.support.ThreadPoolUtil;
import com.github.dexecutor.executor.MigrationTasksExecutor;
import com.github.dexecutor.oxm.MigrationTasks;

public class MigrationApp {

	public static void main(String[] args) throws JAXBException {
		MigrationTasks tasks = buildTasks();
		ExecutorService executorService = buildExecutor();

		new MigrationTasksExecutor(tasks, executorService).execute(true);
		
		executorService.shutdown();
	}

	private static MigrationTasks buildTasks() throws JAXBException {
		InputStream file =  MigrationApp.class.getClassLoader().getResourceAsStream("tasks.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(MigrationTasks.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		MigrationTasks tasks = (MigrationTasks) jaxbUnmarshaller.unmarshal(file);
		return tasks;
	}

	private static ExecutorService buildExecutor() {
		ExecutorService executorService = Executors.newFixedThreadPool(ThreadPoolUtil.ioIntesivePoolSize());
		return executorService;
	}
}
