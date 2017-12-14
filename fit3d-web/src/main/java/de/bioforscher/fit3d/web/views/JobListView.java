package de.bioforscher.fit3d.web.views;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;

import de.bioforscher.fit3d.web.controllers.JobController;
import de.bioforscher.fit3d.web.controllers.SessionController;
import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.fit3d.web.utilities.LogHandler;

public class JobListView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5103812694748897508L;

	private List<Fit3DJob> associatedJobs;

	private JobController jobJontroller;

	private Fit3DJob selectedJob;

	private SessionController sessionController;

	public List<Fit3DJob> getAssociatedJobs() {
		return this.associatedJobs;
	}

	public JobController getJobJontroller() {
		return this.jobJontroller;
	}

	public Fit3DJob getSelectedJob() {
		return this.selectedJob;
	}

	public SessionController getSessionController() {
		return this.sessionController;
	}

	@PostConstruct
	public void init() {

		this.associatedJobs = this.jobJontroller.getManagedJobs().get(
				this.sessionController.getId());
	}

	public void listener() {

		System.out.println("B");
	}

	public String selectJob() {

		this.sessionController.setSelectedJob(this.selectedJob);

		LogHandler.LOG.info("redirecting to detailed results");

		return "success";
	}

	public void setAssociatedJobs(List<Fit3DJob> selectedJob) {
		this.associatedJobs = selectedJob;
	}

	public void setJobJontroller(JobController jobJontroller) {
		this.jobJontroller = jobJontroller;
	}

	public void setSelectedJob(Fit3DJob selectedJob) {
		this.selectedJob = selectedJob;
	}

	public void setSessionController(SessionController sessionController) {
		this.sessionController = sessionController;
	}
}
