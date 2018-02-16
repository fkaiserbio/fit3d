package bio.fkaiser.fit3d.web.beans.view;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;
import bio.fkaiser.fit3d.web.beans.application.JobManager;
import bio.fkaiser.fit3d.web.converter.JobConverter;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fk
 */
public class AdminView {

    private List<Fit3DJob> jobs;
    private JobManager jobManager;

    @PostConstruct
    public void init() {
        MongoClient mongoClient = new MongoClient(Fit3DWebConstants.Database.DB_HOST, Fit3DWebConstants.Database.DB_PORT);
        MongoCollection<Document> mongoCollection = mongoClient.getDatabase(Fit3DWebConstants.Database.DB_NAME).getCollection(Fit3DWebConstants.Database.DB_COLLECTION_NAME);
        FindIterable<Document> documents = mongoCollection.find();
        jobs = new ArrayList<>();
        for (Document document : documents) {
            Fit3DJob job = JobConverter.toFit3DJob(document);
            jobs.add(job);
        }
    }

    public void cancelJob(Fit3DJob job) {
        jobManager.cancelJob(job);
        RequestContext.getCurrentInstance().update("jobForm");
    }

    public JobManager getJobManager() {
        return jobManager;
    }

    public void setJobManager(JobManager jobManager) {
        this.jobManager = jobManager;
    }

    public List<Fit3DJob> getJobs() {
        return jobs;
    }

    public void setJobs(List<Fit3DJob> jobs) {
        this.jobs = jobs;
    }
}
