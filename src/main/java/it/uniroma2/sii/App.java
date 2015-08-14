package it.uniroma2.sii;

import it.uniroma2.sii.service.tor.dns.server.TorDNSServer;
import it.uniroma2.sii.service.tor.webproxy.server.HTTPProxyServer;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class App {
	/**
	 * File di configurazione dell'applicazione.
	 */
	public static final String APPLICATION_CONTEXT_XML = "applicationContext.xml";

	@Autowired
	private TorDNSServer torDNSServer;
	@Autowired
	private HTTPProxyServer httpProxyServer;

	/**
	 * TODO: Inizializza l'applicazione.
	 */
	public void init() {
		torDNSServer.start();
		httpProxyServer.start();
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) throws UnknownHostException {
		@SuppressWarnings("resource")
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				APPLICATION_CONTEXT_XML);
		/* Avvia l'applicazione */
		App app = context.getBean(App.class);
		app.init();
	}
}
