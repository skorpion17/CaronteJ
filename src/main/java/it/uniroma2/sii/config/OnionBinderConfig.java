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
package it.uniroma2.sii.config;

import it.uniroma2.sii.service.tor.OnionBinderService;

/**
 * Configurazione per {@link OnionBinderService}
 * 
 * @author Andrea Mayer
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
