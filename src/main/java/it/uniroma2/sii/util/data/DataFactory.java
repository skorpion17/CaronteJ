package it.uniroma2.sii.util.data;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.HttpDataFactory;
import it.uniroma2.sii.util.data.unknown.UnknownDataFactory;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;

/**
 * È la classe che ha la responsabilità di creare oggetti di tipo {@link Data}.
 * 
 * @author Emanuele Altomare
 */
@Component
public class DataFactory {

	/**
	 * Costruttore di default.
	 */
	public DataFactory() {
	}

	/**
	 * Consente di creare oggetti di tipo {@link Data}.
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static Data createData(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {
		Data returnValue;

		/*
		 * testo il protocollo.
		 */
		switch (proxyConnectionHandler.getProtocolType()) {

		/*
		 * se è HTTP chiamo la factory per HttpData.
		 */
		case HTTP:
			returnValue = HttpDataFactory.createHttpData(
					proxyConnectionHandler, inputStream);
			break;

		/*
		 * se è qualsiasi altra cosa, il proxy non lo sa gestire e chiamo la
		 * factory per UnknownData.
		 */
		default:
			returnValue = UnknownDataFactory.createUnknownData(
					proxyConnectionHandler, inputStream);
			break;

		}
		return returnValue;
	}

}
