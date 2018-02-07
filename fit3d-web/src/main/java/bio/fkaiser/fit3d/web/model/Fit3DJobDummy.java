package bio.fkaiser.fit3d.web.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A dummy job, that does no calculation but waits for a defined time.
 *
 * @author fkaiser
 */
public class Fit3DJobDummy extends Fit3DJob {

    private static final long serialVersionUID = -5240127430082643295L;
    private static final Logger logger = LoggerFactory.getLogger(Fit3DJobDummy.class);

    private static final long SLEEP_TIME = TimeUnit.SECONDS.toMillis(2);

    public Fit3DJobDummy(UUID jobId, UUID sessionId, Date timeStamp, String description, String email, Path workingDirectoryPath, Fit3DJobParameters parameters) {
//        super(jobId, sessionId, timeStamp, description, email, workingDirectoryPath, parameters);
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

        } catch (InterruptedException e) {
            logger.warn(e.getMessage() + " " + this);
        }
    }

}
