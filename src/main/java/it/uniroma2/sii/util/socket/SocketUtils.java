package it.uniroma2.sii.util.socket;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility per la gestione delle Socket.
 * 
 * @author Andrea Mayer
 *
 */
public class SocketUtils {

	private SocketUtils() {
	}

	/**
	 * Ottiene l'indirizzo di destinazione e la porta di destinazione dalla
	 * socket {@code socket} utilizzando un protocollo SOCKS semplificato.
	 * 
	 * La connessione che viene aperta è simile a SOCKS. Sulla socket appena
	 * aperta {@code socket} si ha:
	 * 
	 * 1) AF_FAM che indica la famiglia, IPV4 o IPV6
	 * 
	 * 2) IP_ADDR_NTB che indica l'indirizzo di destinazione in network byte
	 * order.
	 * 
	 * 3) PORT_NTB che indica la porta di destinazione in network byte order.
	 *
	 * AF_FAM sono 2 byte (sa_family_t). IP_ADDR_NTB dipende dalla AF_FAMILY e
	 * PORT_NTB sono 2 byte.
	 * 
	 * 
	 * +--------+-------------+----------+
	 * 
	 * | AF_FAM | IP_ADDR_NTB | PORT_NTB |
	 * 
	 * +--------+-------------+----------+
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	public static SocketAddress getDestSockAddress(final Socket socket)
			throws UnknownHostException, IOException {
		final DataInputStream inputStream = new DataInputStream(
				socket.getInputStream());
		/* Si leggono i tre parametri che vengono inviati. */
		@SuppressWarnings("unused")
		final short saFamily = inputStream.readShort();
		/*
		 * Per ora il parametro saFamily viene ingnorati, si utilizza per
		 * l'indirizzo sempre un indirizzo IPv4. TODO: Rendere generico ed
		 * evitare di cablare la saFamily su IPV4.
		 */
		final int ip = inputStream.readInt();
		final short port = inputStream.readShort();
		/*
		 * Ritorna l'indirizzo ip in un array di byte[] in network-byte-order.
		 */
		final byte[] ipNetworkByteOrder = BigInteger.valueOf(ip).toByteArray();
		return new InetSocketAddress(
				InetAddress.getByAddress(ipNetworkByteOrder), port);
	}

	/**
	 * Consente di estrapolare dallo stream l'header HTTP, creando una mappa
	 * facilmente consultabile.
	 * 
	 * @return
	 */
	public static Map<String, String> getHttpInfo(InputStream stream)
			throws Exception {
		Map<String, String> header = new LinkedHashMap<String, String>();

		/*
		 * creo il reader per poter leggere lo stream riga per riga.
		 */
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String line = "";

		while ((line = reader.readLine()) != null) {
			/*
			 * se la riga è vuota, vuol dire che l'header è finito ed esco.
			 */
			if (line.equals("")) {
				break;
			}

			/*
			 * riaggiungo l'end-line tolto in lettura da readLine()
			 */
			line += "\n";
			String[] couple;

			/*
			 * divido in due la linea dell'header in base al primo carattere di
			 * due punti che trovo.
			 */
			couple = line.split("\\:", 2);

			/*
			 * vuol dire che sono nella prima riga dove vengono specificati i
			 * metodi HTTP di richiesta o il codice di risposta.
			 */
			if (couple.length == 1) {
				/*
				 * TODO: verifico che sia una richiesta o una risposta HTTP
				 */
				//
				// if (!line
				// .matches("^GET(.*)|^HEAD(.*)|^POST(.*)|^PUT(.*)|^DELETE(.*)|^TRACE(.*)|^CONNECT(.*)|^OPTIONS(.*)|^HTTP\\/(.*)"))
				// {
				// throw new Exception("It isn't an HTTP payload.");
				// }
				//

				/*
				 * creo il record nella mappa con le informazioni riguardanti la
				 * richiesta o la risposta HTTP.
				 */
				header.put("Info", line.trim());
				continue;
			}

			if (couple.length != 2) {
				throw new Exception("Header HTTP malformed.");
			}

			header.put(couple[0].trim(), couple[1].trim());
		}

		return header;
	}
}
