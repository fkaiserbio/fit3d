package bio.fkaiser.fit3d.web.beans.application;

/**
 * @author fk
 */
public class JobLoadManager {

    private int runningJobCount;
    private int enqueuedJobCount;
    private int finishedJobCount;
    private int failedJobCount;

    public int getEnqueuedJobCount() {
        return enqueuedJobCount;
    }

    public void setEnqueuedJobCount(int enqueuedJobCount) {
        this.enqueuedJobCount = enqueuedJobCount;
    }

    public int getFailedJobCount() {
        return failedJobCount;
    }

    public void setFailedJobCount(int failedJobCount) {
        this.failedJobCount = failedJobCount;
    }

    public int getFinishedJobCount() {
        return finishedJobCount;
    }

    public void setFinishedJobCount(int finishedJobCount) {
        this.finishedJobCount = finishedJobCount;
    }

    public int getRunningJobCount() {
        return runningJobCount;
    }

    public void setRunningJobCount(int runningJobCount) {
        this.runningJobCount = runningJobCount;
    }

}
