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
package it.uniroma2.sii.log;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.response.HttpResponse;
import it.uniroma2.sii.util.data.unknown.UnknownData;

/**
 * Interfaccia per i Loggers.
 * 
 * @author Emanuele Altomare
 */
public interface Logger {

	/**
	 * Consente di effettuare il log per una richiesta HTTP.
	 */
	public abstract void log(HttpRequest httpRequest,
			ProxyConnectionHandler proxyConnectionHandler);

	/**
	 * Consente di effettuare il log per una risposta HTTP.
	 */
	public abstract void log(HttpResponse httpResponse,
			ProxyConnectionHandler proxyConnectionHandler);

	/**
	 * Consente di effettuare il log per dati sconosciuti, facenti parte di un
	 * protocollo non gestito.
	 */
	public void log(UnknownData unknownData,
			ProxyConnectionHandler proxyConnectionHandler);

	/**
	 * Consente di eseguire delle operazioni poco prima che venga chiusa la
	 * connessione.
	 * 
	 * @param proxyConnectionHandler
	 */
	public void closingConnection(ProxyConnectionHandler proxyConnectionHandler);

}
