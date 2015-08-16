package it.uniroma2.sii.service.tor.web.server.log;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * Logger.
 * 
 * @author andrea
 *
 */
public abstract class Logger {
	private static final int READ_LOOK_AHEAD_LIMIT = 8192;

	private final ProxyConnectionHandler proxyConnectionHandler;
	private final InputStream inputStream;
	private int readLookAhead = READ_LOOK_AHEAD_LIMIT;

	/**
	 * Costruttore del logger.
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 */
	public Logger(final ProxyConnectionHandler proxyConnectionHandler,
			final InputStream inputStream) {
		this.proxyConnectionHandler = proxyConnectionHandler;
		this.inputStream = inputStream;
	}

	/**
	 * Ottiene l'inputStream.
	 * 
	 * @return
	 */
	protected InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Ottiene il {@link ProxyConnectionHandler} a cui si vuole fornire il
	 * servizio di logging.
	 * 
	 * @return
	 */
	public ProxyConnectionHandler getConnectionHandler() {
		return proxyConnectionHandler;
	}

	/**
	 * Ottiene il numero di byte che possono essere letti dall'inputStream senza
	 * che questi vengano effettivamente consumati.
	 * 
	 * @return
	 */
	public int getReadLookAhead() {
		return readLookAhead;
	}

	/**
	 * Imposta il numero di byte che possono essere letti dall'inputStream senza
	 * che questi vengano effettivamente consumati.
	 * 
	 * @param readLookAhead
	 */
	public void setReadLookAhead(int readLookAhead) {
		this.readLookAhead = readLookAhead;
	}

	/**
	 * Effettua l'operazione di logging. L'operazone di log permette
	 * automaticamente di bufferizzare l'inputStream al fine di loggare
	 * eventuali header prima del payload. Dopo l'operazioned i log
	 * automaticamente lo stream viene riportato allo stato precedente alla
	 * operazione. Per cambiare la dimensione del buffer utilizzato per
	 * memorizzare l'intestazione dallo stream vedere
	 * {@link it.uniroma2.sii.service.tor.web.server.log.Logger#setReadLookAhead(int)}
	 * 
	 * @throws IOException
	 */
	public void log() throws IOException {
		inputStream.mark(readLookAhead);
		logImpl();
		inputStream.reset();
	}

	/**
	 * Implementazione dell'operazioned di log.
	 * 
	 * @throws IOException
	 */
	public abstract void logImpl() throws IOException;
}