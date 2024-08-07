package eu.eurekabpo.log4j2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * Appends log events to GMail
 * */
@Plugin(name = "GMail", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class GMailAppender extends AbstractAppender {

	/**
	 * Builds GMailAppender instances
	 * */
	public static class Builder extends AbstractAppender.Builder<Builder>
		implements org.apache.logging.log4j.core.util.Builder<GMailAppender> {

		@PluginBuilderAttribute
		@Required(message = "No serviceAccountKey provided")
		private String serviceAccountKey;

		@PluginBuilderAttribute
		@Required(message = "No delegate provided")
		private String delegate;

		@PluginBuilderAttribute
		@Required(message = "No recipients provided")
		private String recipients;

		@PluginBuilderAttribute
		@Required(message = "No subject provided")
		private String subject;

		@PluginBuilderAttribute
		private String contentType;

		public Builder setServiceAccountKey(String serviceAccountKey) {
			this.serviceAccountKey = serviceAccountKey;
			return this;
		}

		public Builder setDelegate(String delegate) {
			this.delegate = delegate;
			return this;
		}

		public Builder setRecipients(String recipients) {
			this.recipients = recipients;
			return this;
		}

		public Builder setSubject(String subject) {
			this.subject = subject;
			return this;
		}

		public Builder setContentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		private Gmail getGMailClient(File serviceAccountKeyFile, String delegate)
				throws IOException {
			HttpTransport transport = new NetHttpTransport();
			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new FileInputStream(serviceAccountKeyFile), () -> transport)
					.createScoped(Arrays.asList(GmailScopes.GMAIL_SEND)).createDelegated(delegate);
			System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3");
			Gmail client = new Gmail.Builder(transport, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
					.build();
			return client;
		}

		@Override
		public GMailAppender build() {
			try {
				Gmail client = getGMailClient(new File(serviceAccountKey), delegate);
				return new GMailAppender(getName(), getFilter(), this.getOrCreateLayout(), isIgnoreExceptions(), getPropertyArray(),
						client, delegate, recipients, subject, contentType);
			} catch (IOException e) {
				LOGGER.error("Error has acquired while create GMail client", e);
				return null;
			}
		}
	}

	/**
	 * Log4j2 convenient builder method
	 * 
	 * @return Bilder instance
	 * */
	@PluginBuilderFactory
	public static Builder newBuilder() {
		return new Builder();
	}

	private String sender;
	private String recipients;
	private String subject;
	private String contentType;
	private Gmail gMailClient;

	private GMailAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions, Property[] properties, Gmail gMailClient, String sender,
			String recipients, String subject, String contentType) {
		super(name, filter, layout, ignoreExceptions, properties);
		this.gMailClient = gMailClient;
		this.sender = sender;
		this.recipients = recipients;
		this.subject = subject;
		this.contentType = contentType;
	}

	@Override
	public void append(LogEvent event) {
		try {
			String eventStr = toSerializable(event).toString();
			MimeMessage javaMailMessage = getJavaMailMessage(eventStr);
			gMailClient.users().messages().send(sender, getGMailMessage(javaMailMessage)).execute();
			LOGGER.debug("event message successfully sent");
		} catch (Exception e) {
			LOGGER.error("Error has acquired while messagse sendinge", e);
		}
	}
	
	private MimeMessage getJavaMailMessage(String body)
			throws AddressException, MessagingException {
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		MimeMessage email = new MimeMessage(session);
		email.addRecipients(RecipientType.TO, InternetAddress.parse(recipients));
		email.setSubject(subject);
		if (contentType == null) {
			email.setText(body);
		} else {
			Multipart mp = new MimeMultipart();
			MimeBodyPart part = new MimeBodyPart();
			part.setContent(body, contentType);
			mp.addBodyPart(part);
			email.setContent(mp);
		}
		return email;
	}
	
	private Message getGMailMessage(MimeMessage javaMailMessage) throws IOException, MessagingException {
		Message message = new Message();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		javaMailMessage.writeTo(buffer);
		message.encodeRaw(buffer.toByteArray());
		return message;
	}
}
