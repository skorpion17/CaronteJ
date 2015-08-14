package it.uniroma2.sii.service.tor.web.server;

import it.uniroma2.sii.service.tor.OnionBinderService;
import it.uniroma2.sii.sock.SOCKSSocket;
import it.uniroma2.sii.util.io.IOUtils;
import it.uniroma2.sii.util.socket.SocketUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 
 * @author andrea
 *
 */
public class ProxyConnectionHandler extends Thread {
	/**
	 * TODO: Un massimo di 100 campi nell'header oguno dei quali deve essere al
	 * massimo lungo 8190.
	 */
	private static final int BUFFER_INPUT_STREAM_SIZE_IN_BYTE = 8192;

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
				log(serverInputStream);

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
	 * Logging. TODO: DIFFERENZIARE LA REQUEST DALLA RESPONSE.
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	private void log(final InputStream inputStream) throws IOException {
		switch (protocolType) {
		case HTTP:
			logHTTPHeader(inputStream);
			break;
		case HTTPS:
			logHTTPSHeader(inputStream);
			break;
		default:
			logDefaultImpl(inputStream);
			break;
		}
	}

	/**
	 * Permette di lanciare l'avvio dell'attività di logging per la richiesta
	 * del client.
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	private void logHTTPHeader(final InputStream inputStream)
			throws IOException {
		/* Viene impostato il marker e si logga successivamente */
		inputStream.mark(BUFFER_INPUT_STREAM_SIZE_IN_BYTE);
		/* Si logga l'header */
		logHTTPImpl(inputStream);
		/*
		 * Si resetta l'header in modo tale che lo stream possa essere
		 * nuovamente letto dal punto in cui è stato inserito il marker
		 */
		inputStream.reset();
	}

	/**
	 * Entry point per l'implementazione del log per HTTPS.
	 * 
	 * @param inputStream
	 */
	private void logHTTPSHeader(final InputStream inputStream)
			throws IOException {
		logHTTPSImpl(inputStream);
	}

	/**
	 * TODO: TEMPLATE METHOD, RENDERE PROXY_CONNECTION_HANDLER ASTRATTA E
	 * FORZARE LA DEFINIZIONE DI logHTTPImpl.
	 * 
	 * @param inputStream
	 */
	public void logHTTPImpl(final InputStream inputStream) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		while ((line = reader.readLine()) != null) {
			if (line.length() == 0) {
				/* Trovato il marker di fine HEADER per HTTP */
				break;
			}
			System.out.println("Header:" + line);
		}
	}

	// FIXME: ABSTRACT
	public void logHTTPSImpl(final InputStream inputStream) throws IOException {
		System.out.printf("\t >>> HTTPS Request: [%s] <<<\n",
				destSocketAddress.toString());
	}

	// FIXME
	public void logDefaultImpl(final InputStream inputStream)
			throws IOException {
		System.out.println("logDefaultImpl NOT YET IMPLEMENTED");
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
		log(clientInputStream);
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
