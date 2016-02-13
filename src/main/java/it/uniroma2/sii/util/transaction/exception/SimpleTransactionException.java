package it.uniroma2.sii.util.transaction.exception;

import java.io.IOException;

/**
 * Eccezione sulla transazione. Questa eccezione è checked.
 * 
 * @author Andrea Mayer Mayer
 *
 */
public class SimpleTransactionException extends IOException {
	private static final long serialVersionUID = -5428425460234419495L;

	public SimpleTransactionException() {
	}

	public SimpleTransactionException(String message) {
		super(message);
	}

	public SimpleTransactionException(Throwable cause) {
		super(cause);
	}

	public SimpleTransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
