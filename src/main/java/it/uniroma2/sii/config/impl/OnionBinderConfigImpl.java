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
package it.uniroma2.sii.config.impl;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.util.address.AddressUtils;

import org.springframework.beans.factory.annotation.Value;

/**
 * Configurazione per OnionBinderService.
 * 
 * @author Andrea Mayer
 *
 */
public class OnionBinderConfigImpl implements OnionBinderConfig {
	@Value("${onion.binder.address.startfrom}")
	public String onionBinderAddressStartFrom;

	@Value("${onion.binder.address.start.netmask}")
	private String onionBinderAddressNetmask;

	@Value("${onion.binder.address.subnet}")
	private String onionBinderAddressSubnet;

	@Value("${onion.binder.address.increment}")
	private String onionBinderAddressIncrement;

	@Value("${onion.binder.expirationtimemillisec}")
	private int expirationTimeInMillis;

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniroma2.sii.config.OnionBinderConfig#getAddressIPv4Mask()
	 */
	@Override
	public int getAddressIPv4Mask() {
		return 0xffffffff;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnionBinderAddressStartFrom()
	 */
	public int getOnionBinderAddressStartFrom() {
		return AddressUtils
				.fromDotDecimalToIntIPv4(onionBinderAddressStartFrom);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnionBinderAddressNetmask()
	 */
	public int getOnionBinderAddressNetmask() {
		return (-1 << ((AddressUtils.INADDR4SZ * 8) - Integer
				.parseInt(onionBinderAddressNetmask)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnoinBinderNoOfBitNetmask()
	 */
	@Override
	public int getOnionBinderNumberBitOfNetmask() {
		return Integer.parseInt(onionBinderAddressNetmask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnionBinderAddressSubnet()
	 */
	public int getOnionBinderAddressSubnet() {
		return AddressUtils.fromDotDecimalToIntIPv4(onionBinderAddressSubnet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnionBinderAddressIncrement()
	 */
	public int getOnionBinderAddressIncrement() {
		return Integer.parseInt(onionBinderAddressIncrement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getRelativeExpirationTimeInMillis
	 * ()
	 */
	@Override
	public int getOnionBinderRelativeExpirationTimeInMillis() {
		return expirationTimeInMillis;
	}
}
