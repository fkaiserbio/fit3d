package de.bioforscher.fit3d.web.core;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.bioforscher.fit3d.web.utilities.LoadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy job, that does no calculation but waits for a defined time.
 *
 * @author fkaiser
 */
public class Fit3DJobDummy extends Fit3DJob {

    private static final long serialVersionUID = -5240127430082643295L;
    private static final Logger logger = LoggerFactory.getLogger(Fit3DJobDummy.class);

    private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(2);

    public Fit3DJobDummy(UUID jobId, UUID sessionId, Date timeStamp, String description, String email, Path workingDirectoryPath, JobParameters parameters) {
        super(jobId, sessionId, timeStamp, description, email, workingDirectoryPath, parameters);
    }

    @Override
    public void run() {

        try {
            logger.info("starting dummy job {}", this);
            setRunning(true);
            setEnqueued(false);

            Thread.sleep(SLEEP_TIME);

            logger.info("finished dummy job {}", this);
            setFinished(true);
            setRunning(false);

            // count load monitor
            LoadMonitor.getInstance().countDown();

        } catch (InterruptedException e) {
            logger.warn(e.getMessage() + " " + this);
        }
    }

}
