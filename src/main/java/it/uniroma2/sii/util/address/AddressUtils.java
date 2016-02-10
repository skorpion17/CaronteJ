package it.uniroma2.sii.util.address;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Utility per la gestione degli indirizzi.
 * 
 * @author andrea
 *
 */
public class AddressUtils {
	public static final int INADDR4SZ = 0x04;

	private AddressUtils() {

	}

	/**
	 * Ottiene un array di byte in formato network-byte-order a partire dalla
	 * rappresentazione in formato stringa (puntato) di un indirizzo IPv4
	 * 
	 * @param src
	 * @return
	 */
	public static byte[] textToNumericFormatV4(String src) {
		if (src.length() == 0) {
			return null;
		}

		byte[] res = new byte[INADDR4SZ];
		String[] s = src.split("\\.", -1);
		long val;
		try {
			switch (s.length) {
			case 1:
				/*
				 * When only one part is given, the value is stored directly in
				 * the network address without any byte rearrangement.
				 */

				val = Long.parseLong(s[0]);
				if (val < 0 || val > 0xffffffffL)
					return null;
				res[0] = (byte) ((val >> 24) & 0xff);
				res[1] = (byte) (((val & 0xffffff) >> 16) & 0xff);
				res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 2:
				/*
				 * When a two part address is supplied, the last part is
				 * interpreted as a 24-bit quantity and placed in the right most
				 * three bytes of the network address. This makes the two part
				 * address format convenient for specifying Class A network
				 * addresses as net.host.
				 */

				val = Integer.parseInt(s[0]);
				if (val < 0 || val > 0xff)
					return null;
				res[0] = (byte) (val & 0xff);
				val = Integer.parseInt(s[1]);
				if (val < 0 || val > 0xffffff)
					return null;
				res[1] = (byte) ((val >> 16) & 0xff);
				res[2] = (byte) (((val & 0xffff) >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 3:
				/*
				 * When a three part address is specified, the last part is
				 * interpreted as a 16-bit quantity and placed in the right most
				 * two bytes of the network address. This makes the three part
				 * address format convenient for specifying Class B net- work
				 * addresses as 128.net.host.
				 */
				for (int i = 0; i < 2; i++) {
					val = Integer.parseInt(s[i]);
					if (val < 0 || val > 0xff)
						return null;
					res[i] = (byte) (val & 0xff);
				}
				val = Integer.parseInt(s[2]);
				if (val < 0 || val > 0xffff)
					return null;
				res[2] = (byte) ((val >> 8) & 0xff);
				res[3] = (byte) (val & 0xff);
				break;
			case 4:
				/*
				 * When four parts are specified, each is interpreted as a byte
				 * of data and assigned, from left to right, to the four bytes
				 * of an IPv4 address.
				 */
				for (int i = 0; i < 4; i++) {
					val = Integer.parseInt(s[i]);
					if (val < 0 || val > 0xff)
						return null;
					res[i] = (byte) (val & 0xff);
				}
				break;
			default:
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return res;
	}

	/**
	 * Ottiene l'IPv4 in formato intero a partire da {@code address}.
	 * 
	 * @param address
	 * @return
	 */
	public static int fromInetAddressToIntIPv4(final InetAddress address) {
		return ByteBuffer.wrap(address.getAddress()).getInt();
	}

	/**
	 * Permette di ottenere un {@link InetAddress} a partire da {@link Integer}
	 * {@code ipv4}.
	 * 
	 * @param ipv4
	 * @return
	 * @throws UnknownHostException
	 */
	public static InetAddress fromIntIPv4ToInetaddress(final int ipv4)
			throws UnknownHostException {
		byte[] bytes = BigInteger.valueOf(ipv4).toByteArray();
		return InetAddress.getByAddress(bytes);
	}

	/**
	 * Dalla notazione Dot-Decial 0-255.0-255.0-255.0-255 ad intero.
	 * 
	 * @param dotDecimalIp
	 * @return
	 */
	public static int fromDotDecimalToIntIPv4(final String dotDecimalIp) {
		return ByteBuffer.wrap(textToNumericFormatV4(dotDecimalIp)).getInt();
	}
}
