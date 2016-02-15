/*******************************************************************************
 * Copyright (c) 2015, 2016 Emanuele Altomare, Andrea Mayer
 *
 * This file is part of Proxy2Tor.
 * Proxy2Tor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * Proxy2Tor is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proxy2Tor.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package it.uniroma2.sii.service.tor.dns.server;

import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.OnionBinderRepository;
import it.uniroma2.sii.service.tor.dns.TorResolverService;
import it.uniroma2.sii.sock.SOCKSSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;

/**
 * Servizio che realizza il server TorDNSServer per la risoluzione su tor di
 * hostname in indirizzi ip. Tiene in considerazione anche della risoluzione
 * interna per la risoluzione di eventuali .onion.
 * 
 * @author Andrea Mayer Mayer, Emanuele Altomare
 *
 */
@Service
public class TorDNSServer extends Thread {
	/** Dimensione del buffer interno per un DatagramPacket */
	private static final int INTERNAL_BUFFER_LENGTH = 1024;

	@Autowired
	private OnionBinderRepository onionBinderRepository;
	@Autowired
	private TorResolverService torResolverService;

	@Value("${dns.tor.bind.address}")
	private String dnsTorBindAddress;

	@Value("${dns.tor.bind.port}")
	private int dnsTorBindPort;

	/**
	 * Indirizzo su cui il proxy di TOR è in ascolto
	 */
	@Value("${proxy.tor.bind.address}")
	private String proxyTorBindAddress;
	/**
	 * Porta su cui il proxy di TOR è in ascolto
	 */
	@Value("${proxy.tor.bind.port}")
	private int proxyTorBindPort;
	/**
	 * Per contattare gli onioni si prova sulle porte dei servizi HTTP
	 */
	@Value("${http.service.ports}")
	private int[] onionHttpServicePort;
	/**
	 * Per contattare gli onioni si prova sulle porte dei servizi HTTPS
	 */
	@Value("${https.service.ports}")
	private int[] onionHttpsServicePort;

	@Value("${dns.tor.onion.resolution.timeout}")
	private int onionConnectTimeoutInMillis;

	private DatagramSocket datagramSocket;

	/** Non puo essere avviato per piu di una volta il server */
	private Object lock = new Object();
	private boolean started = false;

	/**
	 * Permette di effettuare l'unione di due array.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static int[] combineIntegerArrays(final int[] a, final int[] b) {
		final int length = a.length + b.length;
		final int[] result = new int[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	/**
	 * Gestore delle richieste di risoluzione dei nomi inoltrate al server.
	 * 
	 * @author Andrea Mayer
	 *
	 */
	private class TorDNSRequestHandler extends Thread {
		private static final String SUFFIX_REGEX_ONION = ".+\\.onion$";
		private DatagramPacket dnsRequestPacket;

		/**
		 * Costruttore.
		 * 
		 * @param dnsRequestPacket
		 * @throws SocketException
		 */
		public TorDNSRequestHandler(final DatagramPacket dnsRequestPacket)
				throws SocketException {
			this.dnsRequestPacket = dnsRequestPacket;
		}

		/**
		 * Ottiene l'indirizzo presso cui il server di tor è in ascoto.
		 * 
		 * @return
		 */
		private InetSocketAddress getTorSocketAddress() {
			return InetSocketAddress.createUnresolved(proxyTorBindAddress,
					proxyTorBindPort);
		}

		/**
		 * Prova a connettersi all'onion per verificare se esiste e se risulta
		 * raggiungibile.
		 * 
		 * @param onion
		 * @throws IOException
		 */
		private void checkIfOnionExistsAndIsReachable(final String onion)
				throws IOException {
			if (onionBinderRepository
					.checkIfOnionBinderExistsWithoutCacheRefreshing(onion)) {
				/*
				 * L'onion è stato raggiunto almeno una volta nell'ultimo
				 * nell'ultimo intervallo di tempo prima della scadenza del suo
				 * expiration_time; per questo motivo è un .onion ritenuto
				 * raggiungibile e pertanto non si effettua nessun check sulla
				 * sua raggiungibilità.
				 */
				return;
			}
			boolean isReachable = false;
			final int[] checkPorts = combineIntegerArrays(onionHttpServicePort,
					onionHttpsServicePort);
			for (int i = 0; i < checkPorts.length; ++i) {
				/*
				 * Per ogni porta su cui può essere in ascolto un servizio
				 * onion.
				 */
				final Socket socket = new SOCKSSocket(getTorSocketAddress());
				try {
					socket.setSoTimeout(onionConnectTimeoutInMillis);
					InetSocketAddress inetSockAddress = InetSocketAddress
							.createUnresolved(onion, checkPorts[i]);
					socket.connect(inetSockAddress);
					/*
					 * Se sono arrivato fin qui, la connessione con l'onion è
					 * andata a buon fine, posso terminare e ritornare la
					 * disponibilità.
					 */
					isReachable = true;
					break;
				} catch (final IOException ioe) {
					/* Non devo stampare nulla per ora. */
				} finally {
					socket.close();
				}
			}
			if (!isReachable) {
				/*
				 * Se non è mai stato possibile raggiungere il .onion lancio
				 * l'eccezione.
				 */
				throw new SocketException(
						String.format(
								"%s is not reachable at this moment, please try later.",
								onion));
			}
		}

		/**
		 * Genera il pacchetto contenete la risposta alla richiesta DNS.
		 * 
		 * @param dnsRequestPacket
		 * @return
		 * @throws IOException
		 */
		private DatagramPacket createDNSResponsePacket(
				final DatagramPacket dnsRequestPacket) throws IOException {
			/* Crea i messaggio a partire dal pacchetto di richiesta */
			final Message message = new Message(dnsRequestPacket.getData());
			final Record question = message.getQuestion();
			/* Si prende l'hostname dal record */
			final Name hostname = question.getName();
			final String host = hostname.toString(true);

			boolean createARecord = true;
			InetAddress inetAddress = null;
			/* Si verifica che l'hostname non sia un .onion */
			if (host.matches(SUFFIX_REGEX_ONION)) {
				try {
					/*
					 * Si verifica che l'onion a cui ci si vuole connettere sia
					 * valido ed accessibile.
					 */
					checkIfOnionExistsAndIsReachable(host);
					/*
					 * l'hostname è un .onion allora deve essere utilizzato un
					 * meccanismo di risoluzione interna che associa questo
					 * onion ad un indirizzo privato; successivamente quando
					 * verrà richieta la connessinoe http(s) si andrà alla
					 * ricerca della risoluzione interna.
					 */
					final OnionBinder onionBinder = onionBinderRepository
							.registerOnionBinderByNameWithExpirationTime(host);
					/*
					 * Ottiene l'indirizzo locale utilizzato per risolvere il
					 * .onion
					 */
					inetAddress = onionBinder.getInetAddress();
				} catch (final IOException ioe) {
					/*
					 * Si stampa il messaggio a video, questo non è un errore
					 * grave. Stampando a video lato server si riesce a capire
					 * cosa sta succedendo. I fallimenti potrebbero essere
					 * causati dalla rete lenta o servizi non disponibili.
					 */
					createARecord = false;
				}
			} else {
				/* E' un hostname canonico, lo risolvo attraverso TOR. */
				inetAddress = torResolverService.resolveDNS(host);
			}

			if (createARecord) {
				/*
				 * Creo il record di risposta se l'onion esiste e raggiungibile,
				 * altrimenti rispondo con un record vuoto.
				 */
				final ARecord dnsAnswer = new ARecord(hostname, 1, 86400,
						inetAddress);
				message.addRecord(dnsAnswer, Section.ANSWER);
				System.out
						.printf("\t >>> TorDNSRequestHandler replied to Client with: %s <<<\n",
								inetAddress.getHostAddress());
			} else {
				System.out.printf("\t >>> TorDNS cannot resolve %s <<< \n",
						host);
			}
			/*
			 * Creo il datagramma di risposta
			 */
			return new DatagramPacket(message.toWire(),
					message.toWire().length, dnsRequestPacket.getAddress(),
					dnsRequestPacket.getPort());
		}

		@Override
		public void run() {
			try {
				/* Gestisce la traduzione del nome */
				final DatagramPacket dnsResponsePacket = createDNSResponsePacket(dnsRequestPacket);
				System.out
						.println("\t >>> TorDNSRequestHandler managed DNS Request and replied to the client <<<");
				/*
				 * Risponde al client con l'indirizzo risolto.
				 */
				datagramSocket.send(dnsResponsePacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Ottengo l'indirizzo su cui il TorDNSServer è in ascolto.
	 * 
	 * @return
	 */
	public String getDnsTorBindAddress() {
		return dnsTorBindAddress;
	}

	/**
	 * Ottengo la porta su cui il TorDNSServer è in ascolto.
	 * 
	 * @return
	 */
	public int getDnsTorBindPort() {
		return dnsTorBindPort;
	}

	@Override
	public void run() {
		try {
			/* Crea la datagramSocket appena il server viene avviato. */
			datagramSocket = new DatagramSocket(getDnsTorBindPort(),
					InetAddress.getByName(getDnsTorBindAddress()));
			while (true) {
				/*
				 * Finchè il server è attivo gestisce l'arrivo di richeste di
				 * traduzione dei nomi in modo concorrente.
				 */
				try {
					final byte[] buffer = new byte[INTERNAL_BUFFER_LENGTH];
					final DatagramPacket dnsRequestPacket = new DatagramPacket(
							buffer, buffer.length);
					/*
					 * Si mette in attesa di ricezione della richiesta DNS del
					 * client (intercettata)
					 */
					System.out
							.println("\t >>> TorDNSServer is waiting for a DNS request <<<");
					datagramSocket.receive(dnsRequestPacket);
					/*
					 * Crea l'handler e avvia il thread per la sua gestione
					 */
					final TorDNSRequestHandler torDNSRequestHandler = new TorDNSRequestHandler(
							dnsRequestPacket);
					torDNSRequestHandler.start();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		synchronized (lock) {
			if (!started) {
				/* Non è mai stato avviato il server, lo si avvia. */
				started = true;
				super.start();
				/* Debug */
				System.out.println("\t >>> TorDNSServer is started <<<");
			} else {
				/* Rilancia eccezione a runtime. */
				throw new IllegalStateException(String.format(
						"%s Server is already started.", getClass().getName()));
			}
		}
	}
}
