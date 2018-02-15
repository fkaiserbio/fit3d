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
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class JobManager implements Serializable {

    private static final long serialVersionUID = 8788250546269904072L;
    private static final Logger logger = LoggerFactory.getLogger(JobManager.class);
    private MongoCollection<Document> mongoCollection;

    private JobExecutor jobExecutor;
    private JobLoadManager jobLoadManager;
    private MailNotifier mailNotifier;
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

        // schedule job cleaning
        TimerTask jobCleaningTask = new TimerTask() {
            @Override
            public void run() {
                logger.info("looking for expired jobs");
                List<Fit3DJob> oldJobs = managedJobs.values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(job -> job.getJobAgeInHours() > Fit3DWebConstants.JobManager.JOB_AGE_IN_HOURS)
                                                    .collect(Collectors.toList());
                for (Fit3DJob oldJob : oldJobs) {
                    oldJob.cancel();
                    try {
                        oldJob.delete();
                        Optional<List<Fit3DJob>> jobList = managedJobs.values().stream()
                                                                      .filter(jobs -> jobs.stream().anyMatch(job -> job.equals(oldJob)))
                                                                      .findFirst();
                        if (jobList.isPresent()) {
                            Iterator<Fit3DJob> iterator = jobList.get().iterator();
                            while (iterator.hasNext()) {
                                Fit3DJob currentJob = iterator.next();
                                if (currentJob.equals(oldJob)) {
                                    iterator.remove();
                                    logger.info("removed job {} from managed jobs", oldJob);
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.error("failed to delete old job {}", oldJob);
                    }
                }
            }
        };

        TimerTask jobNotificationTask = new TimerTask() {
            @Override
            public void run() {
                List<Fit3DJob> notifiableJobs = managedJobs.values().stream()
                                                    .flatMap(Collection::stream)
                                                    .filter(Fit3DJob::isSendMail)
                                                    .collect(Collectors.toList());
                for (Fit3DJob job : notifiableJobs) {
                    mailNotifier.sendMail(job);
                    job.setSendMail(false);
                    mongoCollection.updateOne(eq("jobIdentifier", job.toString()), new Document("$set", new Document()
                            .append("sendMail", false)));
                }
            }
        };

        Timer timer = new Timer(true);
        timer.schedule(loadMonitoringTask, 1000, Fit3DWebConstants.JobManager.LOAD_UPDATE_INTERVAL);
        timer.schedule(jobCleaningTask, 1000, Fit3DWebConstants.JobManager.CLEANUP_INTERVAL);
        timer.schedule(jobNotificationTask, 1000, 1000);
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

    public MailNotifier getMailNotifier() {
        return mailNotifier;
    }

    public void setMailNotifier(MailNotifier mailNotifier) {
        this.mailNotifier = mailNotifier;
    }

    public Map<UUID, List<Fit3DJob>> getManagedJobs() {
        return managedJobs;
    }

    public void setManagedJobs(Map<UUID, List<Fit3DJob>> managedJobs) {
        this.managedJobs = managedJobs;
    }
}
