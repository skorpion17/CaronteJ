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
package it.uniroma2.sii.service.tor;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.OnionBinderRepository;
import it.uniroma2.sii.util.address.AddressUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servizio che fornisce l'accesso per la risoluzione degli .onion e per la
 * registrazione di questi attraverso l'utilizzo dei {@link OnionBinder}.
 * 
 * @author Andrea Mayer
 *
 */
@Service
public class OnionBinderService {
	@Autowired
	private OnionBinderRepository onionBinderRepository;
	@Autowired
	private OnionBinderConfig onionBinderConfig;

	/**
	 * Ottiene la socketAddress relativa ad un .onion risolto internamente
	 * (tenuto in cache). La risoluzione è local_ipaddress <=> .onion.
	 * 
	 * @param socketAddress
	 * @return
	 * @throws IOException
	 */
	public SocketAddress resolveCachedOnionNameByInternalInetSocketAddress(
			final InetSocketAddress socketAddress) throws IOException {
		/*
		 * Ottiene la notazione in intero (network-byte-order) dell'indirizzo
		 * ipv4 specificato in {@code socketAddress}
		 */
		final int resolvedInternalIp = AddressUtils
				.fromInetAddressToIntIPv4(socketAddress.getAddress());
		final OnionBinder onionBinder = onionBinderRepository
				.findOnionBindByAddressAndDoCacheRefreshing(resolvedInternalIp);
		if (onionBinder == null) {
			/* Ritorna errore */
			throw new UnknownHostException(String.format(
					"No hidden service name is bound to: %s",
					socketAddress.getAddress()));
		}
		/*
		 * E' stato trovato, pertanto ritorna la InetSocketAddress non risolta
		 * con l'onion.
		 */
		return InetSocketAddress.createUnresolved(onionBinder.getOnionName(),
				socketAddress.getPort());
	}

	/**
	 * Verifica se l'indirizzo {@code address} è un indirizzo utilizzato per la
	 * risoluzione interna degli .onion.
	 * 
	 * @param address
	 * @return
	 */
	public boolean isInetAddressForInternalOnionResolution(
			final InetAddress address) {
		final int ipV4 = AddressUtils.fromInetAddressToIntIPv4(address);
		final int result = (onionBinderConfig.getOnionBinderAddressNetmask() & onionBinderConfig
				.getOnionBinderAddressSubnet())
				^ (ipV4 & onionBinderConfig.getOnionBinderAddressNetmask());
		if (result != 0x0) {
			/* Non appartire alla sottorete */
			return false;
		}
		return true;
	}

	/**
	 * Permette di registrare un {@link OnionBinder} attraverso il
	 * {@code onionName} all'interno dello strato di storage. Una volta
	 * registrato a questo .onion viene associato un ip locale privato che verrà
	 * utilizzato dalle applicazioni per accedere in modo trasparente al hidden
	 * server. La traduzione tra ip privato locale e hidden service viene
	 * attuata dal proxy.
	 * 
	 * @param onionName
	 * @return
	 * @throws IOException
	 */
	public OnionBinder registerOnionByName(final String onionName)
			throws IOException {
		return onionBinderRepository
				.registerOnionBinderByNameWithExpirationTime(onionName);
	}
}
