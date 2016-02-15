/*******************************************************************************
 * Copyright (c) 2015, 2016 Emanuele Altomare, Andrea Mayer
 *
 * This file is part of Proxy2Tor.
 * Proxy2Tor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * Proxy2Tor is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proxy2Tor.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package it.uniroma2.sii.util.data.http;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.exception.HttpException;
import it.uniroma2.sii.util.data.http.exception.HttpMalformedResponseStatusCodeException;
import it.uniroma2.sii.util.data.http.exception.HttpNullStartLineException;
import it.uniroma2.sii.util.data.http.exception.HttpVersionNotSupportedException;
import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.request.HttpRequestLine;
import it.uniroma2.sii.util.data.http.response.HttpResponse;
import it.uniroma2.sii.util.data.http.response.HttpResponseStatusLine;

import java.io.IOException;
import java.io.InputStream;

/**
 * Ha il compito di creare gli oggetti HttpData.
 * 
 * @author Emanuele Altomare
 */
public class HttpDataFactory {

	/**
	 * Costruttore di default.
	 */
	public HttpDataFactory() {
	}

	/**
	 * Questo metodo si occupa di creare un oggetto {@link HttpData}.
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static HttpData createHttpData(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {

		if (proxyConnectionHandler == null || inputStream == null) {
			throw new IOException(
					"FATAL ERROR: connection handler or stream are null!");
		}

		byte[] startLineBytes;
		HttpData httpData = null;

		/*
		 * leggo la prima linea, la start-line.
		 */
		startLineBytes = HttpUtils.readLineBytesFromInputStream(inputStream);

		/*
		 * provo a vedere se è una richiesta HTTP.
		 */
		try {

			httpData = createHttpRequest(proxyConnectionHandler, inputStream,
					startLineBytes);

		} catch (HttpVersionNotSupportedException e) {

			/*
			 * ok, ho avuto un'eccezione sulla versione di protocollo nel creare
			 * una richiesta, forse è una risposta, provo a creare una risposta,
			 * se si verificano altre eccezioni non verranno catturate in questo
			 * metodo.
			 */
			httpData = createHttpResponse(proxyConnectionHandler, inputStream,
					startLineBytes);
		}

		/*
		 * se arrivo qua è stato creato correttamente o un oggetto HttpRequest o
		 * un oggetto HttpResponse ed è stato assegnato ad httpData, quindi
		 * posso uscire restituendolo.
		 */
		return httpData;
	}

	/**
	 * Questo metodo si occupa di creare un oggetto {@link HttpRequest}.
	 * 
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 * @param startLineBytes
	 * @param startLine
	 * @throws HttpNullStartLineException
	 * @throws HttpVersionNotSupportedException
	 * @throws HttpException
	 * @throws IOException
	 */
	public static HttpRequest createHttpRequest(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream, byte[] requestLineBytes)
			throws HttpNullStartLineException,
			HttpVersionNotSupportedException, HttpException, IOException {

		HttpRequest httpRequest;
		String requestLineString;
		String supportedHttpVersion;

		if (proxyConnectionHandler == null || inputStream == null) {
			throw new IOException(
					"FATAL ERROR: connection handler or stream are null!");
		}

		/*
		 * se la riga è vuota...
		 */
		if (requestLineBytes == null) {

			/*
			 * non posso creare il pacchetto HTTP e quindi sollevo un'eccezione.
			 */
			throw new HttpNullStartLineException(
					"Error to create HttpData, the start-line is null!");
		}

		requestLineString = new String(requestLineBytes, "ASCII");

		/*
		 * vedo se la versione di HTTP è supportata e la prendo.
		 */
		supportedHttpVersion = HttpUtils.getSupportedHttpVersionByStartLineString(
				requestLineString, HttpUtils.HttpPacketType.REQUEST);

		/*
		 * prendo il metodo della richiesta.
		 */
		String requestMethod = HttpUtils
				.getSupportedRequestMethodByStartLine(requestLineString);

		/*
		 * creo una HttpRequest.
		 */
		httpRequest = new HttpRequest(proxyConnectionHandler);

		/*
		 * creo una HttpRequestLine e dato che li ho letti, setto subito anche
		 * la versione http ed il metodo.
		 */
		HttpRequestLine requestLine = new HttpRequestLine(requestLineBytes);
		requestLine.setMethod(requestMethod);
		requestLine.setHttpVersion(supportedHttpVersion);

		/*
		 * setto la request-line nella richiesta.
		 */
		httpRequest.setStartLine(requestLine);

		/*
		 * costruisco un HttpHeader e lo assegno all'HttpRequest.
		 */
		httpRequest.makeHeaders(inputStream);

		/*
		 * costruisco il body e lo assegno all'HttpRequest.
		 */
		httpRequest.makeBody(inputStream);

		/*
		 * ritorno l'oggetto HttpRequest
		 */
		return httpRequest;
	}

	/**
	 * Questo metodo si occupa di creare un oggetto {@link HttpResponse}.
	 * 
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 * @param statusLineBytes
	 * @param statusLineString
	 * @return
	 * @throws HttpNullStartLineException
	 * @throws HttpVersionNotSupportedException
	 * @throws HttpMalformedResponseStatusCodeException
	 * @throws IOException
	 */
	public static HttpData createHttpResponse(
			ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream, byte[] statusLineBytes)
			throws HttpNullStartLineException,
			HttpVersionNotSupportedException,
			HttpMalformedResponseStatusCodeException, IOException {

		if (proxyConnectionHandler == null || inputStream == null) {
			throw new IOException(
					"FATAL ERROR: connection handler or stream are null!");
		}

		/*
		 * se la riga è vuota...
		 */
		if (statusLineBytes == null) {

			/*
			 * non posso creare il pacchetto HTTP e quindi sollevo un'eccezione.
			 */
			throw new HttpNullStartLineException(
					"Error to create HttpData, the start-line is null!");
		}

		String statusLineString = new String(statusLineBytes, "ASCII");
		HttpResponse httpResponse;
		String supportedHttpVersion;
		int validHttpResponseStatusCode = -1;

		/*
		 * vedo se la versione HTTP è supportata e la prendo.
		 */
		supportedHttpVersion = HttpUtils.getSupportedHttpVersionByStartLineString(
				statusLineString, HttpUtils.HttpPacketType.RESPONSE);

		/*
		 * vedo se lo status code è valido e lo prendo.
		 */
		validHttpResponseStatusCode = HttpUtils
				.getValidResponseStatusCodeByStartLine(statusLineString);

		/*
		 * creo una HttpResponse.
		 */
		httpResponse = new HttpResponse(proxyConnectionHandler);

		/*
		 * creo la status-line e dato che li ho letti, setto subito sia la
		 * versione di HTTP che il codice di stato.
		 */
		HttpResponseStatusLine httpResponseStatusLine = new HttpResponseStatusLine(
				statusLineBytes);
		httpResponseStatusLine.setHttpVersion(supportedHttpVersion);
		httpResponseStatusLine.setStatusCode(validHttpResponseStatusCode);

		/*
		 * assegno la status-line alla risposta.
		 */
		httpResponse.setStartLine(httpResponseStatusLine);

		/*
		 * creo un HttpHeader e lo assegno all'HttpResponse.
		 */
		httpResponse.makeHeaders(inputStream);

		/*
		 * creo il body e lo assegno all'HttpResponse.
		 */
		httpResponse.makeBody(inputStream);

		/*
		 * restituisco l'oggetto HttpResponse appena creato.
		 */
		return httpResponse;
	}
}