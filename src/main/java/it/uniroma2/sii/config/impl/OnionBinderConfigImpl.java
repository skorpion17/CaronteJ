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
	@Value("${onion.binder.address.startfrom}")
	public String onionBinderAddressStartFrom;

	@Value("${onion.binder.address.start.netmask}")
	private String onionBinderAddressNetmask;

	@Value("${onion.binder.address.subnet}")
	private String onionBinderAddressSubnet;

	@Value("${onion.binder.address.increment}")
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
