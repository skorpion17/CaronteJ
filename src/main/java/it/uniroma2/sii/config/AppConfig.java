package it.uniroma2.sii.config;

import it.uniroma2.sii.config.impl.OnionBinderConfigImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Configurazione per l'applicazione.
 * 
 * @author Andrea Mayer
 *
 */
@Configuration
@PropertySources({ @PropertySource("application.properties") })
public class AppConfig {
	/*
	 * PropertySourcesPlaceHolderConfigurer Bean only required for @Value("{}")
	 * annotations. Remove this bean if you are not using @Value annotations for
	 * injecting properties.
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public OnionBinderConfig onionBinderConfig() {
		return new OnionBinderConfigImpl();
	}
}
