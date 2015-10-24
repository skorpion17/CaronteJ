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
