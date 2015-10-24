package it.uniroma2.sii.util.data.http.exception;

/**
 * Eccezione che viene lanciata se l'header HTTP ricevuto è malformato.
 * 
 * @author Emanuele Altomare
 */
public class HttpMalformedHeaderException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 797342346917488773L;

	public HttpMalformedHeaderException(String message) {
		super(message);
	}
}
