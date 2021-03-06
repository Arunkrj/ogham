package fr.sii.ogham.email.builder;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sii.ogham.core.builder.AbstractParent;
import fr.sii.ogham.core.builder.Builder;
import fr.sii.ogham.core.builder.env.EnvironmentBuilder;
import fr.sii.ogham.core.builder.resolution.ClassPathResolutionBuilder;
import fr.sii.ogham.core.builder.resolution.FileResolutionBuilder;
import fr.sii.ogham.core.builder.resolution.ResourceResolutionBuilder;
import fr.sii.ogham.core.builder.resolution.ResourceResolutionBuilderHelper;
import fr.sii.ogham.core.builder.resolution.StringResolutionBuilder;
import fr.sii.ogham.core.resource.resolver.FirstSupportingResourceResolver;
import fr.sii.ogham.core.resource.resolver.ResourceResolver;
import fr.sii.ogham.core.translator.resource.AttachmentResourceTranslator;
import fr.sii.ogham.core.translator.resource.EveryResourceTranslator;
import fr.sii.ogham.core.translator.resource.LookupResourceTranslator;
import fr.sii.ogham.email.attachment.Attachment;

/**
 * Configures how {@link Attachment}s are handled.
 * 
 * Attachment resolution consists of finding a file:
 * <ul>
 * <li>either on filesystem</li>
 * <li>or in the classpath</li>
 * <li>or anywhere else</li>
 * </ul>
 * 
 * 
 * @author Aurélien Baudet
 *
 */
public class AttachmentHandlingBuilder extends AbstractParent<EmailBuilder> implements ResourceResolutionBuilder<AttachmentHandlingBuilder>, Builder<AttachmentResourceTranslator> {
	private static final Logger LOG = LoggerFactory.getLogger(AttachmentHandlingBuilder.class);

	private ResourceResolutionBuilderHelper<AttachmentHandlingBuilder> resourceResolutionBuilderHelper;

	/**
	 * Initializes the builder with a parent builder. The parent builder is used
	 * when calling {@link #and()} method. The {@link EnvironmentBuilder} is
	 * used to evaluate properties when {@link #build()} method is called.
	 * 
	 * @param parent
	 *            the parent builder
	 * @param environmentBuilder
	 *            the configuration for property resolution and evaluation
	 */
	public AttachmentHandlingBuilder(EmailBuilder parent, EnvironmentBuilder<?> environmentBuilder) {
		super(parent);
		resourceResolutionBuilderHelper = new ResourceResolutionBuilderHelper<>(this, environmentBuilder);
	}

	@Override
	public ClassPathResolutionBuilder<AttachmentHandlingBuilder> classpath() {
		return resourceResolutionBuilderHelper.classpath();
	}

	@Override
	public FileResolutionBuilder<AttachmentHandlingBuilder> file() {
		return resourceResolutionBuilderHelper.file();
	}

	@Override
	public StringResolutionBuilder<AttachmentHandlingBuilder> string() {
		return resourceResolutionBuilderHelper.string();
	}

	@Override
	public AttachmentHandlingBuilder resolver(ResourceResolver resolver) {
		return resourceResolutionBuilderHelper.resolver(resolver);
	}

	@Override
	public AttachmentResourceTranslator build() {
		EveryResourceTranslator translator = new EveryResourceTranslator();
		LOG.info("Using translator that calls all registered translators");
		translator.addTranslator(new LookupResourceTranslator(buildResolver()));
		LOG.debug("Registered translators: {}", translator.getTranslators());
		return translator;
	}

	private ResourceResolver buildResolver() {
		List<ResourceResolver> resolvers = resourceResolutionBuilderHelper.buildResolvers();
		return new FirstSupportingResourceResolver(resolvers);
	}
}
