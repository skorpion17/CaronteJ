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

import java.io.IOException;

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.Data;

/**
 * Rappresenta un dato che il proxy non è in grado di riconoscere.
 * 
 * @author Emanuele Altomare
 */
public class UnknownData extends Data {

	public UnknownData(ProxyConnectionHandler proxyConnectionHandler) {
		super(proxyConnectionHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.service.tor.web.server.util.Data#log(it.uniroma2.sii.
	 * log.Logger)
	 */
	@Override
	public void log(Logger logger) {
		logger.log(this, proxyConnectionHandler);
	}

	@Override
	public byte[] getDataInBytes() throws IOException {
		return null;
	}

}
