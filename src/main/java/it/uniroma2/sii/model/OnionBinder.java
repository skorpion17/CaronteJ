package it.uniroma2.sii.model;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * OnionBinder permette di tenere accoppiato un indirizzp IPv4 ad un .onion per
 * rendere possibile la risoluzione interna dei .onion. Questo può risultare
 * molto utile qualora si voglia risolvere attraverso un indirizzo fittizio un
 * .onion e usare successivamente l'ip fittizio per riferirsi sempre allo stesso
 * .onion rendendo possibile gestire la risoluzione di un qualsiasi indirizzo
 * (sia un hidden service che un host canonico).
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
		final int ipAddress = (int) (getAddress());
		final byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
		return InetAddress.getByAddress(bytes);
	}
}
