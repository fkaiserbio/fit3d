package bio.fkaiser.fit3d.web.model;

import bio.fkaiser.fit3d.web.utilities.Fit3DWebConstants;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3D;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DBuilder;
import de.bioforscher.singa.structure.algorithms.superimposition.fit3d.Fit3DMatch;
import de.bioforscher.singa.structure.model.oak.StructuralMotif;
import de.bioforscher.singa.structure.parser.pdb.structures.StructureParser;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.mongodb.client.model.Filters.eq;

/**
 * A class representing a Fit3D job.
 */
public class Fit3DJob implements Runnable, Serializable {

    private static final long serialVersionUID = -4519263362860178857L;
    private static final Logger logger = LoggerFactory.getLogger(Fit3DJob.class);

    /**
     * job information
     */
    private UUID sessionIdentifier;
    private UUID jobIdentifier;
    private Path jobPath;
    private String description;
    private String email;
    private LocalDateTime timeStamp;
    private String ipAddress;
    private Fit3DJobParameters parameters;

    /**
     * status information
     */
    private boolean enqueued = false;
    private boolean finished = false;
    private boolean running = false;
    private boolean failed = false;
    /**
     * results
     */
    private List<Fit3DMatch> matches;
    private Future<?> future;
    private Fit3D fit3d;

    public Fit3DJob() {

    }

    public Fit3DJob(UUID jobIdentifier, UUID sessionIdentifier, String ipAddress, String description, String email, Path jobPath, Fit3DJobParameters parameters) {

        // get new time stamp for job
        timeStamp = LocalDateTime.now();

        this.jobIdentifier = jobIdentifier;
        this.sessionIdentifier = sessionIdentifier;
        this.ipAddress = ipAddress;
        this.description = description;
        this.email = email;
        this.jobPath = jobPath;
        this.parameters = parameters;
    }

    @Override
    public void run() {

        MongoClient mongoClient = new MongoClient(Fit3DWebConstants.Database.DB_HOST, Fit3DWebConstants.Database.DB_PORT);
        MongoCollection<Document> mongoCollection =
                mongoClient.getDatabase(Fit3DWebConstants.Database.DB_NAME).getCollection(Fit3DWebConstants.Database.DB_COLLECTION_NAME);

        logger.info("starting job {}", this);

        running = true;
        enqueued = false;

        mongoCollection.updateOne(eq("jobIdentifier", jobIdentifier.toString()), new Document("$set", new Document()
                .append("running", true)
                .append("enqueued", false)));
        logger.info("status of job {} updated", this);

        try {
            startFit3D();
        } catch (Exception e) {
            failed = true;
            running = false;
            mongoCollection.updateOne(eq("jobIdentifier", jobIdentifier.toString()), new Document("$set", new Document()
                    .append("failed", true)
                    .append("running", false)));
            logger.error("job {} failed with error {}", this, e.getMessage(), e);
            return;
        }

        logger.info("finished job {}", this);

        finished = true;
        running = false;

        mongoCollection.updateOne(eq("jobIdentifier", jobIdentifier.toString()), new Document("$set", new Document()
                .append("finished", true)
                .append("running", false)));
        logger.info("status of job {} updated", this);

//        // send email notification
//        if (!email.isEmpty()) {
//            try {
//                MailNotifier.getInstance().sendNotificationMail(sessionIdentifier, this);
//            } catch (MessagingException | IOException e) {
//                logger.info("failed to send notification mail for job {}", this);
//            }
//        }
    }

    private void startFit3D() throws Exception {
        logger.info("starting Fit3D for job {}", this);
        StructuralMotif motif = StructuralMotif.fromLeafSubstructures(StructureParser.local()
                                                                                     .path(parameters.getMotifPath())
                                                                                     .parse().getAllLeafSubstructures());
        StructureParser.MultiParser multiParser;
        if (parameters.isChainTargetList()) {
            multiParser = StructureParser.local()
                                         .localPDB(Fit3DWebConstants.LOCAL_PDB)
                                         .chainList(parameters.getTargetListPath());
        } else {
            List<String> pdbIdentifiers = Files.readAllLines(parameters.getTargetListPath());
            multiParser = StructureParser.local()
                                         .localPDB(Fit3DWebConstants.LOCAL_PDB, pdbIdentifiers)
                                         .everything();
        }
        fit3d = Fit3DBuilder.create()
                            .query(motif)
                            .targets(multiParser)
                            .limitedParallelism(Fit3DWebConstants.CORES / Fit3DWebConstants.THREAD_POOL_SIZE)
                            .atomFilter(parameters.getAtomFilterType().getFilter())
                            .mapECNumbers()
                            .mapPfamIdentifiers()
                            .mapUniProtIdentifiers()
                            .run();

        matches = fit3d.getMatches();
    }

    public void writeMatches() {
        Path matchesPath = jobPath.resolve("matches");
        if (matchesPath.toFile().exists()) {
            logger.info("matches were already written for job {}", this);
            return;
        } else {
            fit3d.writeMatches(matchesPath, Fit3DWebConstants.STRUCTURE_OUTPUT_RMSD_LIMIT);
        }
    }

    public boolean cancel() {
        if (future != null) {
            return future.cancel(true);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return (description.equals("") ? "job" : ("'" + description + "'")) + " submitted at " + timeStamp.toString();
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

    public Fit3D getFit3d() {
        return fit3d;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void setFuture(Future<?> future) {
        this.future = future;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public UUID getJobIdentifier() {
        return jobIdentifier;
    }

    public void setJobIdentifier(UUID jobIdentifier) {
        this.jobIdentifier = jobIdentifier;
    }

    public Path getJobPath() {
        return jobPath;
    }

    public void setJobPath(Path jobPath) {
        this.jobPath = jobPath;
    }

    public List<Fit3DMatch> getMatches() {
        return matches;
    }

    public Fit3DJobParameters getParameters() {
        return parameters;
    }

    public void setParameters(Fit3DJobParameters parameters) {
        this.parameters = parameters;
    }

    public UUID getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(UUID sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
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

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isEnqueued() {
        return enqueued;
    }

    public void setEnqueued(boolean enqueue) {
        enqueued = enqueue;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
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
}
