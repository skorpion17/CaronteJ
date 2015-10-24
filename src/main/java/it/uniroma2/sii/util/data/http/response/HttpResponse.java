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
