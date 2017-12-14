package de.bioforscher.fit3d.web.utilities;

import java.io.Serializable;

public class LoadMonitor implements Serializable {

	private static LoadMonitor instance = new LoadMonitor();

	/**
	 * 
	 */
	private static final long serialVersionUID = -3830192140495906886L;

	public static LoadMonitor getInstance() {

		return instance;
	}

	private int enqueued = 0;

	private LoadMonitor() {

	}

	public void countDown() {

		this.enqueued--;
	}

	public void countUp() {

		this.enqueued++;
	}

	public int getEnqueued() {
		return this.enqueued;
	}

	public void setEnqueued(int enqueued) {
		this.enqueued = enqueued;
	}

}
