package de.bioforscher.fit3d.web.utilities;

public class LoadMonitorBean {

	private LoadMonitor loadMonitor;

	public LoadMonitorBean() {

		this.loadMonitor = LoadMonitor.getInstance();
	}

	public LoadMonitor getLoadMonitor() {
		return this.loadMonitor;
	}
}
