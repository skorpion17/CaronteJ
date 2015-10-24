package it.uniroma2.sii.util.data.http.exception;

/**
 * Viene lanciata quando non è presente alcuna reason-phrase valida.
 * 
 * @author Emanuele Altomare
 */
public class HttpNoReasonPhraseException extends HttpException {

	public HttpNoReasonPhraseException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3811748182533646809L;

}
