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
package it.uniroma2.sii.util.data.http;

import it.uniroma2.sii.util.data.http.exception.HttpException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Rappresenta la prima riga di un messaggio HTTP.
 * 
 * @author Emanuele Altomare
 */
public abstract class HttpStartLine {

	protected String startLine = "";
	protected String httpVersion = null;

	/**
	 * Inizializza la start-line.
	 * 
	 * @param startLineBytes
	 * @throws IOException
	 */
	private void initStartLine(byte[] startLineBytes) throws IOException {
		if (startLineBytes == null) {
			throw new IOException("The start-line is null!");
		}

		startLine = new String(startLineBytes, "ASCII");

		/*
		 * aggiungo i caratteri CRLF di fine linea.
		 */
		startLine += new String(new byte[] { HttpUtils.CR, HttpUtils.LF },
				"ASCII");
	}

	public HttpStartLine() {
	}

	/**
	 * Consente di costruire un oggetto {@link HttpStartLine} a partire dai
	 * bytes.
	 * 
	 * @param startLineBytes
	 * @throws IOException
	 */
	public HttpStartLine(byte[] startLineBytes) throws IOException {
		this();
		initStartLine(startLineBytes);
	}

	/**
	 * Consente di costruire l'oggetto {@link HttpStartLine} a partire dai dati
	 * dentro all'InputStream passato come parametro.
	 * 
	 * @param inputStream
	 * @throws IOException
	 */
	public HttpStartLine(InputStream inputStream) throws IOException {
		this();
		if (inputStream == null) {
			throw new IOException("The input stream is null!");
		}

		/*
		 * leggo la start-line dall'input stream.
		 */
		byte[] startLineBytes = HttpUtils
				.readLineBytesFromInputStream(inputStream);
		initStartLine(startLineBytes);
	}

	/**
	 * Creo la start-line a partire da una stringa.
	 * 
	 * @param line
	 */
	public HttpStartLine(String line) {
		setStartLine(line);
	}

	/**
	 * Restituisco la start-line.
	 * 
	 * @return
	 */
	public String getStartLine() {
		return startLine;
	}

	/**
	 * Restituisco la start-line in bytes.
	 * 
	 * @return
	 */
	public byte[] getLineBytes() {
		return startLine.getBytes();
	}

	/**
	 * setto la start-line con la stringa in input.
	 * 
	 * @param startLine
	 */
	public void setStartLine(String startLine) {
		this.startLine = startLine;
	}

	/**
	 * Consente di popolare il campo httpVersion effettuando i controlli
	 * necessari.
	 * 
	 * @throws HttpException
	 */
	protected abstract void makeHttpVersion() throws HttpException;

	/**
	 * Consente di ottenere la versione del protocollo HTTP in uso.
	 * 
	 * @return
	 * @throws HttpException
	 */
	public String getHttpVersion() throws HttpException {
		if (httpVersion == null) {
			makeHttpVersion();
		}
		return httpVersion;
	}

	/**
	 * setta la versione del protocollo HTTP. ATTENZIONE, non viene effettuato
	 * alcun controllo di correttezza!
	 * 
	 * @param httpVersion
	 */
	public void setHttpVersion(String httpVersion) {
		this.httpVersion = httpVersion;
	}

	public String toString() {
		return String.format("%s", startLine);
	}
}
