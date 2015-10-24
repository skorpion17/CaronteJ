package it.uniroma2.sii.util.data.unknown;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.Data;

import java.io.IOException;
import java.io.InputStream;

/**
 * Ha il compito di creare gli oggetti {@link UnknownData}.
 * 
 * @author Emanuele Altomare
 */
public class UnknownDataFactory {

	/**
	 * Costruttore di default.
	 */
	public UnknownDataFactory() {
	}

	public static Data createUnknownData(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {

		if (proxyConnectionHandler == null || inputStream == null) {
			throw new IOException(
					"FATAL ERROR: connection handler or input stream are null!");
		}

		return new UnknownData(proxyConnectionHandler);
	}

}
