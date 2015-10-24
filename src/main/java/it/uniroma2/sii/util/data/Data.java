package it.uniroma2.sii.util.data;

import java.io.IOException;

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;

/**
 * Rappresenta il dato che scorre in entrata e uscita sulla socket.
 * 
 * @author Emanuele Altomare
 */
public abstract class Data implements Filter {

	/**
	 * metto un riferimento all'handler della connessione all'interno della
	 * quale è fluito il dato.
	 */
	protected final ProxyConnectionHandler proxyConnectionHandler;

	public Data(ProxyConnectionHandler proxyConnectionHandler) {
		this.proxyConnectionHandler = proxyConnectionHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.service.tor.web.server.util.Data#log(it.uniroma2.sii.
	 * log.Logger)
	 */
	@Override
	public abstract void log(Logger logger);

	/**
	 * Consente di ottenere l'handler della connessione all'interno della quale
	 * è fruito il dato.
	 * 
	 * @return
	 */
	public ProxyConnectionHandler getProxyConnectionHandler() {
		return proxyConnectionHandler;
	}

	/**
	 * Consente di ottenere il dato in bytes.
	 * 
	 * @return
	 * @throws IOException 
	 */
	public abstract byte[] getDataInBytes() throws IOException;
}
