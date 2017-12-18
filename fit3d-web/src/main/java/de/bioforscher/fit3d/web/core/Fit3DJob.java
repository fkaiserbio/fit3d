package de.bioforscher.fit3d.web.core;

import de.bioforscher.fit3d.web.utilities.MailNotifier;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * A class representing a Fit3D job.
 */
public class Fit3DJob implements Runnable, Serializable {

    private static final long serialVersionUID = -4519263362860178857L;
    private static final Logger logger = LoggerFactory.getLogger(Fit3DJob.class);

    private Path workingDirectoryPath;
    private String description;
    private String email;
    private boolean enqueued = false;
    private boolean finished = false;
    private UUID jobId;
    private boolean running = false;

    private JobParameters parameters;

    private Date timeStamp;

    private UUID sessionId;
    transient private Process process;
    private List<Fit3DMatch> results;

    public Fit3DJob(UUID jobId, UUID sessionId, Date timeStamp, String description, String email, Path workingDirectoryPath, JobParameters parameters) {
        this.jobId = jobId;
        this.sessionId = sessionId;
        this.timeStamp = timeStamp;
        this.description = description;
        this.email = email;
        this.workingDirectoryPath = workingDirectoryPath;
        this.parameters = parameters;
    }

    public Path getWorkingDirectoryPath() {
        return workingDirectoryPath;
    }

    public void setWorkingDirectoryPath(Path workingDirectoryPath) {
        this.workingDirectoryPath = workingDirectoryPath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public JobParameters getParameters() {
        return parameters;
    }

    public void setParameters(JobParameters parameters) {
        this.parameters = parameters;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getStatus() {
        if (enqueued) {
            return "enqueued";
        }
        if (running) {
            return "running";
        }
        if (finished) {
            return "finished";
        }
        return "new";
    }

    public boolean isEnqueued() {
        return enqueued;
    }

    public void setEnqueued(boolean enqueue) {
        enqueued = enqueue;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {

        // TODO implement
        logger.info("starting job {}", this);

        // run Fit3D here
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        running = true;
        enqueued = false;

        logger.info("finished job " + this);

        // send email notification
        if (!email.isEmpty()) {
            try {
                MailNotifier.getInstance().sendNotificationMail(sessionId, this);
            } catch (MessagingException | IOException e) {
                logger.info("failed to send notification mail for job {}", this);
            }
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return description + " submitted at " + df.format(timeStamp);
    }

    public List<Fit3DMatch> getResults() {
        return results;
    }
}
