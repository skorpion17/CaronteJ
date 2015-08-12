package it.uniroma2.sii.service.tor.dns.server;

import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.OnionBinderRepository;
import it.uniroma2.sii.service.tor.dns.TorResolverService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
 * @author andrea
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

	@Value("${dns_tor_bind_address}")
	private String dnsTorBindAddress;

	@Value("${dns_tor_bind_port}")
	private int dnsTorBindPort;

	private DatagramSocket datagramSocket;

	/** Non puo essere avviato per piu di una volta il server */
	private Object lock = new Object();
	private boolean started = false;

	/**
	 * Gestore delle richieste di risoluzione dei nomi inoltrate al server.
	 * 
	 * @author andrea
	 *
	 */
	private class TorDNSRequestHandler extends Thread {
		private static final String SUFFIX_REGEX_ONION = ".+\\.onion\\.$";
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
		 * Genera il pacchetto contenete la risposta alla richiesta DNS.
		 * 
		 * @param dnsRequestPacket
		 * @return
		 * @throws IOException
		 */
		private DatagramPacket createDNSRensponsePacket(
				final DatagramPacket dnsRequestPacket) throws IOException {
			/* Crea i messaggio a partire dal pacchetto di richiesta */
			final Message message = new Message(dnsRequestPacket.getData());
			final Record question = message.getQuestion();
			/* Si prende l'hostname dal record */
			final Name hostname = question.getName();
			final String host = hostname.toString();

			InetAddress inetAddress;
			/* Si verifica che l'hostname non sia un .onion */
			if (host.matches(SUFFIX_REGEX_ONION)) {
				/*
				 * Se l'hostname è un .onion allora deve essere utilizzato un
				 * meccanismo di risoluzione interna che associa questo onion ad
				 * un indirizzo privato; successivamente quando verrà richieta
				 * la connessinoe http(s) si andrà alla ricerca della
				 * risoluzione interna.
				 */
				final OnionBinder onionBinder = onionBinderRepository
						.registerOnionByName(host);
				/* Ottiene l'indirizzo locale utilizzato per risolvere il .onion */
				inetAddress = onionBinder.getInetAddress();
			} else {
				/* E' un hostname canonico, lo risolvo attraverso TOR. */
				inetAddress = torResolverService.resolveDNS(host);
			}

			/* Creo il record di risposta */
			final ARecord dnsAnswer = new ARecord(hostname, 1, 86400,
					inetAddress);
			message.addRecord(dnsAnswer, Section.ANSWER);

			System.out
					.printf("\t >>> TorDNSRequestHandler replied to Client with: <<<\n",
							inetAddress);
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
				final DatagramPacket dnsResponsePacket = createDNSRensponsePacket(dnsRequestPacket);
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
