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
 * L'aggiunta di ${propertySource:application.properties} permette di poter
 * sovrascrivere il file application.properties con uno specificato da linea di
 * comando durante l'esecuzione del .jar utilizzando
 * -DpropertySource=file:PATH_DEL_FILE_DI_PROPERTIES.
 * 
 * @author andrea
 *
 */
@Configuration
@PropertySources({ @PropertySource("${propertySource:application.properties}") })
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
