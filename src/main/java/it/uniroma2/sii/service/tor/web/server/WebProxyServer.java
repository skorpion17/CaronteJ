package it.uniroma2.sii.service.tor.web.server;

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
 * @author andrea
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

	@Autowired
	private OnionBinderService onionBinderService;

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
	 * Ottiene il riferimento al service per {@link OnionBinder}.
	 * 
	 * @return
	 */
	public OnionBinderService getOnionBinderService() {
		return onionBinderService;
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
	 * Ottiene l'indirizzo presso cui il server di tor è in ascoto.
	 * 
	 * @return
	 */
	public InetSocketAddress getTorSocketAddress() {
		return InetSocketAddress.createUnresolved(proxyTorBindAddress,
				proxyTorBindPort);
	}
}
