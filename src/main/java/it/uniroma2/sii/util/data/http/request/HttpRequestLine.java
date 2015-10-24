package it.uniroma2.sii.util.data.http.request;

import it.uniroma2.sii.util.data.http.HttpStartLine;
import it.uniroma2.sii.util.data.http.HttpUtils;
import it.uniroma2.sii.util.data.http.exception.HttpException;
import it.uniroma2.sii.util.data.http.exception.HttpMethodNotSupportedException;
import it.uniroma2.sii.util.data.http.exception.HttpVersionNotSupportedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Rappresenta la request-line di una richiesta HTTP.
 * 
 * @author Emanuele Altomare
 */
public class HttpRequestLine extends HttpStartLine {

	String method = null;
	URI requestUri = null;

	public HttpRequestLine() {
		super();
	}

	public HttpRequestLine(byte[] requestLine) throws IOException {
		super(requestLine);
	}

	public HttpRequestLine(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	/**
	 * Consente di popolare il campo method a partire dalla start-line,
	 * effettuando i controlli necessari.
	 * 
	 * @throws HttpMethodNotSupportedException
	 */
	private void makeMethod() throws HttpException {

		method = HttpUtils.getSupportedRequestMethodByStartLine(startLine);

		if (method == null) {
			throw new HttpException(
					"this is not a valid request-line or method not supported.");
		}
	}

	/**
	 * Consente di popolare il campo requestUri.
	 * 
	 * @throws URISyntaxException
	 */
	private void makeRequestUri() throws URISyntaxException {

		String[] elements = startLine.split(" ");

		/*
		 * creo un nuovo oggetto URI e lo assegno.
		 */
		requestUri = new URI(elements[1].trim());
	}

	/**
	 * Consente di ottenere il metodo relativo alla specifica richiesta HTTP.
	 * 
	 * @return
	 * @throws HttpMethodNotSupportedException
	 */
	public String getMethod() throws HttpException {
		if (method == null) {
			makeMethod();
		}
		return method;
	}

	/**
	 * Consente di settare il metodo per la specifica richiesta HTTP.
	 * ATTENZIONE, non viene effettuato alcun controllo sulla correttezza del
	 * metodo!
	 * 
	 * @param method
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * Consente di ottenre l'URI relativo alla specifica richiesta HTTP.
	 * 
	 * @return
	 * @throws URISyntaxException
	 */
	public URI getRequestUri() throws URISyntaxException {
		if (requestUri == null) {
			makeRequestUri();
		}
		return requestUri;
	}

	@Override
	protected void makeHttpVersion() throws HttpException {
		String supportedHttpVersion = HttpUtils
				.getSupportedHttpVersionByStartLine(startLine,
						HttpUtils.HttpPacketType.REQUEST);
		if (supportedHttpVersion == null) {
			throw new HttpVersionNotSupportedException(supportedHttpVersion);
		}
		httpVersion = supportedHttpVersion;
	}
}
