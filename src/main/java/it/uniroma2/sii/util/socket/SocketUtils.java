package it.uniroma2.sii.util.socket;

import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * Utility per la gestione delle Socket.
 * 
 * @author andrea
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
}
