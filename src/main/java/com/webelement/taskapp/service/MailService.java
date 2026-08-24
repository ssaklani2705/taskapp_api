package com.webelement.taskapp.service;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.springframework.stereotype.Service;

import com.webelement.taskapp.entity.SmtpEntity;

import java.io.File;

import java.util.Properties;

@Service
public class MailService {

	public int postMailAttach(String[] to, String[] cc, String[] bcc, String message, String sub, String filePath,
			String localFileName, int check, String from, SmtpEntity smtp) {
		int ccval = 0;
		try {

			if (smtp == null) {
				throw new IllegalStateException("No SMTP configuration found.");
			}
//
			String host = smtp.getHost();
			String port = smtp.getPort();
			String username = smtp.getServerUsername();
			String password = smtp.getServerPassword();
			String fromAddress = smtp.getFrom();
			String displayName = smtp.getDisplayName();

			// Mail properties
			Properties properties = new Properties();
			properties.put("mail.smtp.host", host);
			if (port != null && !port.isEmpty()) {
				properties.put("mail.smtp.port", port);
			}
			properties.put("mail.smtp.auth", "true");
			properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.put("mail.smtp.socketFactory.fallback", "false");
			properties.put("mail.smtp.starttls.enable", "true");
			properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

			// Authenticator
			Authenticator authenticator = new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			};

			Session session = Session.getInstance(properties, authenticator);
			session.setDebug(false);

			MimeMessage mimeMessage = new MimeMessage(session);
			InternetAddress fromAddressObj = new InternetAddress(fromAddress, displayName);
			mimeMessage.setSubject(sub);
			mimeMessage.setFrom(fromAddressObj);
			if (check > 0) {
				mimeMessage.addHeader("Disposition-Notification-To", fromAddress);
			}

			// Recipients
			if (to != null && to.length > 0) {
				InternetAddress[] toAddresses = new InternetAddress[to.length];
				for (int i = 0; i < to.length; i++) {
					toAddresses[i] = new InternetAddress(to[i]);
				}
				mimeMessage.setRecipients(Message.RecipientType.TO, toAddresses);
			}
			if (cc != null && cc.length > 0) {
				InternetAddress[] ccAddresses = new InternetAddress[cc.length];
				for (int i = 0; i < cc.length; i++) {
					ccAddresses[i] = new InternetAddress(cc[i]);
				}
				mimeMessage.setRecipients(Message.RecipientType.CC, ccAddresses);
			}
			if (bcc != null && bcc.length > 0) {
				InternetAddress[] bccAddresses = new InternetAddress[bcc.length];
				for (int i = 0; i < bcc.length; i++) {
					bccAddresses[i] = new InternetAddress(bcc[i]);
				}
				mimeMessage.setRecipients(Message.RecipientType.BCC, bccAddresses);
			}

			// Email body
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(message, "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			// Attachments
			if (localFileName != null && !localFileName.isEmpty() && localFileName.contains(",")) {
				String[] fileNames = localFileName.split(",");
				String[] filePaths = filePath.split(",");
				for (int i = 0; i < fileNames.length; i++) {
					File file = new File(filePaths[i]);
					if (file.exists()) {
						MimeBodyPart attachPart = new MimeBodyPart();
						DataSource source = new FileDataSource(filePaths[i]);
						attachPart.setDataHandler(new DataHandler(source));
						attachPart.setFileName(fileNames[i]);
						multipart.addBodyPart(attachPart);
					}
				}
			} else {
				File file = new File(filePath);
				if (file.exists()) {
					MimeBodyPart attachPart = new MimeBodyPart();
					DataSource source = new FileDataSource(filePath);
					attachPart.setDataHandler(new DataHandler(source));
					attachPart.setFileName(localFileName);
					multipart.addBodyPart(attachPart);
				}
			}

			mimeMessage.setContent(multipart);
			mimeMessage.setSentDate(new java.util.Date());

			// Send email
			Transport.send(mimeMessage);
			ccval = 1;

		} catch (Exception e) {
			ccval = 0;
			e.printStackTrace();
		}
		return ccval;
	}
}
