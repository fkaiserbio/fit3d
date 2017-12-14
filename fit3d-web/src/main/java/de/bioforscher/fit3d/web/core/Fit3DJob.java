package de.bioforscher.fit3d.web.core;

import de.bioforscher.fit3d.web.utilities.Fit3dConstants;
import de.bioforscher.fit3d.web.utilities.LoadMonitor;
import de.bioforscher.fit3d.web.utilities.LogHandler;
import de.bioforscher.fit3d.web.utilities.MailNotifier;

import java.io.File;
import java.io.Serializable;
import java.lang.ProcessBuilder.Redirect;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * A class representing a Fit3D job.
 */
public class Fit3DJob implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6539323103513381973L;
	private String commandLine;
	private String description;
	private String email;
	private boolean enqueued = false;
	private boolean finished = false;
	private UUID id;
	private boolean running = false;

	private JobParameters parameters;

	private Date timeStamp;

	private String workingDirectory;
	private UUID sessionId;
	transient private Process process;

	public Fit3DJob(UUID id, UUID sessionId, Date timeStamp, String description, String email, String workingDirectory,
			String commandLine, JobParameters parameters) {

		this.id = id;
		this.sessionId = sessionId;
		this.timeStamp = timeStamp;
		this.description = description;
		this.email = email;
		this.workingDirectory = workingDirectory;
		this.commandLine = commandLine;
		this.parameters = parameters;
	}

	public String getCommandLine() {
		return this.commandLine;
	}

	public String getDescription() {
		return this.description;
	}

	public String getEmail() {
		return this.email;
	}

	public UUID getId() {
		return this.id;
	}

	public JobParameters getParameters() {
		return this.parameters;
	}

	public Process getProcess() {
		return this.process;
	}

	public UUID getSessionId() {
		return this.sessionId;
	}

	public String getStatus() {

		if (this.enqueued) {

			return "enqueued";
		}

		if (this.running) {

			return "running";
		}

		if (this.finished) {

			return "finished";
		}

		return null;
	}

	public Date getTimeStamp() {
		return this.timeStamp;
	}

	public String getWorkingDirectory() {
		return this.workingDirectory;
	}

	public boolean isEnqueued() {
		return this.enqueued;
	}

	public boolean isFinished() {
		return this.finished;
	}

	public boolean isRunning() {
		return this.running;
	}

	@Override
	public void run() {

		try {

			LogHandler.LOG.info("starting job " + this);

			ProcessBuilder pb = new ProcessBuilder(
					new String("java -jar " + Fit3dConstants.FIT3D_LOCATION + " " + this.commandLine).split(" "));
			File log = new File(this.workingDirectory + "/Fit3D.log");
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(log));
			pb.directory(new File(this.workingDirectory));

			this.process = pb.start();

			this.running = true;
			this.enqueued = false;

			// catch log
			assert pb.redirectInput() == Redirect.PIPE;
			assert pb.redirectOutput().file() == log;
			assert this.process.getInputStream().read() == -1;

			this.process.waitFor();

			LogHandler.LOG.info("finished job " + this);
			this.finished = true;
			this.running = false;

			// count load monitor
			LoadMonitor.getInstance().countDown();

			// send notification mail
			if (!this.email.equals("")) {

				MailNotifier.getInstance().sendNotificationMail(this.sessionId, this);
			}

		} catch (Exception e) {

			LogHandler.LOG.severe(e.getMessage() + " " + this);
		}
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEnqueued(boolean enqueue) {
		this.enqueued = enqueue;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	@Override
	public String toString() {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return this.description + " submitted at " + df.format(this.timeStamp);
	}
}
