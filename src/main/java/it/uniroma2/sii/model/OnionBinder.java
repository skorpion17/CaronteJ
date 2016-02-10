package it.uniroma2.sii.model;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
	private static final int IPV4_INT_MASK = 0xffffffff;
	public static final String ONION_BINDER_TABLE = "OnionBinder";
	public static final String ONION_BINDER_ADDRESS_FIELD_NAME = "address";

	@Id
	@Column(name = ONION_BINDER_ADDRESS_FIELD_NAME)
	private int address;
	private String onionName;
	/**
	 * Timestamp per gestire la scandenza delle risoluzioni tra
	 * ip_privato_loopback <=> .onion
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date expirationTimestamp;

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
	public int getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(final int address) {
		this.address = IPV4_INT_MASK & address;
	}

	/**
	 * Aggiorna il valore di {@link OnionBinder#expirationTimestamp} con il
	 * valore del tempo attuale + il tempo di expiration.
	 */
	public void updateExpirationTimestamp(final int relativeTimeInMillis) {
		setExpirationTimestamp(new Date(System.currentTimeMillis()
				+ relativeTimeInMillis));
	}

	/**
	 * Ottiene la SocketAddress interna associata all'onion.
	 * 
	 * @return
	 * @throws UnknownHostException
	 */
	public InetAddress getInetAddress() throws UnknownHostException {
		final int ipAddress = (int) (IPV4_INT_MASK & (getAddress()));
		final byte[] bytes = BigInteger.valueOf(ipAddress).toByteArray();
		return InetAddress.getByAddress(bytes);
	}

	/**
	 * Ottiene il tempo di scadenza del {@link OnionBinder} corrente.
	 * 
	 * @return
	 */
	public Date getExpirationTimestamp() {
		return expirationTimestamp;
	}

	/**
	 * Imposta il tempo di scadenza per {@link OnionBinder} corrente.
	 * 
	 * @param expirationTimestamp
	 */
	public void setExpirationTimestamp(Date expirationTimestamp) {
		this.expirationTimestamp = expirationTimestamp;
	}
}
