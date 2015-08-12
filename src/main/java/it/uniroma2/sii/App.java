package it.uniroma2.sii;

import it.uniroma2.sii.service.tor.dns.server.TorDNSServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class App {
	private static final String APPLICATION_CONTEXT_XML = "applicationContext.xml";

	@Autowired
	private TorDNSServer torDNSServer;

	/**
	 * TODO: Inizializza l'applicazione.
	 */
	public void init() {
		torDNSServer.start();
		try {
			/* Attende che il server termini */
			torDNSServer.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				APPLICATION_CONTEXT_XML);
		/* Avvia l'applicazione */
		App app = context.getBean(App.class);
		app.init();
	}
}
