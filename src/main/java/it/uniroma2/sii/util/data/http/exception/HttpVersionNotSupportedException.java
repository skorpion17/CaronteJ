/**
 * 
 */
package it.uniroma2.sii.util.data.http.exception;

/**
 * Viene lanciata se la versione di HTTP non è supportata.
 * 
 * @author Emanuele Altomare
 */
public class HttpVersionNotSupportedException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1800099737776984008L;

	public HttpVersionNotSupportedException(String notSupportedHttpVersion) {
		super(notSupportedHttpVersion);
	}
}
