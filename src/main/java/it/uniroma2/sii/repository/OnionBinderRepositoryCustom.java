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
package it.uniroma2.sii.repository;

import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.exception.NoMoreOnionBinderAddressAvailableException;

import java.io.IOException;

/**
 * Repository custom per {@link OnionBinder}.
 * 
 * @author Andrea Mayer
 *
 */
public interface OnionBinderRepositoryCustom {
	/**
	 * Permette di registrare una coppia <onion,indirizzo_ip_privato> in modo
	 * tale che la connect e le altre operazioni di open di glibc non vedano in
	 * alcun modo l'indirizzo .onion. La traduzione da indirizzo ip privato
	 * associato al particolare .onion verrà effettuata nel proxy in modo
	 * completamente trasparente per l'applicazione client.
	 * 
	 * @param onionName
	 * @return
	 * @throws NoMoreOnionBinderAddressAvailableException
	 * @throws IOException
	 */
	public OnionBinder registerOnionBinderByNameWithExpirationTime(
			final String onionName)
			throws NoMoreOnionBinderAddressAvailableException, IOException;

	/**
	 * Permette di ottenere un oggetto {@link OnionBinder} identificato da
	 * {@code ipv4} se presente nello strato di storage. Inoltre questo metodo
	 * una volta chiamato va ad aggiornare il contenuto della cache rimuovendo
	 * le coppie <address,onoin> scadute e facendo il refresh di quella a cui
	 * l'eventuale onion recuperato si riferisce.
	 * 
	 * @param ipv4
	 * @return
	 * @throws IOException
	 */
	public OnionBinder findOnionBindByAddressAndDoCacheRefreshing(final int ipv4)
			throws IOException;

	/**
	 * Verifica se {@link OnionBinder} {@code onion} esiste senza rinfrescare il
	 * suo expiration_time.
	 * 
	 * @param onion
	 * @return
	 */
	public boolean checkIfOnionBinderExistsWithoutCacheRefreshing(
			final String onion);
}
