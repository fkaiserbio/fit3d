package de.bioforscher.fit3d.web.services;

import de.bioforscher.fit3d.web.controllers.JobController;
import de.bioforscher.fit3d.web.core.Fit3DJob;
import de.bioforscher.fit3d.web.core.Fit3DJobDummy;
import de.bioforscher.fit3d.web.utilities.LogHandler;

import javax.annotation.PostConstruct;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JobJanitorService extends Timer {

    private static final long TIMELIMIT = TimeUnit.HOURS.toMillis(72);

    private static final long PERIOD = TimeUnit.HOURS.toMillis(1);

    private static final long INITIAL_DELAY = TimeUnit.MINUTES.toMillis(1);

    private JobController jobController;
    private Date genesis;

    public Date getGenesis() {
        return this.genesis;
    }

    public void setGenesis(Date genesis) {
        this.genesis = genesis;
    }

    public JobController getJobController() {
        return this.jobController;
    }

    public void setJobController(JobController jobController) {
        this.jobController = jobController;
    }

    @PostConstruct
    public void init() {

        this.genesis = new Date();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        LogHandler.LOG.info("registering job janitor service at " + df.format(this.genesis));

        TimerTask t = new TimerTask() {

            @Override
            public void run() {

                LogHandler.LOG.info("looking for old jobs");

                Iterator<List<Fit3DJob>> it = JobJanitorService.this.jobController.getManagedJobs().values().iterator();

                while (it.hasNext()) {

                    List<Fit3DJob> jobs = it.next();

                    for (Iterator<Fit3DJob> iterator = jobs.iterator(); iterator.hasNext(); ) {
                        Fit3DJob job = iterator.next();
                        long timeDiff = new Date().getTime() - job.getTimeStamp().getTime();

                        if (timeDiff > TIMELIMIT) {

                            LogHandler.LOG.info("removing job " + job);

                            if (!(job instanceof Fit3DJobDummy)) {

                                // kill the job if it is still running
                                if (job.getProcess().isAlive()) {

                                    LogHandler.LOG.info("job " + job + " is still running, now terminating");
                                    job.getProcess().destroyForcibly();

                                    // TODO handle email notification that
                                    // calculation was not successful
                                }

                                File workingDirectory = new File(job.getWorkingDirectory());

                                // delete working directory
//								TODO implement
//								try {
//									FileUtils.deleteDirectory(workingDirectory);
//								} catch (IOException e) {
//									LogHandler.LOG.warning("error while deleting job directory " + workingDirectory);
//								}

                                // delete session directory if empty or contains
                                // only extract PDB file
                                // TODO delete if contains only motif or extract
                                // pdb file
                                File sessionDirectory = workingDirectory.getParentFile();

                                // long cutoffDate = new Date().getTime() - new
                                // Date(TIMELIMIT).getTime();
                                //
                                // deleteOrphanedDirectories(sessionDirectory,
                                // cutoffDate);

                                if (sessionDirectory.list().length == 0 || containsNoJobs(sessionDirectory)) {

                                    LogHandler.LOG.info("removing empty session folder");

                                    // TODO implement
//                                    try {
//                                        FileUtils.deleteDirectory(sessionDirectory);
//                                    } catch (IOException e) {
//                                        LogHandler.LOG
//                                                .warning("error while deleting session directory " + sessionDirectory);
//                                    }
                                }
                            }

                            // remove registered job
                            iterator.remove();

                        } else {

                            LogHandler.LOG.info("job " + job + " not expired");
                        }
                    }
                }
            }

            private boolean containsNoJobs(File sessionDirectory) {

                // TODO implement
//                int dirCountMotif = FileUtils
//                        .listFiles(sessionDirectory, FileFilterUtils.prefixFileFilter("motif"), null).size();
//                int dirCountExtract = FileUtils
//                        .listFiles(sessionDirectory, FileFilterUtils.prefixFileFilter("extract"), null).size();
//
//                int dirCountAll = FileUtils
//                        .listFilesAndDirs(sessionDirectory, TrueFileFilter.INSTANCE, DirectoryFileFilter.DIRECTORY)
//                        .size();
//
//                if (dirCountAll == (dirCountMotif + dirCountExtract)) {
//
//                    return true;
//                } else {
//
//                    return false;
//                }

                // if (sessionDirectory.list().length == 2
                // && FileUtils.directoryContains(sessionDirectory,
                // new File(sessionDirectory.toString() + "/extract.pdb"))
                // && FileUtils.directoryContains(sessionDirectory,
                // new File(sessionDirectory.toString() + "/motif.pdb"))) {
                //
                // return true;
                // } else if (sessionDirectory.list().length == 1 &&
                // (FileUtils.directoryContains(sessionDirectory,
                // new File(sessionDirectory.toString() + "/extract.pdb"))
                // || FileUtils.directoryContains(sessionDirectory,
                // new File(sessionDirectory.toString() + "/motif.pdb")))) {
                //
                return true;
                // }

            }

            @SuppressWarnings("unused")
            private void deleteOrphanedDirectories(File sessionDirectory, long cutoffDate) {
                // TODO implement
//                // TODO Auto-generated method stub
//
//                Collection<File> filesAgeCutoff = FileUtils.listFiles(sessionDirectory,
//                                                                      FileFilterUtils.ageFileFilter(TIMELIMIT), DirectoryFileFilter.DIRECTORY);
//                filesAgeCutoff.stream().forEach(System.out::println);
            }
        };

        this.schedule(t, INITIAL_DELAY, PERIOD);
    }
}
