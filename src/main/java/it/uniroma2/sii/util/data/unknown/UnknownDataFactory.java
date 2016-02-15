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
package it.uniroma2.sii.util.data.unknown;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.Data;

import java.io.IOException;
import java.io.InputStream;

/**
 * Ha il compito di creare gli oggetti {@link UnknownData}.
 * 
 * @author Emanuele Altomare
 */
public class UnknownDataFactory {

	/**
	 * Costruttore di default.
	 */
	public UnknownDataFactory() {
	}

	public static Data createUnknownData(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {

		if (proxyConnectionHandler == null || inputStream == null) {
			throw new IOException(
					"FATAL ERROR: connection handler or input stream are null!");
		}

		return new UnknownData(proxyConnectionHandler);
	}

}
