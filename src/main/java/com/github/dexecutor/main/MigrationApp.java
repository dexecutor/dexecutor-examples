package com.github.dexecutor.main;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.dexecutor.core.support.ThreadPoolUtil;
import com.github.dexecutor.executor.MigrationTasksExecutor;
import com.github.dexecutor.oxm.MigrationTasks;

public class MigrationApp {

	public static void main(String[] args) throws JAXBException {
		ExecutorService executor = buildExecutor();
		new MigrationTasksExecutor(buildTasks(), executor).execute();		
		awaitTermination(executor);
	}

	private static ExecutorService buildExecutor() {
		ExecutorService executorService = Executors.newFixedThreadPool(ThreadPoolUtil.ioIntesivePoolSize());
		return executorService;
	}

	private static MigrationTasks buildTasks() throws JAXBException {
		InputStream file =  MigrationApp.class.getClassLoader().getResourceAsStream("tasks.xml");
		JAXBContext jaxbContext = JAXBContext.newInstance(MigrationTasks.class);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (MigrationTasks) jaxbUnmarshaller.unmarshal(file);
	}

	private static void awaitTermination(final ExecutorService executor) {
		try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
	}
}
