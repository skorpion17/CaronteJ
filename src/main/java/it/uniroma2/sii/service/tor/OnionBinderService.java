package it.uniroma2.sii.service.tor;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.OnionBinderRepository;
import it.uniroma2.sii.util.address.AddressUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	 * @throws UnknownHostException
	 */
	public SocketAddress resolveCachedOnionNameByInternalInetSocketAddress(
			final InetSocketAddress socketAddress) throws UnknownHostException {
		/*
		 * Ottiene la notazione in intero (network-byte-order) dell'indirizzo
		 * ipv4 specificato in {@code socketAddress}
		 */
		final int resolvedInternalIp = AddressUtils
				.fromInetAddressToIntIPv4(socketAddress.getAddress());
		final OnionBinder onionBinder = onionBinderRepository
				.findOne(resolvedInternalIp);
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
}
