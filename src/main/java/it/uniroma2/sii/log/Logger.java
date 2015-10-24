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
