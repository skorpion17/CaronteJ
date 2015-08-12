package it.uniroma2.sii.tor.dns;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Contiene il server DNS semplificato per rispondere alle richieste di tipo A
 * attraverso TOR.
 * 
 * @author Emanuele Altomare
 */
public class TorDns extends Thread {

	/*
	 * indirizzo al quale bindarsi.
	 */
	private final String bindIp;

	/*
	 * porta alla quale bindarsi.
	 */
	private final int bindPort;

	/*
	 * indica la massima dimensione del buffer che ospiterà il pacchetto di
	 * richiesta. (la massima dimensione di un messaggio UDP per il protocollo
	 * DNS)
	 */
	private final int BUFF_DIM = 512;

	/**
	 * Consente di creare il server passandogli l'indirizzo e la porta dove
	 * dovrà mettersi in ascolto.
	 * 
	 * @param ip
	 * @param port
	 */
	public TorDns(String bindIp, int bindPort) {
		this.bindIp = bindIp;
		this.bindPort = bindPort;
	}

	public void run() {
		/*
		 * creo l'oggetto che conterrà l'indirizzo ip sul quale il server dovrà
		 * mettersi in ascolto.
		 */
		InetAddress addr = null;

		try {
			addr = InetAddress.getByName(bindIp);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
			return;
		}

		/*
		 * avvio il server.
		 */
		DatagramSocket server = null;

		try {
			server = new DatagramSocket(bindPort, addr);
		} catch (SocketException e1) {
			e1.printStackTrace();
			return;
		}

		while (true) {
			try {

				/*
				 * creo il buffer che dovrà ospitare il pacchetto di richiesta
				 * DNS.
				 */
				byte[] buff = new byte[BUFF_DIM];

				/*
				 * creo il pacchetto.
				 */
				DatagramPacket p = new DatagramPacket(buff, buff.length);

				/*
				 * attendo di ricevere la richiesta e la metto nel pacchetto
				 * creato sopra.
				 */
				server.receive(p);

				/*
				 * creo un nuovo thread che dovrà gestire la richiesta appena
				 * arrivata.
				 */
				DnsRequestHandler handler = new DnsRequestHandler(p, server);

				/*
				 * faccio partire il thread.
				 */
				handler.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
