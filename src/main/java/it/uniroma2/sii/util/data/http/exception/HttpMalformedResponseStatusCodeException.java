package it.uniroma2.sii.util.data.http.exception;

/**
 * Eccezione che viene lanciata se il codice di stato di una risposta HTTP è in
 * un formato non corretto.
 * 
 * @author Emanuele Altomare
 */
public class HttpMalformedResponseStatusCodeException extends HttpException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3327409380128250707L;

	public HttpMalformedResponseStatusCodeException(String malformedStatusCode) {
		super(malformedStatusCode);
	}

}
