package de.bioforscher.fit3d.web.services;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.bioforscher.fit3d.web.core.Fit3DJob;

public class JobExecutorService implements Serializable {

	public static final int CORES = Runtime.getRuntime().availableProcessors();

	public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

	/**
	 * 
	 */
	private static final long serialVersionUID = 7937388794327541006L;

	transient private ExecutorService executor = Executors
			.newSingleThreadExecutor();

	public void enqueue(Fit3DJob job) {

		this.executor.submit(job);

		job.setEnqueued(true);
	}

	public ExecutorService getExecutor() {
		return this.executor;
	}

}