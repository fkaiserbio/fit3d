package de.bioforscher.fit3d.web.utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogHandler {

	public static final Logger LOG = initLogger();

	private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static final String NEWLINE = System.lineSeparator();

	private static Logger initLogger() {
		final Logger log = Logger.getLogger("de.bioforscher.fit3d.webserver");

		// set standard log level
		log.setLevel(Level.INFO);

		log.setUseParentHandlers(false);

		log.addHandler(new Handler() {
			private BufferedWriter writer;

			@Override
			public void close() throws SecurityException {
				try {
					if (this.writer != null) {

						this.writer.close();
					}
				} catch (final Exception e) {
				}
			}

			@Override
			public void flush() {
			}

			@Override
			public void publish(LogRecord record) {
				synchronized (LOG) {
					if (record.getLevel().intValue() > Level.WARNING.intValue()) {
						addLogEntry(record);
						System.err.print(getFormattedLogRecord(record));
					} else {
						System.out.print(getFormattedLogRecord(record));
					}
				}
			}

			private synchronized void addLogEntry(LogRecord record) {
				if (this.writer == null) {
					try {
						this.writer = new BufferedWriter(new FileWriter(
								"Fit3DWebserver.err", true));
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}

				try {
					this.writer.write(getFormattedLogRecord(record));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

			private String getFormattedLogRecord(LogRecord record) {
				final StringBuilder builderRecord = new StringBuilder();

				builderRecord.append(LOG_DATE_FORMAT.format(new Date(record
						.getMillis())) + " ");

				if (record.getLevel().intValue() == Level.SEVERE.intValue()) {

					builderRecord.append("[" + record.getSourceClassName()
							+ "#" + record.getSourceMethodName() + "] ");
				}

				builderRecord.append(record.getLevel() + ": "
						+ record.getMessage());

				if (record.getThrown() != null) {
					builderRecord.append(NEWLINE);
					for (final StackTraceElement entry : record.getThrown()
							.getStackTrace()) {
						builderRecord.append("\t" + entry.toString() + NEWLINE);
					}
				}

				builderRecord.append(NEWLINE);

				return builderRecord.toString();
			}
		});

		return log;
	}
}
