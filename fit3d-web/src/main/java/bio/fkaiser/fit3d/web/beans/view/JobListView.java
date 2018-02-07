package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.beans.application.JobManager;
import bio.fkaiser.fit3d.web.beans.session.SessionManager;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;

public class JobListView implements Serializable {

    private static final long serialVersionUID = 6696995028085411645L;
    private static final Logger logger = LoggerFactory.getLogger(JobListView.class);

    private List<Fit3DJob> associatedJobs;

    private JobManager jobManager;

    private Fit3DJob selectedJob;

    private SessionManager sessionManager;

    @PostConstruct
    public void init() {
        associatedJobs = jobManager.getManagedJobs().get(sessionManager.getSessionIdentifier());
    }

    public String selectJob() {
        sessionManager.setSelectedJob(selectedJob);
        logger.info("redirecting to results");
        return "success";
    }

    public List<Fit3DJob> getAssociatedJobs() {
        return associatedJobs;
    }

    public void setAssociatedJobs(List<Fit3DJob> selectedJob) {
        associatedJobs = selectedJob;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public Fit3DJob getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(Fit3DJob selectedJob) {
        this.selectedJob = selectedJob;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
