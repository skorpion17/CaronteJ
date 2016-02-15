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

import java.io.IOException;

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;

/**
 * Rappresenta il dato che scorre in entrata e uscita sulla socket.
 * 
 * @author Emanuele Altomare
 */
public abstract class Data implements Filter {

	/**
	 * metto un riferimento all'handler della connessione all'interno della
	 * quale è fluito il dato.
	 */
	protected final ProxyConnectionHandler proxyConnectionHandler;

	public Data(ProxyConnectionHandler proxyConnectionHandler) {
		this.proxyConnectionHandler = proxyConnectionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.service.tor.web.server.util.Data#log(it.uniroma2.sii.
	 * log.Logger)
	 */
	@Override
	public abstract void log(Logger logger);

	/**
	 * Consente di ottenere l'handler della connessione all'interno della quale
	 * è fruito il dato.
	 * 
	 * @return
	 */
	public ProxyConnectionHandler getProxyConnectionHandler() {
		return proxyConnectionHandler;
	}

	/**
	 * Consente di ottenere il dato in bytes.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public abstract byte[] getDataInBytes() throws IOException;
}
