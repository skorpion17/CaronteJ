/**
 * 
 */
package it.uniroma2.sii.util.data.http.exception;

import it.uniroma2.sii.util.data.http.HttpData;

import java.io.IOException;

/**
 * Rappresenta le possibili eccezioni che si possono verificare nel costruire od
 * usare oggetti {@link HttpData}.
 * 
 * @author Emanuele Altomare
 */
public class HttpException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2945418280764711062L;

	/**
	 * Costruisce l'eccezione con un messaggio dato in input.
	 * 
	 * @param message
	 */
	public HttpException(String message) {
		super(message);
	}
}
