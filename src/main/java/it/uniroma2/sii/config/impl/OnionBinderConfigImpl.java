package it.uniroma2.sii.config.impl;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.util.address.AddressUtils;

import org.springframework.beans.factory.annotation.Value;

/**
 * Configurazione per OnionBinderService.
 * 
 * @author andrea
 *
 */
public class OnionBinderConfigImpl implements OnionBinderConfig {
	@Value("${onion_binder_address_start_from}")
	public String onionBinderAddressStartFrom;

	@Value("${onion_binder_address_start_netmask}")
	private String onionBinderAddressNetmask;

	@Value("${onion_binder_address_subnet}")
	private String onionBinderAddressSubnet;

	@Value("${onion_binder_address_increment}")
	private String onionBinderAddressIncrement;

	@Override
	public int getAddressIPv4Mask() {
		return 0xffffffff;
	}

	@Override
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

	@Override
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

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnionBinderAddressSubnet()
	 */
	public int getOnionBinderAddressSubnet() {
		return AddressUtils.fromDotDecimalToIntIPv4(onionBinderAddressSubnet);
	}

	@Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.uniroma2.sii.config.OnionBinderConfig#getOnionBinderAddressIncrement()
	 */
	public int getOnionBinderAddressIncrement() {
		return Integer.parseInt(onionBinderAddressIncrement);
	}
}
