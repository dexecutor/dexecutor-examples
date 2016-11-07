package com.github.dexecutor.flow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.github.dexecutor.core.support.ThreadPoolUtil;

public class WorkFlowApplication {

	public static void main(String[] args) {
		ExecutorService executor = buildExecutor();
		WorkFlowManager manager = new WorkFlowManager(executor);
		manager.execute();
		awaitTermination(executor);
	}
	
	private static ExecutorService buildExecutor() {
		ExecutorService executorService = Executors.newFixedThreadPool(ThreadPoolUtil.ioIntesivePoolSize());
		return executorService;
	}
	
	private static void awaitTermination(final ExecutorService executor) {
		try {
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {

		}
	}
}
