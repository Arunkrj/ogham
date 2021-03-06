package fr.sii.ogham.spring.template;

import org.springframework.boot.autoconfigure.freemarker.FreeMarkerProperties;
import org.springframework.boot.bind.RelaxedNames;

import fr.sii.ogham.core.builder.MessagingBuilder;
import fr.sii.ogham.core.builder.configurer.MessagingConfigurerAdapter;
import fr.sii.ogham.email.builder.EmailBuilder;
import fr.sii.ogham.sms.builder.SmsBuilder;
import fr.sii.ogham.spring.common.OghamTemplateProperties;
import fr.sii.ogham.spring.common.SpringMessagingConfigurer;
import fr.sii.ogham.spring.email.OghamEmailProperties;
import fr.sii.ogham.spring.sms.OghamSmsProperties;
import fr.sii.ogham.template.freemarker.FreeMarkerParser;
import fr.sii.ogham.template.freemarker.FreemarkerConstants;
import fr.sii.ogham.template.freemarker.builder.AbstractFreemarkerBuilder;
import fr.sii.ogham.template.freemarker.builder.FreemarkerEmailBuilder;
import fr.sii.ogham.template.freemarker.builder.FreemarkerSmsBuilder;
import freemarker.template.Configuration;

/**
 * Integrates with Spring templating system by using Freemarker
 * {@link Configuration} object provided by Spring and by using Spring
 * properties defined with prefix {@code spring.freemarker} (see
 * {@link FreeMarkerProperties}).
 * 
 * If both Spring property and Ogham property is defined, Spring property is
 * used.
 * 
 * For example, if the file application.properties contains the following
 * configuration:
 * 
 * <pre>
 * spring.freemarker.prefix=/email/
 * ogham.email.freemarker.path-prefix=/foo/
 * </pre>
 * 
 * The {@link FreeMarkerParser} will use the templates in "/email/".
 * 
 * <p>
 * This configurer is also useful to support property naming variants (see
 * {@link RelaxedNames}).
 * 
 * @author Aurélien Baudet
 *
 */
public class FreeMarkerConfigurer extends MessagingConfigurerAdapter implements SpringMessagingConfigurer {
	private final Configuration emailConfiguration;
	private final Configuration smsConfiguration;
	private final OghamCommonTemplateProperties templateProperties;
	private final OghamEmailProperties emailProperties;
	private final OghamSmsProperties smsProperties;
	private final FreeMarkerProperties springProperties;

	public FreeMarkerConfigurer(Configuration emailConfiguration, Configuration smsConfiguration, OghamCommonTemplateProperties templateProperties, OghamEmailProperties emailProperties,
			OghamSmsProperties smsProperties, FreeMarkerProperties springProperties) {
		super();
		this.emailConfiguration = emailConfiguration;
		this.smsConfiguration = smsConfiguration;
		this.templateProperties = templateProperties;
		this.emailProperties = emailProperties;
		this.smsProperties = smsProperties;
		this.springProperties = springProperties;
	}

	@Override
	public void configure(MessagingBuilder builder) {
		// use same environment as parent builder
		builder.email().template(FreemarkerEmailBuilder.class).environment(builder.environment());
		builder.sms().template(FreemarkerSmsBuilder.class).environment(builder.environment());
		super.configure(builder);
	}

	@Override
	public void configure(EmailBuilder emailBuilder) {
		AbstractFreemarkerBuilder<?, ?> builder = emailBuilder.template(FreemarkerEmailBuilder.class);
		builder.configuration(emailConfiguration);
		if (springProperties != null) {
			applySpringConfiguration(builder);
		}
		if (emailProperties != null) {
			applyOghamConfiguration(builder, emailProperties);
		}
	}

	@Override
	public void configure(SmsBuilder smsBuilder) {
		AbstractFreemarkerBuilder<?, ?> builder = smsBuilder.template(FreemarkerSmsBuilder.class);
		builder.configuration(smsConfiguration);
		if (springProperties != null) {
			applySpringConfiguration(builder);
		}
		if (smsProperties != null) {
			applyOghamConfiguration(builder, smsProperties);
		}
	}

	@Override
	public int getOrder() {
		return FreemarkerConstants.DEFAULT_FREEMARKER_EMAIL_CONFIGURER_PRIORITY + 1000;
	}

	private void applyOghamConfiguration(AbstractFreemarkerBuilder<?, ?> builder, OghamTemplateProperties props) {
		// @formatter:off
		builder
			.classpath()
				.pathPrefix(props.getFreemarker().getClasspath().getPathPrefix(),
							props.getTemplate().getClasspath().getPathPrefix(),
							props.getFreemarker().getPathPrefix(),
							props.getTemplate().getPathPrefix(),
							templateProperties.getPathPrefix())
				.pathSuffix(props.getFreemarker().getClasspath().getPathSuffix(),
							props.getTemplate().getClasspath().getPathSuffix(),
							props.getFreemarker().getPathSuffix(),
							props.getTemplate().getPathSuffix(),
							templateProperties.getPathSuffix())
				.and()
			.file()
				.pathPrefix(props.getFreemarker().getFile().getPathPrefix(),
							props.getTemplate().getFile().getPathPrefix(),
							props.getFreemarker().getPathPrefix(),
							props.getTemplate().getPathPrefix(),
							templateProperties.getPathPrefix())
				.pathSuffix(props.getFreemarker().getFile().getPathSuffix(),
							props.getTemplate().getFile().getPathSuffix(),
							props.getFreemarker().getPathSuffix(),
							props.getTemplate().getPathSuffix(),
							templateProperties.getPathSuffix());
		// @formatter:on
	}

	private void applySpringConfiguration(AbstractFreemarkerBuilder<?, ?> builder) {
		// @formatter:off
		builder
			.classpath()
				.pathPrefix(springProperties.getPrefix())
				.pathSuffix(springProperties.getSuffix())
				.and()
			.file()
				.pathPrefix(springProperties.getPrefix())
				.pathSuffix(springProperties.getSuffix());
		// @formatter:on
	}

}
