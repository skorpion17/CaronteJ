package it.uniroma2.sii.service.tor.webproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * 
 * @author andrea
 *
 */
public class ProxyConnectionHandler extends Thread {
	private final Socket clientSocket;
	private Socket proxyServerSocket;

	/** Stream per il client */
	private InputStream clientInputStream;
	private OutputStream clientOutputStream;

	/** Stream per il server */
	private InputStream serverInputStream;
	private OutputStream serverOutputStream;

	/** Indirizzo di destinazione a cui ci si connette attraverso la rete TOR */
	private SocketAddress destSockAddress;

	/**
	 * Costruttore.
	 * 
	 * @param clientSocket
	 * @throws IOException
	 */
	public ProxyConnectionHandler(final Socket clientSocket) throws IOException {
		this.clientSocket = clientSocket;
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
		// TODO Auto-generated method stub
	}

	/**
	 * Apre gli stream del client e del server.
	 * 
	 * @throws IOException
	 */
	private void openStreams() throws IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * Effettua il log dell'apertura della connessione e della richiesta del
	 * client.
	 */
	private void logConnection() {
		// TODO Auto-generated method stub
	}

	/**
	 * Congiunge le due socket. In pratica l'input di una socket diventa
	 * l'output dell'altra e viceversa.
	 */
	private void socketSplice() {
		// TODO Auto-generated method stub
		/*
		 * TODO 2 tipi diversi di log:
		 * 
		 * Log di livello 4: Richiesta di apertura della socket SOCKS con TOR
		 * 
		 * Log di livello 5: Log dell'header HTTP nel caso in cui la
		 * SocketAddress di destinazione sia su porta 80 o sia traffico HTTP.
		 */

		/*
		 * TODO: Per migliorare le performance i log dovrebbero essere inseriti
		 * all'interno di una struttura dati e poi un thread scansiona la
		 * struttura dati e rende peristenti su file i log. In questo modo si
		 * disaccoppia la gestion del log da quella di invio/ricezione dei dati
		 * da e verso la destinazione.
		 */
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
			// TODO: chiusura degli stream e socket.
		}
	}
}
