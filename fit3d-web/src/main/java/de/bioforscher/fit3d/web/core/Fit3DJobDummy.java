package de.bioforscher.fit3d.web.core;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.bioforscher.fit3d.web.utilities.LoadMonitor;
import de.bioforscher.fit3d.web.utilities.LogHandler;

/**
 * A dummy job, that does no calculation but waits for a defined time.
 * 
 * @author fkaiser
 *
 */
public class Fit3DJobDummy extends Fit3DJob {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3362504070533220168L;
	private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(2);

	public Fit3DJobDummy(UUID id, UUID sessionId, Date timeStamp, String description, String email, String workingDirectory, String commandLine, JobParameters parameters) {
		super(id, sessionId, timeStamp, description, email, workingDirectory, commandLine, parameters);
	}

	@Override
	public void run() {

		try {

			LogHandler.LOG.info("starting dummy job " + this);
			setRunning(true);
			setEnqueued(false);

			Thread.sleep(SLEEP_TIME);

			LogHandler.LOG.info("finished dummy job " + this);
			setFinished(true);
			setRunning(false);

			// count load monitor
			LoadMonitor.getInstance().countDown();

		} catch (InterruptedException e) {

			LogHandler.LOG.warning(e.getMessage() + " " + this);
		}
	}

}
