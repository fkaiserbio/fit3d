package bio.fkaiser.fit3d.web.beans.application;

import bio.fkaiser.fit3d.web.Fit3DWebConstants;
import bio.fkaiser.fit3d.web.model.Fit3DJob;
import bio.singa.core.utility.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author fk
 */
public class MailNotifier {

    private static final Logger logger = LoggerFactory.getLogger(MailNotifier.class);

    public void sendMail(Fit3DJob job) {

        InputStream inputStream;
        if (job.isFinished()) {
            inputStream = Resources.getResourceAsStream("email_finished.html");
        } else {
            inputStream = Resources.getResourceAsStream("email_failed.html");
        }

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            String emailContent = buffer.lines().collect(Collectors.joining("\n"));
            if (!job.getDescription().isEmpty()) {
                emailContent = emailContent.replace("[JOBNAME]", job.getDescription());
            } else {
                emailContent = emailContent.replace("[JOBNAME]", "");
            }
            emailContent = emailContent.replace("[JOBURL]", "https://biosciences.hs-mittweida.de/fit3d/jobs?id=" + job.getSessionIdentifier());

            // add error message
            if (job.isFailed() && job.getErrorMessage().isEmpty()) {
                emailContent = emailContent.replace("[JOB_ERROR]", "unknown error");
            } else if (job.isFailed() && !job.getErrorMessage().isEmpty()) {
                emailContent = emailContent.replace("[JOB_ERROR]", job.getErrorMessage());
            }

            Properties properties = new Properties();
            properties.put("mail.smtp.host", "mail.hs-mittweida.de");
            // properties.put("mail.smtp.port", "PORTNUMBER");
            properties.put("mail.transport.protocol", "smtp");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.tls", "true");
            properties.put("mail.smtp.ssl.checkserveridentity", "true");
            Authenticator authenticator = new javax.mail.Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(Fit3DWebConstants.Mail.SMTP_USER, Fit3DWebConstants.Mail.SMTP_PASS);
                }
            };
            Session session = Session.getDefaultInstance(properties, authenticator);

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@bioforscher.de", "Fit3D Webserver"));
            message.setSentDate(new Date());
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(job.getEmail(), job.getEmail()));

            if (job.getDescription() != null) {
                message.setSubject("Your Fit3D job " + job.getDescription());
            } else {
                message.setSubject("Your Fit3D job");
            }

            message.setText(emailContent, "utf-8", "html");
            message.saveChanges();

            Transport.send(message);

            logger.info("notification mail sent for job {}", job);
        } catch (IOException | MessagingException e) {
            logger.error("failed to send mail notification for job: {}", job, e);
        }
    }
}
