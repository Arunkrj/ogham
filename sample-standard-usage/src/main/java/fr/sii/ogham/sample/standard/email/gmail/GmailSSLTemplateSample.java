package fr.sii.ogham.sample.standard.email.gmail;

import fr.sii.ogham.context.SimpleBean;
import fr.sii.ogham.core.builder.MessagingBuilder;
import fr.sii.ogham.core.exception.MessagingException;
import fr.sii.ogham.core.message.content.MultiTemplateContent;
import fr.sii.ogham.core.service.MessagingService;
import fr.sii.ogham.email.attachment.Attachment;
import fr.sii.ogham.email.message.Email;

public class GmailSSLTemplateSample {

	public static void main(String[] args) throws MessagingException {
		// Instantiate the messaging service using default behavior and
		// provided properties (properties can be externalized)
		MessagingService service = MessagingBuilder.standard()
										.environment()
											.properties()
												.set("mail.smtp.auth", "true")
												.set("mail.smtp.host", "smtp.gmail.com")
												.set("mail.smtp.port", "465")
												.set("mail.smtp.socketFactory.port", "465")
												.set("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
												.set("ogham.email.javamail.authenticator.username", "<your gmail username>")
												.set("ogham.email.javamail.authenticator.password", "<your gmail password>")
												.set("ogham.email.from", "<your gmail address>")
												.set("ogham.email.template.path-prefix", "/template/")
												.and()
											.and()
										.build();
		// send the email
		service.send(new Email()
						.content(new MultiTemplateContent("withImagesAndCss/resources", new SimpleBean("foo", 42)))
						.to("ogham-test@yopmail.com")
						.attach(new Attachment("/attachment/test.pdf")));
	}

}
