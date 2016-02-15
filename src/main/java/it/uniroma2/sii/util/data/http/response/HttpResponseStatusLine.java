/*******************************************************************************
 * Copyright (c) 2015 Emanuele Altomare, Andrea Mayer
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
package it.uniroma2.sii.util.data.http.response;

import it.uniroma2.sii.util.data.http.HttpStartLine;
import it.uniroma2.sii.util.data.http.HttpUtils;
import it.uniroma2.sii.util.data.http.exception.HttpException;
import it.uniroma2.sii.util.data.http.exception.HttpMalformedResponseStatusCodeException;
import it.uniroma2.sii.util.data.http.exception.HttpNoReasonPhraseException;
import it.uniroma2.sii.util.data.http.exception.HttpNullStartLineException;
import it.uniroma2.sii.util.data.http.exception.HttpVersionNotSupportedException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Rappresenta la linea di stato della risposta HTTP.
 * 
 * @author Emanuele Altomare
 */
public class HttpResponseStatusLine extends HttpStartLine {

	int statusCode = -1;
	String reasonPhrase = null;

	public HttpResponseStatusLine(byte[] startLine) throws IOException {
		super(startLine);
	}

	public HttpResponseStatusLine(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	/**
	 * Consente di generare lo status-code a partire dalla start-line.
	 * 
	 * @throws HttpMalformedResponseStatusCodeException
	 * @throws HttpNullStartLineException
	 */
	private void makeStatusCode()
			throws HttpMalformedResponseStatusCodeException,
			HttpNullStartLineException {

		statusCode = HttpUtils.getValidResponseStatusCodeByStartLine(startLine);

		if (statusCode == -1) {
			throw new HttpMalformedResponseStatusCodeException(
					"The status-code is invalid.");
		}
	}

	/**
	 * Consente di generare la reason-phrase a partire dalla start-line.
	 * 
	 * @throws HttpNoReasonPhraseException
	 */
	private void makeReasonPhrase() throws HttpNoReasonPhraseException {
		String[] elements = startLine.split(" ");
		String reasonPhraseTemp = "";

		/*
		 * raggruppo tutti glie elementi separati da spazio dopo l'inizio della
		 * reason-phrase.
		 */
		for (int i = 2; i < elements.length; ++i) {
			reasonPhraseTemp += elements[i].trim() + " ";
		}

		/*
		 * non testo la correttezza della reason-phrase in abbinamento allo
		 * status-code poichè non è fondamentale che sia corretta per le
		 * funzioni svolte dal proxy.
		 */
		reasonPhrase = reasonPhraseTemp.trim();
	}

	/**
	 * Consente di ottenere lo status-code.
	 * 
	 * @return
	 * @throws HttpMalformedResponseStatusCodeException
	 * @throws HttpNullStartLineException
	 */
	public int getStatusCode() throws HttpNullStartLineException,
			HttpMalformedResponseStatusCodeException {
		if (statusCode == -1) {
			makeStatusCode();
		}
		return statusCode;
	}

	/**
	 * Consente di ottenere la reason-phrase.
	 * 
	 * @return
	 * @throws HttpNoReasonPhraseException
	 */
	public String getReasonPhrase() throws HttpNoReasonPhraseException {
		if (reasonPhrase == null) {
			makeReasonPhrase();
		}
		return reasonPhrase;
	}

	@Override
	protected void makeHttpVersion() throws HttpException {
		String supportedHttpVersion = HttpUtils
				.getSupportedHttpVersionByStartLineString(startLine,
						HttpUtils.HttpPacketType.RESPONSE);
		if (supportedHttpVersion == null) {
			throw new HttpVersionNotSupportedException(supportedHttpVersion);
		}
		httpVersion = supportedHttpVersion;
	}

	/**
	 * Consente di settare lo status-code. ATTENZIONE, non viene effettuato
	 * nessun controllo di correttezza.
	 * 
	 * @param statusCode
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}