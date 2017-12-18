package de.bioforscher.fit3d.web.views;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import de.bioforscher.fit3d.web.controllers.JobController;
import de.bioforscher.fit3d.web.controllers.SessionController;
import de.bioforscher.fit3d.web.core.Fit3DJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobListView implements Serializable {

    private static final long serialVersionUID = 6696995028085411645L;
    private static final Logger logger = LoggerFactory.getLogger(JobListView.class);

    private List<Fit3DJob> associatedJobs;

    private JobController jobJontroller;

    private Fit3DJob selectedJob;

    private SessionController sessionController;

    public List<Fit3DJob> getAssociatedJobs() {
        return associatedJobs;
    }

    public void setAssociatedJobs(List<Fit3DJob> selectedJob) {
        associatedJobs = selectedJob;
    }

    public JobController getJobJontroller() {
        return jobJontroller;
    }

    public void setJobJontroller(JobController jobJontroller) {
        this.jobJontroller = jobJontroller;
    }

    public Fit3DJob getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(Fit3DJob selectedJob) {
        this.selectedJob = selectedJob;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    @PostConstruct
    public void init() {

        associatedJobs = jobJontroller.getManagedJobs().get(sessionController.getSessionIdentifier());
    }

    public void listener() {

        System.out.println("B");
    }

    public String selectJob() {

        sessionController.setSelectedJob(selectedJob);

        logger.info("redirecting to detailed results");

        return "success";
    }
}
