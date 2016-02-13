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
