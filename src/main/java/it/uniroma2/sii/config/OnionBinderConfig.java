package it.uniroma2.sii.config;

public interface OnionBinderConfig {
	/**
	 * <Ip,port> iniziale per l'OnionBinder: 127.1.0.1:0000
	 */
	// public static long ONION_BINDER_ADDRESS_6_BYTE_START = 0x7f0100010000L;
	// public static final long PORT_MASK = 0x00000000ffffL;
	// public static final long ADDRESS_MASK = 0xffffffff0000L;
	// public static int ONION_BINDER_ADDRESS_PORT_6_BYTE_START = 0x3ff;
	// public static final int ADDRESS_6_BYTE_SHIFT_RIGHT_TO_IPV4 = 0x10;

	/** Maschera per Indirizzo IPV4 */
	public static final long ADDRESS_IPV4_MASK = 0xffffffffL;
	public static final long ONION_BINDER_ADDRESS_4_BYTE_START = 0x7f010001L;
	public static final int ONION_BINDER_ADDRESS_INCREMENT = 0x01;
}
