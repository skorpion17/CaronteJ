package it.uniroma2.sii.model;

import it.uniroma2.sii.config.OnionBinderConfig;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author andrea
 *
 */
@Entity
@Table(name = OnionBinder.ONION_BINDER_TABLE)
public class OnionBinder {
	public static final String ONION_BINDER_TABLE = "OnionBinder";
	public static final String ONION_BINDER_ADDRESS_FIELD_NAME = "address";

	@Id
	@Column(name = ONION_BINDER_ADDRESS_FIELD_NAME)
	private int address;
	private String onionName;

	// TODO: AGGIUNGERE CAMPO PER IL TIMESTAMP PER LA CREAZIONE DI UN
	// ONION_BINDER. IN QUESTO MODO SI PUÒ DECIDERE SE CACHARE PER UN CERTO
	// PERIODO DI TEMPO OPPURE NO.

	/**
	 * Costruttore.
	 */
	public OnionBinder() {
	}

	/**
	 * @return the onionName
	 */
	public String getOnionName() {
		return onionName;
	}

	/**
	 * @param onionName
	 *            the onionName to set
	 */
	public void setOnionName(String onionName) {
		this.onionName = onionName;
	}

	/**
	 * @return the address
	 */
	public long getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(int address) {
		this.address = address;
	}

	/**
	 * Ottiene la SocketAddress interna associata all'onion.
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public InetAddress getInetAddress() throws UnknownHostException {
		// final int ipv4 = (int) ((address & OnionBinderConfig.ADDRESS_MASK) >>
		// OnionBinderConfig.ADDRESS_6_BYTE_SHIFT_RIGHT_TO_IPV4);
		// final int port = (int) (address & OnionBinderConfig.PORT_MASK);
		// final byte[] bytes = BigInteger.valueOf(ipv4).toByteArray();
		// return new InetSocketAddress(InetAddress.getByAddress(bytes), port);
		final int ipAddress = (int) (getAddress() & OnionBinderConfig.ADDRESS_IPV4_MASK);
		final byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
		return InetAddress.getByAddress(bytes);
	}
}
