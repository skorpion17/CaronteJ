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
package it.uniroma2.sii.log.impl;

import it.uniroma2.sii.log.LoggerAbstract;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.HttpData;
import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.response.HttpResponse;
import it.uniroma2.sii.util.data.unknown.UnknownData;

/**
 * Consente di effettuare il log sullo schermo.
 * 
 * @author Emanuele Altomare, Andrea Mayer
 *
 */
public class ScreenLogger extends LoggerAbstract {

	/**
	 * Costruttore di default.
	 */
	public ScreenLogger() {
		super();
	}

	@Override
	public void log(HttpRequest httpRequest,
			ProxyConnectionHandler proxyConnectionHandler) {
		log((HttpData) httpRequest, proxyConnectionHandler);

	}

	@Override
	public void log(HttpResponse httpResponse,
			ProxyConnectionHandler proxyConnectionHandler) {
		log((HttpData) httpResponse, proxyConnectionHandler);
	}

	/**
	 * logga genericamente qualsiasi tipo di pacchetto HTTP.
	 * 
	 * @param httpData
	 * @param proxyConnectionHandler
	 */
	private void log(HttpData httpData,
			ProxyConnectionHandler proxyConnectionHandler) {
		String separator = "---------------------------------------\n";
		synchronized (getLock()) {
			System.out.print(separator);
			System.out.print(String.format("Connection UUID >>> %s\n",
					proxyConnectionHandler.getConnectionId()));
			System.out.print(httpData.toString());
			System.out.print(separator);
		}
	}

	@Override
	public void log(UnknownData unknownData,
			ProxyConnectionHandler proxyConnectionHandler) {
		String separator = "---------------------------------------\n";
		synchronized (getLock()) {
			System.out.print(separator);
			System.out.print(String.format("Connection UUID >>> %s\n",
					proxyConnectionHandler.getConnectionId()));
			System.out.print("Protocollo non gestito!\n");
			System.out.print(separator);
		}

	}

	@Override
	public void closingConnection(ProxyConnectionHandler proxyConnectionHandler) {
	}
}
