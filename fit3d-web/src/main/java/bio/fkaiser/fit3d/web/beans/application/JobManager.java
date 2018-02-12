package bio.fkaiser.fit3d.web.beans.application;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;
import bio.fkaiser.fit3d.web.converter.JobConverter;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Future;

public class JobManager implements Serializable {

    private static final long serialVersionUID = 8788250546269904072L;
    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
    private MongoCollection<Document> mongoCollection;

    private JobExecutor jobExecutor;
    private JobLoadManager jobLoadManager;
    private Map<UUID, List<Fit3DJob>> managedJobs = new HashMap<>();

    public JobManager() {
        logger.info("creating new database collection '{}' in database '{}'", Fit3DWebConstants.Database.DB_COLLECTION_NAME, Fit3DWebConstants.Database.DB_NAME);
        if (Fit3DWebConstants.Database.DROP_DB_ON_RESTART) {
            MongoClient mongoClient = new MongoClient(Fit3DWebConstants.Database.DB_HOST, Fit3DWebConstants.Database.DB_PORT);
            mongoCollection = mongoClient.getDatabase(Fit3DWebConstants.Database.DB_NAME).getCollection(Fit3DWebConstants.Database.DB_COLLECTION_NAME);
            mongoCollection.drop();
        }
    }

    public void addJob(Fit3DJob job) {

        Document jobObject = JobConverter.toDocument(job);
        mongoCollection.insertOne(jobObject);
        logger.info("new job {} added to database", job);

        // add jobs to existing jobs of current session
        UUID sessionIdentifier = job.getSessionIdentifier();
        if (managedJobs.containsKey(sessionIdentifier)) {
            managedJobs.get(sessionIdentifier).add(job);
        } else {
            List<Fit3DJob> jobs = new ArrayList<>();
            jobs.add(job);
            managedJobs.put(sessionIdentifier, jobs);
        }

        // enqueue new job for execution
        Future<?> future = jobExecutor.enqueue(job);
        job.setFuture(future);
    }

    public void cancelJob(Fit3DJob job) {
        UUID jobIdentifier = job.getJobIdentifier();
        Optional<Fit3DJob> optionalJob = managedJobs.values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(managedJob -> managedJob.getJobIdentifier().equals(jobIdentifier))
                                                    .findFirst();
        optionalJob.ifPresent(Fit3DJob::cancel);
        if (optionalJob.isPresent()) {
            Fit3DJob managedJob = optionalJob.get();
            managedJob.cancel();
            logger.info("sent cancel signal to job {}", managedJob);
        } else {
            logger.info("job with identifier not found {}", job.getJobIdentifier());
        }
    }

    @PostConstruct
    public void init() {
        // schedule updating of load for execution
        TimerTask loadMonitoringTask = new TimerTask() {
            @Override
            public void run() {

                int runningJobCount = (int) managedJobs.values().stream()
                                                       .flatMap(Collection::stream)
                                                       .filter(Fit3DJob::isRunning)
                                                       .count();
                int enqueuedJobCount = (int) managedJobs.values().stream()
                                                        .flatMap(Collection::stream)
                                                        .filter(Fit3DJob::isEnqueued)
                                                        .count();
                int finishedJobCount = (int) managedJobs.values().stream()
                                                        .flatMap(Collection::stream)
                                                        .filter(Fit3DJob::isFinished)
                                                        .count();
                int failedJobCount = (int) managedJobs.values().stream()
                                                      .flatMap(Collection::stream)
                                                      .filter(Fit3DJob::isFailed)
                                                      .count();

                jobLoadManager.setRunningJobCount(runningJobCount);
                jobLoadManager.setEnqueuedJobCount(enqueuedJobCount);
                jobLoadManager.setFinishedJobCount(finishedJobCount);
                jobLoadManager.setFailedJobCount(failedJobCount);
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(loadMonitoringTask, 1000, 1000);
    }

    public JobExecutor getJobExecutor() {
        return jobExecutor;
    }

    public void setJobExecutor(JobExecutor jobExecutor) {
        this.jobExecutor = jobExecutor;
    }

    public JobLoadManager getJobLoadManager() {
        return jobLoadManager;
    }

    public void setJobLoadManager(JobLoadManager jobLoadManager) {
        this.jobLoadManager = jobLoadManager;
    }

    public Map<UUID, List<Fit3DJob>> getManagedJobs() {
        return managedJobs;
    }

    public void setManagedJobs(Map<UUID, List<Fit3DJob>> managedJobs) {
        this.managedJobs = managedJobs;
    }
}
