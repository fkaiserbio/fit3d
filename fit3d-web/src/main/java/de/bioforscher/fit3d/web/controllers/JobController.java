package de.bioforscher.fit3d.web.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.fit3d.web.services.JobExecutorService;
import de.bioforscher.fit3d.web.services.JobJanitorService;
import de.bioforscher.fit3d.web.utilities.LoadMonitor;

public class JobController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4322178419279421056L;

	private JobExecutorService jobExecutorService;
	private JobJanitorService jobVanitorService;

	private Map<UUID, List<Fit3DJob>> managedJobs = new HashMap<>();

	/**
	 * adds a new job for a specific UUID
	 * 
	 * @param id
	 * @param job
	 */
	public void addNewJob(UUID id, Fit3DJob job) {

		// add jobs to existing UUID-associated list
		if (this.managedJobs.containsKey(id)) {

			this.managedJobs.get(id).add(job);
		} else {

			// put job to new UUID-associated list
			List<Fit3DJob> newJobList = new ArrayList<>();
			newJobList.add(job);
			this.managedJobs.put(id, newJobList);
		}

		// enqueue new job for execution
		this.jobExecutorService.enqueue(job);

		// count up load monitor
		LoadMonitor.getInstance().countUp();
	}

	public JobExecutorService getJobExecutorService() {
		return this.jobExecutorService;
	}

	public JobJanitorService getJobVanitorService() {
		return this.jobVanitorService;
	}

	public Map<UUID, List<Fit3DJob>> getManagedJobs() {

		return this.managedJobs;
	}

	@PostConstruct
	public void init() {

		FacesContext.getCurrentInstance().getExternalContext().getSession(true);
	}

	public void setJobExecutorService(JobExecutorService jobExecutorService) {
		this.jobExecutorService = jobExecutorService;
	}

	public void setJobVanitorService(JobJanitorService jobVanitorService) {
		this.jobVanitorService = jobVanitorService;
	}

	public void setManagedJobs(Map<UUID, List<Fit3DJob>> managedJobs) {

		this.managedJobs = managedJobs;
	}
}
