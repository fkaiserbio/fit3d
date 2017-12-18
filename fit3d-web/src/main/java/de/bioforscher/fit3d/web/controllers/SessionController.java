package de.bioforscher.fit3d.web.controllers;

import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class SessionController implements Serializable {

    private static final long serialVersionUID = 3604747342156591226L;
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    // TODO change to real matching path in docker (e.g. /srv/data) and add to docker run command (-v)
    private static final Path BASE_PATH = Paths.get("/home/fkaiser/Workspace/IdeaProjects/fit3d/fit3d-web/src/main/webapp/data");

    private UUID sessionIdentifier;
    private Path workingPath;
    private JobController jobController;
    private Fit3DJob selectedJob;

    public List<Fit3DMatch> getResultsForSelectedJob() {
        logger.info("loading results for job {}", selectedJob);
        return selectedJob.getResults();
    }

    @PostConstruct
    public void init() {
        sessionIdentifier = UUID.randomUUID();
        logger.info("initializing new session with id {}", sessionIdentifier);
        workingPath = BASE_PATH.resolve(sessionIdentifier.toString());
        //create directories
        try {
            Files.createDirectories(workingPath);
        } catch (IOException e) {
            logger.error("failed to create working directory for session {}", sessionIdentifier, e);
        }
        logger.info("created working path of session is {}", workingPath);
    }

    public UUID getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(UUID sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public Path getWorkingPath() {
        return workingPath;
    }

    public JobController getJobController() {
        return jobController;
    }

    public void setJobController(JobController jobController) {
        this.jobController = jobController;
    }

    public Fit3DJob getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(Fit3DJob selectedJob) {
        this.selectedJob = selectedJob;
    }
}
