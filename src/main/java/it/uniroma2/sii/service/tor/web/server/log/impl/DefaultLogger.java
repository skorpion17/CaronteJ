package it.uniroma2.sii.service.tor.web.server.log.impl;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler.ProtocolType;
import it.uniroma2.sii.service.tor.web.server.WebProxyServer;
import it.uniroma2.sii.service.tor.web.server.log.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Logger per gestire una richiesta da parte del client giunta al
 * {@link WebProxyServer}.
 * 
 * @author andrea
 *
 */
public class DefaultLogger extends Logger {
	/**
	 * Nome del logger.
	 */
	private String name;

	/**
	 * Il logger prende come parametri {@link ProxyConnectionHandler} che
	 * gestisce la connessione e lo stream di input su cui si vuole loggare il
	 * traffico.
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 */
	public DefaultLogger(final ProxyConnectionHandler proxyConnectionHandler,
			final InputStream inputStream) {
		this(proxyConnectionHandler, inputStream, "No-Name");
	}

	/**
	 * Il logger prende come parametri {@link ProxyConnectionHandler} che
	 * gestisce la connessione e lo stream di input su cui si vuole loggare il
	 * traffico.
	 * 
	 * @param proxyConnectionHandler
	 * @param clientInputStream
	 * @param loggerName
	 */
	public DefaultLogger(ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream, String loggerName) {
		super(proxyConnectionHandler, inputStream);
		setName(loggerName);
	}

	/**
	 * Ottiene il nome dell logger.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Imposta il nome del logger.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Log per HTTP
	 * 
	 * TODO: Questo è un semplice stub; bisogna implementare correttamente il
	 * log.
	 * 
	 * @throws IOException
	 */
	private void logHTTP() throws IOException {
		/*
		 * Crea un reader orientato ai caratteri. NB: Non bisogna chiudere il
		 * Reader altrimenti viene chiuso anche lo stream.
		 */
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				getInputStream()));
		String line = null;

		/*
		 * FIXME: non e' richiesta tutta questa serializzazione, troppo
		 * bloccante.
		 */
		synchronized (System.out) {
			System.out.println("\n\t @@@ [ " + getName() + " HTTP Log ]: "
					+ getConnectionHandler().getDestSocketAddress());
			while ((line = reader.readLine()) != null) {
				if (line.length() == 0) {
					/* Letto tutto l'header */
					break;
				}
				System.out.println("\t @@@ [ " + getName() + " HTTP Log ]: "
						+ line);
			}
			System.out.println();
		}
	}

	/**
	 * Log per default. TODO:
	 * 
	 * @throws IOException
	 */
	private void logDefault() throws IOException {
		System.out.println("\n\t @@@ [ " + getName() + " HTTPS Log ]: "
				+ getConnectionHandler().getDestSocketAddress());
		System.out.println();
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniroma2.sii.service.tor.web.server.log.Logger#log()
	 */
	public void logImpl() throws IOException {
		/* In base al tipo di protocollo gestito dall'handler */
		final ProtocolType protocolType = getConnectionHandler()
				.getProtocolType();
		switch (protocolType) {
		case HTTP:
			logHTTP();
			break;
		case HTTPS:
		default:
			logDefault();
			break;
		}
	}
}
