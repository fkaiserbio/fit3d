package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.beans.application.JobManager;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

/**
 * @author fk
 */
public class ConfirmView {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmView.class);
    private JobManager jobManager;
    private Fit3DJob job;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Flash flash = facesContext.getExternalContext().getFlash();
        job = (Fit3DJob) flash.get("job");
        logger.info("received job {}", job);
    }

    public String submit() {
        jobManager.addJob(job);
        logger.info("job {} with ID {} submitted", job, job.getJobIdentifier());
        return "success";
    }

    public Fit3DJob getJob() {
        return job;
    }

    public void setJob(Fit3DJob job) {
        this.job = job;
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }
}
