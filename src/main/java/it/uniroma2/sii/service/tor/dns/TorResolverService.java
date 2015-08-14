package it.uniroma2.sii.service.tor.dns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Consente di effettuare una risoluzione DNS attraverso TOR.
 * 
 * @author Emanuele Altomare
 */
@Service
public class TorResolverService {
	/** Costante per dire a TOR di effettuare una risoluzione DNS. */
	private final static byte TOR_RESOLVE = (byte) 0xF0;
	/** Costante per dire quale versione di SOCKS utilizzare */
	private final static byte TOR_RESOLVER_SOCKS_VERSION_4A = (byte) 0x04;
	/** Separatore. */
	private final static byte SOCKS_DELIM = (byte) 0x00;
	/** Settando il campo IP a 0.0.0.1 abilito l'uso di SOCKS4a. */
	private final static int SOCKS4A_FAKEIP = (int) 0x01;
	/** Dimensione in byte di un indirizzo ipv4 */
	private final static int IPV4_SIZE = 0x04;

	/**
	 * Codici di risposta per SOCKS4.
	 * 
	 * TODO: Spostare questi codici in una interfaccia per renderli separati da
	 * TorResolverService ?
	 */
	private final static byte SOCKS4A_REPLY_CODE_REQUEST_GRANTED = 90;
	private final static byte SOCKS4A_REPLY_CODE_REJECT_OR_FAILED = 91;
	private final static byte SOCKS4A_REPLY_CODE_REJECT_SOCKS_SERVER_CANNOT_CONNECT_TO_CLIENT = 92;
	private final static byte SOCKS4A_REPLY_CODE_REJECT_PROGRAM_AND_IDENTD_MISMATCH_USER_IDS = 93;

	/**
	 * Indirizzo sulla quale effettuare il bind con il proxy tor per la
	 * risoluzione dei nomi attraverso la rete. Il valore viene letto dal file
	 * di configurazione.
	 */
	@Value("${proxy.tor.bind.address}")
	private String proxyTorBindAddress;

	/**
	 * Porta sulla quale contattare il proxy tor per la risoluzione dei nomi
	 * attraverso la rete. Il valore viene letto dal file di configurazione.
	 */
	@Value("${proxy.tor.bind.port}")
	private int proxyTorBindPort;

	/**
	 * Risolve l'indirizzo {@code hostname} attraverso la rete TOR.
	 * 
	 * @param hostname
	 * @return
	 * @throws Exception
	 */
	public InetAddress resolveDNS(final String hostname)
			throws UnknownHostException, IOException {
		if (hostname == null || hostname.length() == 0) {
			/* L'host non è valido */
			throw new UnknownHostException("Hostname cannot be null or empty.");
		}

		/*
		 * Si apre la socket verso il proxy di TOR e si ottengono l'input e
		 * l'output stream
		 */
		Socket socket = null;
		DataInputStream inputStream = null;
		DataOutputStream outputStream = null;
		try {
			socket = new Socket(proxyTorBindAddress, proxyTorBindPort);
			inputStream = new DataInputStream(socket.getInputStream());
			outputStream = new DataOutputStream(socket.getOutputStream());

			/* Inizio SOCKS4a */
			outputStream.writeByte(TOR_RESOLVER_SOCKS_VERSION_4A);
			outputStream.writeByte(TOR_RESOLVE);
			/*
			 * scrivo i due byte della porta, in questo caso uso 0 perchè la
			 * porta non serve per la risoluzione.
			 */
			outputStream.writeShort(0);
			outputStream.writeInt(SOCKS4A_FAKEIP);
			outputStream.writeByte(SOCKS_DELIM);
			outputStream.writeBytes(hostname);
			outputStream.writeByte(SOCKS_DELIM);
			/* elimino il byte di versione del protocollo dallo stream. */
			inputStream.readByte();

			/*
			 * Messaggio di risposta.
			 */
			final byte replyStatusCode = inputStream.readByte();
			if (replyStatusCode != SOCKS4A_REPLY_CODE_REQUEST_GRANTED) {
				/*
				 * Si parsa il byte di risposta associando una descrizione
				 * dell'errore.
				 */
				throw new IOException(parseSocksV4Error(replyStatusCode));
			}

			/* Vengono letti i 2 byte per eliminare la porta dallo stream */
			inputStream.readShort();
			/*
			 * Creo un array di 4 byte per ospitare l'indirizzo risolto e lo
			 * memorizzo.
			 */
			final byte[] ipAddrBytes = new byte[IPV4_SIZE];
			if (inputStream.read(ipAddrBytes) != IPV4_SIZE) {
				/* Errore durante la lettura dell'indirizzo IPv4 */
				throw new IOException("IPv4 size error.");
			}

			/*
			 * Si costruisce un oggetto che identifica un indirizzo di rete.
			 * XXX: non viene eseguito nessun tentativo di risoluzione inverso.
			 */
			return InetAddress.getByAddress(ipAddrBytes);
		} catch (UnknownHostException e) {
			/* Rilancia l'eccezione */
			throw e;
		} catch (IOException e) {
			/* Rilancia l'eccezione */
			throw e;
		} finally {
			/*
			 * Qui si chiudono gli stream aperti e la socket. Questa è una
			 * chiusura corretta.
			 */
			IOException lastException = null;
			if (inputStream != null) {
				try {
					/* Chiudo l'inputStream */
					inputStream.close();
				} catch (IOException e) {
					lastException = e;
				}
			}
			if (outputStream != null) {
				try {
					/* Chiudo l'outputStream */
					outputStream.close();
				} catch (IOException e) {
					lastException = e;
				}
			}
			if (socket != null) {
				try {
					/* Chiudo la socket */
					socket.close();
				} catch (IOException e) {
					lastException = e;
				}
			}
			/*
			 * Se si è verificata una eccezione durante le operazioni di
			 * chiusura, allora rilancia l'ultima eccezione sollevata.
			 */
			if (lastException != null) {
				throw lastException;
			}
		}
	}

	/**
	 * Associa al codice {@code status} un messaggio di descrizione.
	 * 
	 * @param status
	 * @return
	 */
	private String parseSocksV4Error(byte status) {
		String retval = null;
		switch (status) {
		case SOCKS4A_REPLY_CODE_REQUEST_GRANTED:
			retval = status + " Request granted.";
			break;
		case SOCKS4A_REPLY_CODE_REJECT_OR_FAILED:
			retval = status + " Request rejected/failed - unknown reason.";
			break;
		case SOCKS4A_REPLY_CODE_REJECT_SOCKS_SERVER_CANNOT_CONNECT_TO_CLIENT:
			retval = status
					+ " Request rejected: SOCKS server cannot connect to identd on the client.";
			break;
		case SOCKS4A_REPLY_CODE_REJECT_PROGRAM_AND_IDENTD_MISMATCH_USER_IDS:
			retval = status
					+ " Request rejected: the client program and identd report different user-ids.";
			break;
		default:
			/* Caso di default */
			retval = status + " Unknown SOCKS status code.";
		}
		return retval;
	}
}
