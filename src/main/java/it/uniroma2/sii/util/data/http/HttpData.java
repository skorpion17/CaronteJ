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
import it.uniroma2.sii.util.data.Data;
import it.uniroma2.sii.util.data.http.exception.HttpException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Rappresenta il pacchetto HTTP.
 * 
 * @author Emanuele Altomare
 */
public abstract class HttpData extends Data {

	private HttpStartLine startLine;

	private HttpHeader headers = null;

	/*
	 * contiene il body così come viene letto.
	 */
	private byte[] messageBody = null;

	/*
	 * contiene il body riassemblato eliminando le possibili codifiche di
	 * trasferimento. ATTENZIONE viene eliminata solo la codifica sul
	 * message-body se presente, l'entity-body resta inalterato e può presentare
	 * altre codifiche (es. gzip), segnalate dall'header Content-Encoding in
	 * ordine di aplicazione sull'entity-body e separate da virgole.
	 */
	private byte[] entityBody = null;

	/**
	 * Costruttore per il dato Http.
	 * 
	 */
	public HttpData(ProxyConnectionHandler proxyConnectionHandler) {
		super(proxyConnectionHandler);
	}

	/**
	 * Costruttore che consente di creare a partire dallo stream in input, un
	 * oggetto {@link HttpData}.
	 * 
	 * @param proxyConnectionHandler
	 * @param inputStream
	 * @throws HttpException
	 */
	public HttpData(ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {
		this(proxyConnectionHandler);
		startLine = makeStartLine(inputStream);
		headers = makeHeaders(inputStream);
		messageBody = makeBody(inputStream);
	}

	/**
	 * Consente di costruire il body a partire dall'InputStream e di assegnarlo
	 * all'oggetto.
	 * 
	 * @param inputtStream
	 * @return l'array di byte contenente il body.
	 * @throws IOException
	 */
	public abstract byte[] makeBody(InputStream inputtStream)
			throws IOException;

	/**
	 * Consente di costruire gli headers a partire dall' Input Stream e di
	 * assegnarlo all'oggetto.
	 * 
	 * @param inputStream
	 * @return l'oggetto HttpHeader costruito ed assegnato all'oggetto.
	 * @throws IOException
	 */
	public HttpHeader makeHeaders(InputStream inputStream) throws IOException {
		setHeader(HttpUtils.createHttpHeader(inputStream));
		return getHeader();
	}

	/**
	 * Consente di costruire la start-line a partire dall'InputStream ed
	 * assegnarla all'oggetto.
	 * 
	 * @param inputStream
	 * @return l'oggetto HttpStartLine costruito ed assegnato all'oggetto.
	 * @throws IOException
	 */
	public abstract HttpStartLine makeStartLine(InputStream inputStream)
			throws IOException;

	/**
	 * Consente di ottenere la start-line.
	 * 
	 * @return
	 */
	public HttpStartLine getStartLine() {
		return startLine;
	}

	/**
	 * Consente di settare la start line.
	 * 
	 * @param startLine
	 */
	public void setStartLine(HttpStartLine startLine) {
		this.startLine = startLine;
	}

	/**
	 * Consente di settare gli headers.
	 *
	 * @param headers
	 */
	public void setHeader(HttpHeader headers) {
		this.headers = headers;
	}

	/**
	 * Consente di ottenere l'header HTTP.
	 * 
	 * @return
	 */
	public HttpHeader getHeader() {
		return headers;
	}

	/**
	 * Consente di settare il message-body.
	 * 
	 * @param messageBody
	 */
	public void setMessageBody(byte[] messageBody) {
		this.messageBody = messageBody;
	}

	/**
	 * Consente di settare l'entity-body.
	 * 
	 * @param entityBody
	 */
	public void setEntityBody(byte[] entityBody) {
		this.entityBody = entityBody;
	}

	/**
	 * Consente di ottenere il body così come è stato letto.
	 * 
	 * @return
	 */
	public byte[] getMessageBody() {
		return messageBody;
	}

	/**
	 * Consente di ottenere il body riassemblato eliminando le possibili
	 * codifiche di trasferimento. ATTENZIONE viene eliminata solo la codifica
	 * sul message-body se presente, l'entity-body resta inalterato e può
	 * presentare altre codifiche (es. gzip), segnalate dall'header
	 * Content-Encoding in ordine di aplicazione sull' entity-body e separate da
	 * virgole.
	 * 
	 * @return
	 */
	public byte[] getEntityBody() {
		return entityBody;
	}

	/**
	 * Consente di ottenere l'header HTTP in bytes.
	 * 
	 * @return
	 */
	public byte[] getHeaderInBytes() {
		return headers.getBytes();
	}

	public byte[] getDataInBytes() throws IOException {
		byte[] data = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(getStartLine().getLineBytes());
		outputStream.flush();
		outputStream.write(headers.getBytes());
		outputStream.flush();
		if (messageBody != null) {
			outputStream.write(messageBody);
			outputStream.flush();
		}
		data = outputStream.toByteArray();
		outputStream.close();
		return data;
	}

	public String toString() {
		double bodyLength = (messageBody != null ? (double) messageBody.length
				: 0.0) / (double) 1024;
		return String.format("%s%sBody Dimension: %sKB\n",
				startLine.toString(), headers.toString(),
				String.valueOf(bodyLength));
	}
}