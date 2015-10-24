/**
 * 
 */
package it.uniroma2.sii.util.data.http.exception;

/**
 * Viene lanciata quando la start-line risulta essere nulla.
 * 
 * @author Emanuele Altomare
 */
public class HttpNullStartLineException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8772534051408499153L;

	public HttpNullStartLineException(String message) {
		super(message);
	}

}
