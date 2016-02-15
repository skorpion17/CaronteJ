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
package it.uniroma2.sii.sock;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

/**
 * SOCKS Socket.
 * 
 * @author Andrea Mayer
 *
 */
public class SOCKSSocket extends Socket {
	/**
	 * Costruttore.
	 * 
	 * @param socksProxySocketAddress
	 */
	public SOCKSSocket(final InetSocketAddress socksProxySocketAddress) {
		/*
		 * Creo il proxy SOCKS per connettermi al Proxy di TOR
		 */
		super(new Proxy(Proxy.Type.SOCKS, socksProxySocketAddress));
	}
}
