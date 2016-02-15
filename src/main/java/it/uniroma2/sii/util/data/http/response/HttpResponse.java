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

import it.uniroma2.sii.log.Logger;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.HttpData;
import it.uniroma2.sii.util.data.http.HttpStartLine;
import it.uniroma2.sii.util.data.http.HttpUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Rappresenta la risposta HTTP.
 * 
 * @author Emanuele Altomare
 */
public class HttpResponse extends HttpData {

	public HttpResponse(ProxyConnectionHandler proxyConnectionHandler) {
		super(proxyConnectionHandler);
	}

	public HttpResponse(ProxyConnectionHandler proxyConnectionHandler,
			InputStream inputStream) throws IOException {
		super(proxyConnectionHandler, inputStream);
	}

	@Override
	public void log(Logger logger) {
		logger.log(this, proxyConnectionHandler);
	}

	@Override
	public byte[] makeBody(InputStream inputStream) throws IOException {
		byte[] body = HttpUtils.createHttpResponseBody(this, inputStream);
		setMessageBody(body);
		return getMessageBody();
	}

	@Override
	public HttpStartLine makeStartLine(InputStream inputStream)
			throws IOException {
		setStartLine(new HttpResponseStatusLine(inputStream));
		return getStartLine();
	}
}
