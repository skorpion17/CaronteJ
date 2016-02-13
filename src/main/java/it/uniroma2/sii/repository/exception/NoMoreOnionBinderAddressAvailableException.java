package it.uniroma2.sii.repository.exception;

import java.io.IOException;

/**
 * Questa eccezione notifica che sono terminati gli indirizzi usati per
 * costruire la tabella di mapping tra .onion e indirizzi privati.
 * 
 * @author Andrea Mayer
 *
 */
public class NoMoreOnionBinderAddressAvailableException extends IOException {
	private static final long serialVersionUID = 8857020505939584179L;

	public NoMoreOnionBinderAddressAvailableException() {
	}

	public NoMoreOnionBinderAddressAvailableException(String message) {
		super(message);
	}

	public NoMoreOnionBinderAddressAvailableException(Throwable cause) {
		super(cause);
	}

	public NoMoreOnionBinderAddressAvailableException(String message,
			Throwable cause) {
		super(message, cause);
	}
}
