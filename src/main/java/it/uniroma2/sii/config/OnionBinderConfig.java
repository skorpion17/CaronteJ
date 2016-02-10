package it.uniroma2.sii.config;

import it.uniroma2.sii.service.tor.OnionBinderService;

/**
 * Configurazione per {@link OnionBinderService}
 * 
 * @author andrea
 *
 */
public interface OnionBinderConfig {
	/**
	 * Maschera per indirizzo IPv4.
	 * 
	 * @deprecated
	 * @return
	 */
	public int getAddressIPv4Mask();

	/**
	 * Ottiene l'indirizzo di partenza utilizzato per la risoluzione interna dei
	 * .onion.
	 * 
	 * @return
	 */
	public int getOnionBinderAddressStartFrom();

	/**
	 * Ottiene la maschera di rete per la famiglia di indirizzi utilizzati per
	 * la risoluzione interna dei .onion.
	 * 
	 * @return
	 */
	public int getOnionBinderAddressNetmask();

	/**
	 * Ottiene la sottorete per la famiglia di indirizzi utilizzati per la
	 * risoluzione interna dei .onion.
	 * 
	 * @return
	 */
	public int getOnionBinderAddressSubnet();

	/**
	 * Ottiene l'incremento utilizzato per generare il prossimo indirizzo
	 * interno utilzzato per la risoluzione interna dei .onion.
	 * 
	 * @return
	 */
	public int getOnionBinderAddressIncrement();

	/**
	 * Ottiene il numero di bit della netmask.
	 * 
	 * @return
	 */
	public int getOnionBinderNumberBitOfNetmask();

	/**
	 * Ottiene il tempo di scadenza relativa di un OnionBinder.
	 * 
	 * @return
	 */
	public int getOnionBinderRelativeExpirationTimeInMillis();
}
