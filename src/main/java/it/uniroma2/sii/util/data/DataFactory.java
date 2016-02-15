/*******************************************************************************
 * Copyright (c) 2015 Emanuele Altomare, Andrea Mayer
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
package it.uniroma2.sii.util.data;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.HttpDataFactory;
import it.uniroma2.sii.util.data.unknown.UnknownDataFactory;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

/**
 * È la classe che ha la responsabilità di creare oggetti di tipo {@link Data}.
 * 
 * @author Emanuele Altomare
 */
@Component
public class DataFactory {

	/**
	 * Costruttore di default.
	 */
	public DataFactory() {
	}

	/**
	 * Consente di creare oggetti di tipo {@link Data}.
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static Data createData(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {
		Data returnValue;

		/*
		 * testo il protocollo.
		 */
		switch (proxyConnectionHandler.getProtocolType()) {

		/*
		 * se è HTTP chiamo la factory per HttpData.
		 */
		case HTTP:
			returnValue = HttpDataFactory.createHttpData(
					proxyConnectionHandler, inputStream);
			break;

		/*
		 * se è qualsiasi altra cosa, il proxy non lo sa gestire e chiamo la
		 * factory per UnknownData.
		 */
		default:
			returnValue = UnknownDataFactory.createUnknownData(
					proxyConnectionHandler, inputStream);
			break;

		}
		return returnValue;
	}

}
