package fr.sii.ogham.core.builder.configurer;

import static fr.sii.ogham.core.CoreConstants.DEFAULT_MESSAGING_CONFIGURER_PRIORITY;

import org.apache.tika.Tika;

import fr.sii.ogham.core.builder.MessagingBuilder;
import fr.sii.ogham.core.builder.env.EnvironmentBuilder;
import fr.sii.ogham.core.builder.mimetype.MimetypeDetectionBuilder;
import fr.sii.ogham.core.builder.mimetype.TikaBuilder;
import fr.sii.ogham.core.builder.resolution.ResourceResolutionBuilder;
import fr.sii.ogham.core.convert.DefaultConverter;
import fr.sii.ogham.core.exception.MessagingException;
import fr.sii.ogham.core.id.generator.SequentialIdGenerator;
import fr.sii.ogham.core.service.MessagingService;
import fr.sii.ogham.email.builder.AutofillDefaultEmailAddressBuilder;
import fr.sii.ogham.email.builder.AutofillSubjectBuilder;
import fr.sii.ogham.email.builder.CssInliningBuilder;
import fr.sii.ogham.email.builder.EmailBuilder;
import fr.sii.ogham.email.builder.ImageInliningBuilder;
import fr.sii.ogham.email.message.Email;
import fr.sii.ogham.sms.builder.AutofillSmsBuilder;
import fr.sii.ogham.sms.builder.RecipientNumberFormatBuilder;
import fr.sii.ogham.sms.builder.SenderNumberFormatBuilder;
import fr.sii.ogham.sms.builder.SmsBuilder;
import fr.sii.ogham.sms.message.Sms;

/**
 * Default configurer that is automatically applied every time a
 * {@link MessagingBuilder} instance is created through
 * {@link MessagingBuilder#standard()} or {@link MessagingBuilder#minimal()}.
 * 
 * <p>
 * The configurer has a priority of 100000 in order to be applied before all
 * other configurers.
 * </p>
 * 
 * This configurer applies general configuration (see
 * {@link EnvironmentBuilder}):
 * <ul>
 * <li>The {@link MessagingService} will catch all uncaught exception (even
 * runtime) in order to wrap them in a {@link MessagingException}.</li>
 * <li>General environment configuration:
 * <ul>
 * <li>System properties with</li>
 * <li>Uses the {@link DefaultConverter}</li>
 * <li><strong>Environment will be inherited by sub-builders by
 * default.</strong></li>
 * </ul>
 * </li>
 * <li>Resource resolution configuration (see
 * {@link ResourceResolutionBuilder}):
 * <ul>
 * <li>Lookup prefixes for classpath: "classpath:" and no lookup prefix (if no
 * prefix is defined, then classpath is used).
 * <li>Lookup prefix for file: "file:".
 * <li>Lookup prefixes for string: "string:", "s:.
 * <li><strong>Resource resolution will be by default inherited by
 * sub-builders</strong></li>
 * </ul>
 * </li>
 * <li>Configure common email behaviors:
 * <ul>
 * <li>Configure resource resolution for attachments, css inlining and image
 * inlining using same resource resolution configuration as the general one (see
 * {@link ResourceResolutionBuilder})</li>
 * <li>Configure mimetype detection for attachments and image inlining (see
 * {@link MimetypeDetectionBuilder})</li>
 * <li>Automatically fill {@link Email} messages with subject either from title
 * tag of html template, from first line starting with "Subject:" of text
 * template or using {@code ogham.email.subject} property if defined (see
 * {@link AutofillSubjectBuilder})</li>
 * <li>Autofill {@link Email} messages with sender address if one of the
 * property {@code ogham.email.from} or {@code mail.smtp.from} is defined (see
 * {@link AutofillDefaultEmailAddressBuilder})</li>
 * <li>Autofill {@link Email} messages with recipient address (to) if the
 * property {@code ogham.email.to} is defined (see
 * {@link AutofillDefaultEmailAddressBuilder})</li>
 * <li>Autofill {@link Email} messages with recipient address (cc) if the
 * property {@code ogham.email.cc} is defined (see
 * {@link AutofillDefaultEmailAddressBuilder})</li>
 * <li>Autofill {@link Email} messages with recipient address (bcc) if the
 * property {@code ogham.email.bcc} is defined (see
 * {@link AutofillDefaultEmailAddressBuilder})</li>
 * <li>Automatically inline CSS styles in the HTML templates (see
 * {@link CssInliningBuilder})</li>
 * <li>Automatically inline images in the email either by attaching them or by
 * converting them into base64 (see {@link ImageInliningBuilder})</li>
 * </ul>
 * </li>
 * <li>Configure common SMS behaviors:
 * <ul>
 * <li>Autofill {@link Sms} messages with sender phone number if the property
 * {@code ogham.sms.from} is defined (see {@link AutofillSmsBuilder})</li>
 * <li>Autofill {@link Sms} messages with recipient phone number if the property
 * {@code ogham.sms.to} is defined (see {@link AutofillSmsBuilder})</li>
 * <li>Configure phone number formats (see {@link SenderNumberFormatBuilder} and
 * {@link RecipientNumberFormatBuilder})</li>
 * </ul>
 * </li>
 * <li>Mimetype detection configuration:
 * <ul>
 * <li>Uses {@link Tika} to detect mimetype (see {@link TikaBuilder})</li>
 * <li>Uses property {@code ogham.mimetype.default-mimetype} if Tika has not
 * detected the mimetype (see {@link MimetypeDetectionBuilder})</li>
 * <li>Uses {@code application/octet-stream} if neither Tika has detected
 * mimetype nor default property value has been set</li>
 * </ul>
 * </ul>
 * 
 * @author Aurélien Baudet
 *
 */
@ConfigurerFor(targetedBuilder = { "minimal", "standard" }, priority = DEFAULT_MESSAGING_CONFIGURER_PRIORITY)
public class DefaultMessagingConfigurer extends MessagingConfigurerAdapter {

	@Override
	public void configure(MessagingBuilder builder) {
		super.configure(builder);
		builder.wrapUncaught(true);
	}

	@Override
	public void configure(EnvironmentBuilder<?> builder) {
		// @formatter:off
		builder
			.systemProperties()
			.converter()
				.override(new DefaultConverter());
		// @formatter:on
	}

	@Override
	public void configure(ResourceResolutionBuilder<?> builder) {
		// @formatter:off
		builder
			.string()
				.lookup("string:", "s:")
				.and()
			.file()
				.lookup("file:")
				.and()
			.classpath()
				.lookup("classpath:", "");
		// @formatter:on
	}

	@Override
	public void configure(EmailBuilder builder) {
		// configure resource resolution for attachments, css and images
		configure(builder.attachments());
		configure(builder.css().inline());
		configure(builder.images().inline());
		// configure mimetype detection for images
		configure(builder.images().inline().mimetype());

		// @formatter:off
		builder
			.autofill()
				.subject()
					.defaultValueProperty("${ogham.email.subject}")
					.htmlTitle(true)
					.text("${ogham.email.subject-first-line-prefix}", "Subject:")
					.and()
				.from()
					.defaultValueProperty("${ogham.email.from}", "${mail.smtp.from}")
					.and()
				.to()
					.defaultValueProperty("${ogham.email.to}")
					.and()
				.cc()
					.defaultValueProperty("${ogham.email.cc}")
					.and()
				.bcc()
					.defaultValueProperty("${ogham.email.bcc}")
					.and()
				.and()
			.css()
				.inline()
					.jsoup()
					.and()
				.and()
			.images()
				.inline()
					.attach()
						.cid()
							.generator(new SequentialIdGenerator())
							.and()
						.and()
					.base64();
		// @formatter:on
	}

	@Override
	public void configure(SmsBuilder builder) {
		// @formatter:off
		builder
			.autofill()
				.from()
					.defaultValueProperty("${ogham.sms.from}")
					.and()
				.to()
					.defaultValueProperty("${ogham.sms.to}")
					.and()
				.and()
			.numbers()
				.from()
					.format()
						.alphanumericCode("${ogham.sms.from-format-enable-alphanumeric}", "true")
						.shortCode("${ogham.sms.from-format-enable-shortcode}", "true")
						.internationalNumber("${ogham.sms.from-format-enable-international}", "true")
						.and()
					.and()
				.to()
					.format()
						.internationalNumber("${ogham.sms.to-format-enable-international}", "true");
		// @formatter:on
	}

	@Override
	public void configure(MimetypeDetectionBuilder<?> builder) {
		// @formatter:off
		builder
			.tika()
				.instance(new Tika())
				.failIfOctetStream(true)
				.and()
			.defaultMimetype("${ogham.mimetype.default-mimetype}", "application/octet-stream");
		// @formatter:on
	}

}
