package it.uniroma2.sii.util.data.unknown;

import java.io.IOException;

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.Data;

/**
 * Rappresenta un dato che il proxy non è in grado di riconoscere.
 * 
 * @author Emanuele Altomare
 */
public class UnknownData extends Data {

	public UnknownData(ProxyConnectionHandler proxyConnectionHandler) {
		super(proxyConnectionHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.service.tor.web.server.util.Data#log(it.uniroma2.sii.
	 * log.Logger)
	 */
	@Override
	public void log(Logger logger) {
		logger.log(this, proxyConnectionHandler);
	}

	@Override
	public byte[] getDataInBytes() throws IOException {
		return null;
	}

}
