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
package it.uniroma2.sii.service.tor.web.server;

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.log.LoggerHandler;
import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.service.tor.OnionBinderService;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler.ProtocolType;
import it.uniroma2.sii.util.io.IOUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * HTTProxyServer
 * 
 * @author Andrea Mayer, Emanuele Altomare
 *
 */
@Service
public class WebProxyServer extends Thread {
	/**
	 * Porte per HTTP
	 */
	@Value("${http.service.ports}")
	private int[] httpServicePorts;
	private HashSet<Integer> httpServicePortsSet = new HashSet<Integer>();
	/**
	 * Porte per HTTPS
	 */
	@Value("${https.service.ports}")
	private int[] httpsServicePorts;
	private HashSet<Integer> httpsServicePortsSet = new HashSet<Integer>();
	/**
	 * Porta su cui il server WebProxyServer è in ascolto.
	 */
	@Value("${proxy.web.server.bind.port}")
	private int httpProxyServerBindPort;
	/**
	 * Indirizzo su cui il proxy di TOR è in ascolto
	 */
	@Value("${proxy.tor.bind.address}")
	private String proxyTorBindAddress;
	/**
	 * Porta su cui il proxy di TOR è in ascolto
	 */
	@Value("${proxy.tor.bind.port}")
	private int proxyTorBindPort;
	/**
	 * Timeout nell'apertura della socks con il client di Tor per il web proxy.
	 */
	@Value("${proxy.web.server.socks.timeout}")
	private int webProxySOCKSTimeoutInMillis;

	@Autowired
	private OnionBinderService onionBinderService;

	@Autowired
	private LoggerHandler logger;

	@PostConstruct
	private void init() {
		/*
		 * Inizializza gli hashset in modo tale che la ricerca di un elemento al
		 * loro interno sia in tempo O(1)
		 */
		for (final int httpPort : httpServicePorts) {
			httpServicePortsSet.add(httpPort);
		}
		for (final int httpsPort : httpsServicePorts) {
			httpsServicePortsSet.add(httpsPort);
		}
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			/* ServerSocket su cui HTTPRoxyServer è in ascolto */
			serverSocket = new ServerSocket(httpProxyServerBindPort);

			System.out.println("\t >>> WebProxyServer is started <<<");

			while (true) {
				/* Per ogni connessione in arrivo */
				try {
					final Socket clientSocket = serverSocket.accept();

					/*
					 * Gestisce la nuova connessione in arrivo
					 */
					new ProxyConnectionHandler(this, clientSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuitely(serverSocket);
		}
	}

	/**
	 * Ritorna il timeout in millisecondi per la connessione della socket verso
	 * il client Tor.
	 * 
	 * @return
	 */
	public int getWebProxySOCKSTimeoutInMillis() {
		return webProxySOCKSTimeoutInMillis;
	}

	/**
	 * Ottiene il riferimento al service per {@link OnionBinder}.
	 * 
	 * @return
	 */
	public OnionBinderService getOnionBinderService() {
		return onionBinderService;
	}

	/**
	 * Consente di ottenere il LoggerHandler.
	 * 
	 * @return
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * In base alla porta determina il protocollo in uso.
	 * 
	 * @param port
	 * @return
	 */
	public ProtocolType checkProtocolTypeByPort(int port) {
		if (httpServicePortsSet.contains(port)) {
			/* E' di tipo HTTP il protocollo in uso */
			return ProtocolType.HTTP;
		} else if (httpsServicePortsSet.contains(port)) {
			/* E' di tipo HTTPS il protocollo in uso */
			return ProtocolType.HTTPS;
		} else {
			return ProtocolType.UNKNOWN;
		}
	}

	/**
	 * Ottiene l'indirizzo presso cui il server di tor è in ascolto.
	 * 
	 * @return
	 */
	public InetSocketAddress getTorSocketAddress() {
		return InetSocketAddress.createUnresolved(proxyTorBindAddress,
				proxyTorBindPort);
	}
}
