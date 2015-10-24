package it.uniroma2.sii.util.data.http.exception;

/**
 * Viene lanciata quando il metodo della richiesta HTTP non è supportato.
 * 
 * @author Emanuele Altomare
 */
public class HttpMethodNotSupportedException extends HttpException {

	public HttpMethodNotSupportedException(String methodNotSupported) {
		super(methodNotSupported);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9083901627734254251L;

}
