package bio.fkaiser.fit3d.web.beans.session;

import bio.fkaiser.fit3d.web.beans.application.JobManager;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class SessionManager implements Serializable {

    public static final Path BASE_PATH = Paths.get(FacesContext.getCurrentInstance().getExternalContext().getRealPath("/data"));
    private static final long serialVersionUID = 3604747342156591226L;
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private UUID sessionIdentifier;
    private Path sessionPath;
    private JobManager jobManager;
    private Fit3DJob selectedJob;

    /**
     * Returns a {@link Path} relativized to the working path "data/".
     *
     * @param path The {@link Path} to be relativized.
     * @return The relativzed {@link Path} for external static access.
     */
    public static Path relativizePath(Path path) {
        return Paths.get("data/").resolve(BASE_PATH.relativize(path));
    }

    @PostConstruct
    public void init() {
        sessionIdentifier = UUID.randomUUID();
        logger.info("initializing new session with id {}", sessionIdentifier);
        sessionPath = BASE_PATH.resolve(sessionIdentifier.toString());
        //create directories
        try {
            Files.createDirectories(sessionPath);
        } catch (IOException e) {
            logger.error("failed to create working directory for session {}", sessionIdentifier, e);
        }
        logger.info("created working path of session is {}", sessionPath);
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

    public UUID getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(UUID sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public Path getSessionPath() {
        return sessionPath;
    }
}