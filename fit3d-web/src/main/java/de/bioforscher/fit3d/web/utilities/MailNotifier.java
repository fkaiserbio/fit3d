package de.bioforscher.fit3d.web.utilities;

import de.bioforscher.fit3d.web.core.Fit3DJob;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class MailNotifier {

    private static MailNotifier instance = new MailNotifier();

    private MailNotifier() {

    }

    /**
     *
     */

    public static MailNotifier getInstance() {

        return instance;
    }

    public void sendNotificationMail(UUID sessionId, Fit3DJob job)
            throws MessagingException, IOException {

        // TODO implement
//		// assemble email content
//		String emailContent = IOUtils
//				.toString(Thread
//						.currentThread()
//						.getContextClassLoader()
//						.getResourceAsStream(
//								"de/bioforscher/fit3d/webserver/application/utilities/email.html"));
//
//		if (!job.getDescription().isEmpty()) {
//
//			emailContent = emailContent.replace("[JOBNAME]",
//					job.getDescription());
//		} else {
//
//			emailContent = emailContent.replace("[JOBNAME]", "");
//		}
//		emailContent = emailContent.replace("[JOBURL]",
//				"https://biosciences.hs-mittweida.de/fit3d/jobs?id="
//						+ sessionId);
//
//		LogHandler.LOG
//				.info("sending notification email for job " + job.getId());

        final Properties props = new Properties();
        props.put("mail.smtp.host", "mail.hs-mittweida.de");
        // props.put("mail.smtp.port", "PORTNUMBER");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.tls", "true");
        props.put("mail.smtp.ssl.checkserveridentity", "true");

        final Authenticator auth = new javax.mail.Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                // TODO authenticate
                return new PasswordAuthentication(Fit3dConstants.SMTP_USER,
                                                  Fit3dConstants.SMTP_PASS);
            }
        };

        Session session = Session.getDefaultInstance(props, auth);

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("noreply@bioforscher.de",
                                        "Fit3D Webserver"));
        msg.setSentDate(new Date());
        msg.addRecipient(Message.RecipientType.TO,
                         new InternetAddress(job.getEmail(), job.getEmail()));

        if (job.getDescription() != null) {

            msg.setSubject("Your job " + job.getDescription() + " is finished");
        } else {
            msg.setSubject("Your job is finished");
        }

//        msg.setText(emailContent, "utf-8", "html");
        msg.saveChanges();

        Transport.send(msg);
    }
}
