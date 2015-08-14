package it.uniroma2.sii.config;

public interface OnionBinderConfig {
	public static final int ADDRESS_IPV4_MASK = 0xffffffff;
	public static final int ONION_BINDER_ADDRESS_4_BYTE_START = 0x7f010001;
	public static final int ONION_BINDER_ADDRESS_4_BYTE_START_NETMASK = 0xffff0000;
	public static final int ONION_BINDER_ADDRESS_4_BYTE_SUBNET = 0x7f010000;
	public static final int ONION_BINDER_ADDRESS_INCREMENT = 0x01;
}
