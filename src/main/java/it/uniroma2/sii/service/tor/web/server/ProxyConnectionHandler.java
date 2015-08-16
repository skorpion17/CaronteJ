package it.uniroma2.sii.service.tor.web.server;

import it.uniroma2.sii.service.tor.OnionBinderService;
import it.uniroma2.sii.service.tor.web.server.log.Logger;
import it.uniroma2.sii.service.tor.web.server.log.impl.DefaultLogger;
import it.uniroma2.sii.sock.SOCKSSocket;
import it.uniroma2.sii.util.io.IOUtils;
import it.uniroma2.sii.util.socket.SocketUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 
 * @author andrea
 *
 */
public class ProxyConnectionHandler extends Thread {
	private static final int BUFFER_INPUT_STREAM_SIZE_IN_BYTE = 1024;

	private final WebProxyServer httpProxyServer;
	private final OnionBinderService onionBinderService;

	private final Socket clientSocket;
	private Socket proxyServerSocket;

	/** Stream per il client */
	private InputStream clientInputStream;
	private OutputStream clientOutputStream;

	/** Stream per il server */
	private InputStream serverInputStream;
	private OutputStream serverOutputStream;

	/** Indirizzo di destinazione a cui ci si connette attraverso la rete TOR */
	private InetSocketAddress destSocketAddress;

	/**
	 * Logger per la richiesta.
	 */
	private Logger requestLogger;
	/**
	 * Logger per la risposta.
	 */
	private Logger responseLogger;

	/**
	 * Tipi di protocolli supportati dall'handler.
	 * 
	 * @author andrea
	 *
	 */
	public enum ProtocolType {
		HTTP, HTTPS, UNKNOWN
	}

	/**
	 * Protocollo attualmente supportato dall'handler.
	 */
	private ProtocolType protocolType;

	/**
	 * Lettura asincrona della risposta del server.
	 * 
	 * @author andrea
	 *
	 */
	private class AsynchronousResponse extends Thread {
		private IOException lastIOException = null;

		/**
		 * Si pone in attesa di risposta da parte del server trasferendo quanto
		 * letto sulla socket del client.
		 */
		public AsynchronousResponse() {
			start();
		}

		@Override
		public void run() {
			try {
				/*
				 * Operazione di log.
				 */
				// log(serverInputStream);
				if (responseLogger != null) {
					responseLogger.log();
				}

				/*
				 * Ottiene la risposta dal server (proxy TOR) e la inoltra al
				 * client.
				 */
				final byte[] bufferServerToClient = new byte[BUFFER_INPUT_STREAM_SIZE_IN_BYTE];
				int read = -1;
				while ((read = serverInputStream.read(bufferServerToClient)) != -1) {
					/*
					 * L'input del serverSocket viene passato alla clientSocket
					 */
					clientOutputStream.write(bufferServerToClient, 0, read);
					clientOutputStream.flush();
				}
			} catch (IOException e) {
				/* Memorizza l'ultima eccezione che è stata sollevata nel run() */
				lastIOException = e;
			}
		}

		/**
		 * Attende che la risposta del server sia stata completamente letta e
		 * inviata alla socket del client. Se si verifica una eccezione durante
		 * la lettura dalla {@code serverInputStream} e la scrittura su
		 * {@code clientOutputStream} il metodo la ritorna.
		 * 
		 * @throws InterruptedException
		 * 
		 * @throws Exception
		 */
		public void receiveResponse() throws IOException, InterruptedException {
			join();
			if (lastIOException != null) {
				throw lastIOException;
			}
		}
	}

	/**
	 * Costruttore.
	 * 
	 * @param clientSocket
	 * @throws IOException
	 */
	public ProxyConnectionHandler(final WebProxyServer httpProxyServer,
			final Socket clientSocket) throws IOException {
		this.httpProxyServer = httpProxyServer;
		this.clientSocket = clientSocket;
		this.onionBinderService = this.httpProxyServer.getOnionBinderService();
		/*
		 * Viene avviato il thread che tenta di stabilire la connessione con il
		 * Proxy TOR; NB: questa operazione può essere anche molto lenta.
		 */
		start();
	}

	/**
	 * Ottiene il logger per la richiesta.
	 * 
	 * @return
	 */
	public Logger getLoggerRequest() {
		return requestLogger;
	}

	/**
	 * Imposta il logger per la richiesta.
	 * 
	 * @param requestLogger
	 */
	public void setLoggerRequest(Logger loggerRequest) {
		this.requestLogger = loggerRequest;
	}

	/**
	 * Ottiene il logger per la risposta.
	 * 
	 * @return
	 */
	public Logger getLoggerResponse() {
		return responseLogger;
	}

	/**
	 * Imposta il logger per la risposta.
	 * 
	 * @param responseLogger
	 */
	public void setLoggerResponse(Logger loggerResponse) {
		this.responseLogger = loggerResponse;
	}

	/**
	 * @return the destSocketAddress
	 */
	public InetSocketAddress getDestSocketAddress() {
		return destSocketAddress;
	}

	/**
	 * Ritorna il tipo di protocollo gestito dall'handler.
	 * 
	 * @return
	 */
	public ProtocolType getProtocolType() {
		return protocolType;
	}

	/**
	 * Effettua la connessione con il proxy di TOR.
	 * 
	 * @throws IOException
	 */
	private void torConnect() throws IOException {
		/*
		 * Dalla socket del client viene estratta la porta e l'indirizzo di
		 * destinazione a cui il proxy http effettuerà a sua volta la richiesta
		 * attraverso la rete TOR
		 */
		destSocketAddress = (InetSocketAddress) SocketUtils
				.getDestSockAddress(clientSocket);
		/* Si apre la socket con il Proxy di TOR */
		proxyServerSocket = new SOCKSSocket(
				httpProxyServer.getTorSocketAddress());
		/* .onion */
		if (onionBinderService
				.isInetAddressForInternalOnionResolution(destSocketAddress
						.getAddress())) {
			/*
			 * L'indirizzo ip è interno e riservato per la risoluzione interna
			 * degli hidden service (.onion)
			 */
			destSocketAddress = (InetSocketAddress) onionBinderService
					.resolveCachedOnionNameByInternalInetSocketAddress(destSocketAddress);
		}
		/*
		 * Si connette alla destinazione attraverso la rete di TOR, con la
		 * connect esplicita sulla socket; inoltre si crea un SocketAddress
		 * senza che vi sia la risoluzione del nome in modo tale che sia la rete
		 * TOR a risolvere il nome qualora ve ne fosse la necessità.
		 */
		proxyServerSocket.connect(destSocketAddress);
		/*
		 * Si cerca di determinare in base alla porta di destinazione il tipo di
		 * protocollo utilizzato dal client.
		 */
		protocolType = httpProxyServer
				.checkProtocolTypeByPort(destSocketAddress.getPort());
	}

	/**
	 * Apre gli stream del client e del server.
	 * 
	 * @throws IOException
	 */
	private void openStreams() throws IOException {
		/* Riferimento allo stream di output del server */
		serverOutputStream = proxyServerSocket.getOutputStream();

		/* Riferimento allo stram di output del client */
		clientOutputStream = clientSocket.getOutputStream();

		/* Riferimento allo stream di input del server */
		serverInputStream = new BufferedInputStream(
				proxyServerSocket.getInputStream());

		/* Riferimento allo stream di input del client */
		clientInputStream = new BufferedInputStream(
				clientSocket.getInputStream());
	}

	/**
	 * Inizializzai i logger per la richiesta e la risposta.
	 */
	private void initLogger() {
		if (clientInputStream != null) {
			requestLogger = new DefaultLogger(this, clientInputStream,
					"Request");
		}
		if (serverInputStream != null) {
			responseLogger = new DefaultLogger(this, serverInputStream,
					"Response");
		}
	}

	/**
	 * Chiude gli stream e le socket in modo aggraziato liberando le risorse.
	 */
	private void closeAllQuitely() {
		/* Si chiudono gli inputStream */
		IOUtils.closeQuitely(clientInputStream);
		IOUtils.closeQuitely(serverInputStream);

		/* Si chiudono gli outputStream */
		IOUtils.closeQuitely(clientOutputStream);
		IOUtils.closeQuitely(serverOutputStream);

		/* Si chiudono le socket */
		IOUtils.closeQuitely(clientSocket);
		IOUtils.closeQuitely(proxyServerSocket);
	}

	/**
	 * Invia la richiesta inoltrata dal client al lato server per poterla girare
	 * su TOR.
	 * 
	 * @throws IOException
	 */
	private void sendRequest() throws IOException {
		/*
		 * Operazioni di log.
		 */
		// log(clientInputStream);

		if (requestLogger != null) {
			/* Il logger per la request è stato impostato */
			requestLogger.log();
		}

		/*
		 * Si inoltra la richiesta al server (proxy TOR).
		 */
		final byte[] bufferClientToServer = new byte[BUFFER_INPUT_STREAM_SIZE_IN_BYTE];
		int read = -1;
		while ((read = clientInputStream.read(bufferClientToServer)) != -1) {
			/* L'input della clientSocket viene passata alla serverSocket */
			serverOutputStream.write(bufferClientToServer, 0, read);
			serverOutputStream.flush();
		}
	}

	/**
	 * Congiunge le due socket. In pratica l'input di una socket diventa
	 * l'output dell'altra e viceversa.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void socketSplice() throws IOException, InterruptedException {
		/*
		 * TODO: Per migliorare le performance i log dovrebbero essere inseriti
		 * all'interno di una struttura dati e poi un thread scansiona la
		 * struttura dati e rende peristenti su file i log. In questo modo si
		 * disaccoppia la gestion del log da quella di invio/ricezione dei dati
		 * da e verso la destinazione.
		 */

		/*
		 * Avvio la lettura asincrona della risposta del server in modo tale da
		 * sfruttare la bidirezionalità del collegamento
		 */
		final AsynchronousResponse asynchronousResponse = new AsynchronousResponse();

		/*
		 * Invia la richiesta dalla socket del client a quella del server per
		 * utilizzare la rete TOR
		 */
		sendRequest();
		/*
		 * Attende che sia arrivata tutta la risposta dal server, avendola gia
		 * rigiratà opportunamente sulla socket del client
		 */
		asynchronousResponse.receiveResponse();
	}

	@Override
	public void run() {
		try {
			/* Stabilisce la connessione con il proxy di TOR */
			torConnect();
			/* Apre gli streams */
			openStreams();
			/* Imposta i logger */
			initLogger();
			/* Scambio dei dati */
			socketSplice();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			/* chiusura degli streams e delle socket */
			closeAllQuitely();
		}
	}
}
