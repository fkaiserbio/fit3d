package de.bioforscher.fit3d.web.controllers;

import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.fit3d.web.utilities.LogHandler;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class CopyOfSessionController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 454000165789704379L;
	private UUID id;
	private JobController jobController;

	private Fit3DJob selectedJob;

	public CopyOfSessionController() {
		this.id = UUID.randomUUID();
	}

	@Deprecated
	public Fit3DJob findJob(UUID id) {

		for (Fit3DJob job : this.jobController.getManagedJobs().get(this.id)) {

			if (job.getId().equals(id)) {

				return job;
			}
		}

		return null;
	}

	public UUID getId() {
		return this.id;
	}

	public JobController getJobController() {
		return this.jobController;
	}

	public List<Fit3DMatch> getResultsForSelectedJob() {

		LogHandler.LOG.info("loading results for job" + this.selectedJob);

		String resultFilePath = this.selectedJob.getWorkingDirectory()
				+ "/results.fit";

		// TODO implement
		return null;
	}

	public Fit3DJob getSelectedJob() {
		return this.selectedJob;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setJobController(JobController jobController) {
		this.jobController = jobController;
	}

	public void setSelectedJob(Fit3DJob selectedJob) {
		this.selectedJob = selectedJob;
	}
}
