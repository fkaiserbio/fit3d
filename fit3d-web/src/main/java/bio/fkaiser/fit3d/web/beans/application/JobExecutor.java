package bio.fkaiser.fit3d.web.beans.application;

import bio.fkaiser.fit3d.web.model.Fit3DJob;
import bio.fkaiser.fit3d.web.utilities.Fit3DWebConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JobExecutor implements Serializable {

    private static final long serialVersionUID = 7937388794327541006L;
    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    transient private ExecutorService executor = Executors.newWorkStealingPool(Fit3DWebConstants.THREAD_POOL_SIZE);

    public Future<?> enqueue(Fit3DJob job) {
        Future<?> future = executor.submit(job);
        job.setEnqueued(true);
        logger.info("job {} enqueued for execution", job);
        return future;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}