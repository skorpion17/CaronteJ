package it.uniroma2.sii.service.tor.web.server;

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.service.tor.OnionBinderService;
import it.uniroma2.sii.sock.SOCKSSocket;
import it.uniroma2.sii.util.data.Data;
import it.uniroma2.sii.util.data.DataFactory;
import it.uniroma2.sii.util.data.unknown.UnknownData;
import it.uniroma2.sii.util.io.IOUtils;
import it.uniroma2.sii.util.socket.SocketUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

/**
 * Gestisce la connessione con il proxy.
 * 
 * @author Andrea Mayer, Emanuele Altomare
 */
public class ProxyConnectionHandler extends Thread {
	private static final int BUFFER_INPUT_STREAM_SIZE_IN_BYTE = 1024;

	/**
	 * questo id identifica la connessione.
	 */
	private final UUID connectionId;

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
	 * Logger
	 */
	private Logger logger;

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
	 * @author Andrea Mayer, Emanuele Altomare
	 *
	 */
	private class AsynchronousResponse extends Thread {
		private IOException lastIOException = null;
		private ProxyConnectionHandler proxyConnectionHandler = null;

		/**
		 * Si pone in attesa di risposta da parte del server trasferendo quanto
		 * letto sulla socket del client.
		 */
		public AsynchronousResponse(
				ProxyConnectionHandler proxyConnectionHandler) {
			this.proxyConnectionHandler = proxyConnectionHandler;
			start();
		}

		@Override
		public void run() {
			try {

				/*
				 * creo l'oggetto che rappresenta il dato all'interno del proxy.
				 */
				Data response = DataFactory.createData(proxyConnectionHandler,
						serverInputStream);

				/*
				 * se è un dato sconosciuto relativo ad un protocollo non
				 * gestito...
				 */
				if (response instanceof UnknownData) {

					/*
					 * si potrebbe decidere anche di bloccare il traffico di
					 * protocolli non gestiti dal proxy in questo punto.
					 */

					/*
					 * Ottiene la risposta dal server (proxy TOR) e la inoltra
					 * al client.
					 */
					final byte[] bufferServerToClient = new byte[BUFFER_INPUT_STREAM_SIZE_IN_BYTE];
					int read = -1;
					while ((read = serverInputStream.read(bufferServerToClient)) != -1) {

						/*
						 * L'input del serverSocket viene passato alla
						 * clientSocket
						 */
						clientOutputStream.write(bufferServerToClient, 0, read);
						clientOutputStream.flush();
					}
				}

				/*
				 * applico dei filtri sulla risposta.
				 */
				applyResponseFilters(response);

				/*
				 * invio la risposta al client.
				 */
				byte[] responseBytes = response.getDataInBytes();
				if (responseBytes != null) {
					clientOutputStream.write(responseBytes);
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
		/*
		 * genero un id random e lo assegno a questa connessione.
		 */
		connectionId = UUID.randomUUID();

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
	 * Consente di ottenere l'id della connessione.
	 * 
	 * @return
	 */
	public UUID getConnectionId() {
		return connectionId;
	}

	/**
	 * setta il logger.
	 */
	private void setLogger() {
		logger = httpProxyServer.getLogger();
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
		 * creo l'oggetto che rappresenta il dato all'interno del proxy.
		 */
		Data request = DataFactory.createData(this, clientInputStream);

		/*
		 * se è un dato sconosciuto relativo ad un protocollo non gestito dal
		 * proxy...
		 */
		if (request instanceof UnknownData) {

			/*
			 * si potrebbe decidere anche di bloccare il traffico di protocolli
			 * non gestiti dal proxy in questo punto.
			 */

			/*
			 * Ottiene la risposta dal server (proxy TOR) e la inoltra al
			 * client.
			 */
			final byte[] bufferClientToServer = new byte[BUFFER_INPUT_STREAM_SIZE_IN_BYTE];
			int read = -1;
			while ((read = clientInputStream.read(bufferClientToServer)) != -1) {
				/*
				 * L'input del serverSocket viene passato alla clientSocket
				 */
				serverOutputStream.write(bufferClientToServer, 0, read);
				serverOutputStream.flush();
			}
		}

		/*
		 * applico dei filtri sulla richiesta.
		 */
		applyRequestFilters(request);

		/*
		 * trasmetto la richiesta al server TOR.
		 */
		byte[] requestBytes = request.getDataInBytes();
		if (requestBytes != null) {
			serverOutputStream.write(requestBytes);
			serverOutputStream.flush();
		}

	}

	/**
	 * Qui dentro possono essere inserite le chiamate ai vari filtri da
	 * applicare sul dato della richiesta.
	 * 
	 * @param request
	 */
	private void applyRequestFilters(Data request) {
		if (request == null) {
			return;
		}

		/*
		 * effettua l'operazione di log.
		 */
		request.log(logger);
	}

	/**
	 * Qui dentro possono essere inserite le chiamate ai vari filtri da
	 * applicare sul dato della richiesta.
	 * 
	 * @param request
	 */
	private void applyResponseFilters(Data response) {
		if (response == null) {
			return;
		}

		/*
		 * effettua l'operazione di log.
		 */
		response.log(logger);
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
		 * Avvio la lettura asincrona della risposta del server in modo tale da
		 * sfruttare la bidirezionalità del collegamento
		 */
		final AsynchronousResponse asynchronousResponse = new AsynchronousResponse(
				this);

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
			/* Setta il logger */
			setLogger();
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
			doSomeFinalOperations();
			closeAllQuitely();
		}
	}

	/**
	 * Consente di eseguire delle operazioni alla fine, poco prima che la
	 * connessione venga chiusa.
	 */
	private void doSomeFinalOperations() {
		/*
		 * faccio sapere al logger che la connessione sta per essere chiusa.
		 */
		logger.closingConnection(this);

	}
}
