/*******************************************************************************
 * Copyright (c) 2015, 2016 Emanuele Altomare, Andrea Mayer
 *
 * This file is part of Proxy2Tor.
 * Proxy2Tor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * Proxy2Tor is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proxy2Tor.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
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
 * @author Andrea Mayer
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
