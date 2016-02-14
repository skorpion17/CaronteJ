package it.uniroma2.sii;

import it.uniroma2.sii.generator.OnionURL;
import it.uniroma2.sii.generator.OnionURLGenerator;
import it.uniroma2.sii.log.LoggerHandler;
import it.uniroma2.sii.service.tor.dns.server.TorDNSServer;
import it.uniroma2.sii.service.tor.web.server.WebProxyServer;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Entry point per l'applicazione.
 * 
 * @author Andrea Mayer, Emanuele Altomare
 *
 */
@Component
public class App {
	/**
	 * File di configurazione dell'applicazione.
	 */
	public static final String APPLICATION_CONTEXT_XML = "applicationContext.xml";

	@Value("${onion.generator.main.app.start}")
	private boolean allowOnionGeneratorRunAtBoostrap = false;
	@Autowired
	private TorDNSServer torDNSServer;
	@Autowired
	private WebProxyServer httpProxyServer;
	@Autowired
	private LoggerHandler logger;
	@Autowired
	private OnionURL onionURL;

	/**
	 * Inizializzatore della applicazione.
	 */
	public void init() {
		torDNSServer.start();
		httpProxyServer.start();
		/*
		 * All'avvio dell'applicazione genera un .onion per mostrare il
		 * funzionamento del generatore di URL di Tor.
		 */
		testGenerateOnionAddress();
	}

	/**
	 * Metodo per testare la generazione di un indirizzo .onion
	 */
	private void testGenerateOnionAddress() {
		if (allowOnionGeneratorRunAtBoostrap) {
			try {
				/*
				 * Sleep soltanto per questioni di estetica, evita che l'output
				 * del dei servizi si sovrapponga a quello del test sul
				 * generatore.
				 */
				Thread.sleep(1000);
				System.out.println("\n\t >>> DEMO OnionURLGenerator <<< ");
				OnionURLGenerator generator = onionURL
						.createOnionURLGenerator();
				System.out.println("ONION ADDRESS:");
				System.out.println(generator.generateOnionAddress() + "\n");
				System.out.println("PUBLIC KEY:");
				System.out.println(OnionURL.convertToPem(generator
						.getPubKeyInDer()) + "\n");
				System.out.println("PRIVATE KEY:");
				System.out.println(OnionURL.convertToPem(generator
						.getPrivKeyInDer()));
				generator.saveOnionAddress();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Entry point.
	 * 
	 * @param args
	 * @throws UnknownHostException
	 */
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				APPLICATION_CONTEXT_XML);
		/* Avvia l'applicazione */
		App app = context.getBean(App.class);
		app.init();
		// context.close();
	}
}