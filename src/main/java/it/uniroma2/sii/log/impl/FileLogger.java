package it.uniroma2.sii.log.impl;

import it.uniroma2.sii.log.HttpDataContainer;
import it.uniroma2.sii.log.LoggerAbstract;
import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.HttpData;
import it.uniroma2.sii.util.data.http.HttpHeader;
import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.response.HttpResponse;
import it.uniroma2.sii.util.data.http.response.HttpResponseStatusLine;
import it.uniroma2.sii.util.data.unknown.UnknownData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

/**
 * Consente di effettuare il log su file.
 * 
 * @author Emanuele Altomare
 */
public class FileLogger extends LoggerAbstract {

	@Value("${logger.log.files.path}")
	private String LOG_FOLDER_PATH;
	private BufferedWriter fileWriter = null;

	private enum HttpDataType {
		HTTP_REQUEST, HTTP_RESPONSE
	}

	/*
	 * contiene i nuovi container con le richieste e risposte HTTP ancora da
	 * servire
	 */
	private final Map<UUID, HttpDataContainer> loggerMap;

	/**
	 * Consente di disaccoppiare l'operazione di salvataggio su file dal normale
	 * flusso migliorando le prestazioni.
	 * 
	 * @author Emanuele Altomare
	 */
	private class LogFileSaver extends Thread {

		/**
		 * Costruttore di default.
		 */
		public LogFileSaver() {
		}

		/**
		 * Resta in ascolto sugli elementi HttpDataContainer ed effettua
		 * l'operazione di log delle richieste e risposte abbinate ed
		 * appartenenti all'oggetto HttpDataContainer.
		 * 
		 * @author Emanuele Altomare
		 */
		private class LogFileSaverWorker extends Thread {

			final HttpDataContainer container;

			LogFileSaverWorker(HttpDataContainer container) {
				this.container = container;
			}

			public void run() {
				while (true) {
					synchronized (container) {
						if (container.isClosingConnection()) {
							return;
						}

						/*
						 * finchè la coda è vuota...
						 */
						while (container.requestQueueIsEmpty()) {
							try {

								/*
								 * mi metto in attesa.
								 */
								container.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
								return;
							}
						}

						/*
						 * finchè la coda delle risposte è vuota...
						 */
						while (container.responseQueueIsEmpty()) {
							try {

								/*
								 * mi metto in attesa.
								 */
								container.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
								return;
							}
						}

						/*
						 * a questo punto so che esiste almeno un elemento in
						 * testa ad entrambe, quindi posso eseguire il log. Il
						 * meccanismo si basa sul fatto che da RFC in HTTP/1.1
						 * quando ho una connessione persistente le richieste e
						 * le risposte possono essere messe in pipeline, ma le
						 * risposte devono essere spedite nello stesso ordine
						 * delle richieste. In parole povere la prima risposta
						 * che entra deve essere relativa alla prima richiesta
						 * inviata e così via. Dato che per ogni connessione
						 * solo un thread si occupa delle richieste e solo uno
						 * delle risposte, l'ordine sarà rispettato ed in testa
						 * alle due code avrò sempre una richiesta ed una
						 * risposta correlate.
						 * 
						 * Prendo quindi il lock sul file.
						 */
						synchronized (getLock()) {
							String separator = "---------------------------------------\n";
							try {
								fileWriter.write(separator);

								/*
								 * scrivo sul file la richiesta in testa e la
								 * rimuovo.
								 */
								HttpRequest request = container.popRequest();
								fileWriter.write(String.format(
										"Connection UUID >>> %s\n", request
												.getProxyConnectionHandler()
												.getConnectionId().toString()));
								fileWriter.write(request.toString() + "\n");

								/*
								 * scrivo sul file la risposta in testa e la
								 * rimuovo.
								 */
								HttpResponse response = container.popResponse();
								fileWriter.write(String.format(
										"Connection UUID >>> %s\n", response
												.getProxyConnectionHandler()
												.getConnectionId().toString()));
								fileWriter.write(response.toString() + "\n");
								fileWriter.write(separator);
								fileWriter.flush();

								/*
								 * qui aggiungo una parte per dare enfasi alle
								 * risposte che presentano un redirect.
								 */
								HttpResponseStatusLine responseStatusLine = (HttpResponseStatusLine) response
										.getStartLine();

								String responseStatusCodeString = String
										.valueOf(responseStatusLine
												.getStatusCode());

								HttpHeader responseHeader = response
										.getHeader();

								if (responseStatusCodeString
										.matches("[3][0-9][0-9]")) {
									separator = "\n\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n";
									fileWriter.write(separator);
									fileWriter
											.write("ATTENZIONE!!! C'è stato un pericolosissimo redirect!\n");
									fileWriter.write(String.format(
											"Codice di stato: %s\n",
											responseStatusCodeString));
									fileWriter.write(String.format(
											"Motivo: %s\n", responseStatusLine
													.getReasonPhrase()));
									fileWriter.write(String.format(
											"Verso: %s\n",
											responseHeader.get("Location")));
									fileWriter
											.write(String.format(
													"Body:\n%s\n",
													new String(response
															.getMessageBody(),
															"ASCII")));
									fileWriter.write(separator);

									fileWriter.flush();
								}

							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		public void run() {
			while (true) {

				/*
				 * blocco la mappa del logger.
				 */
				synchronized (loggerMap) {

					/*
					 * finchè la mappa è vuota...
					 */
					while (loggerMap.isEmpty()) {
						try {

							/*
							 * mi metto in attesa.
							 */
							loggerMap.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
							return;
						}
					}

					/*
					 * per ogni HttpDataContainer...
					 */
					for (Entry<UUID, HttpDataContainer> entry : loggerMap
							.entrySet()) {
						HttpDataContainer container = entry.getValue();

						/*
						 * blocco il container
						 */
						synchronized (container) {

							/*
							 * se non è attivo un worker.
							 */
							if (!container.isServed()) {

								/*
								 * lo creo e lo avvio.
								 */
								new LogFileSaverWorker(container).start();

								/*
								 * setto infine a true la variabile che indica
								 * che esiste un worker attivo sul container.
								 */
								container.setServed(true);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Costruttore di default.
	 */
	public FileLogger() {

		/*
		 * inizializzo la lista che conterrà le richieste e risposte HTTP
		 * ordinate per connessione.
		 */
		loggerMap = new HashMap<UUID, HttpDataContainer>();
	}

	@PostConstruct
	public void init() {

		/*
		 * prendo la data.
		 */
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String formattedDate = format.format(Calendar.getInstance().getTime());
		String fileName = String.format("%s-carontej.log", formattedDate);
		String stringFilePath = String.format("%s%s%s", LOG_FOLDER_PATH,
				File.separator, fileName);
		/*
		 * creo il writer.
		 */
		Charset charset = Charset.forName("UTF-8");
		Path filePath = Paths.get(URI.create("file://" + stringFilePath));
		try {
			fileWriter = Files.newBufferedWriter(filePath, charset,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * faccio partire il thread che gestisce il salvataggio su file.
		 */
		new LogFileSaver().start();

	}

	@Override
	public void log(HttpRequest httpRequest,
			ProxyConnectionHandler proxyConnectionHandler) {
		log(httpRequest, proxyConnectionHandler, HttpDataType.HTTP_REQUEST);
	}

	@Override
	public void log(HttpResponse httpResponse,
			ProxyConnectionHandler proxyConnectionHandler) {
		log(httpResponse, proxyConnectionHandler, HttpDataType.HTTP_RESPONSE);
	}

	/**
	 * Consente di effettuare il log sia di una richiesta sia di una risposta in
	 * base al valore dell'enum {@link HttpDataType} passata come parametro.
	 * 
	 * @param httpData
	 * @param proxyConnectionHandler
	 * @param type
	 */
	private void log(HttpData httpData,
			ProxyConnectionHandler proxyConnectionHandler, HttpDataType type) {
		if ((httpData != null) && (proxyConnectionHandler != null)) {
			HttpDataContainer container;
			UUID connectionId = proxyConnectionHandler.getConnectionId();

			/*
			 * blocco la mappa del logger.
			 */
			synchronized (loggerMap) {

				/*
				 * se è già presente un container per quella connessione...
				 */
				if (loggerMap.containsKey(connectionId)) {

					/*
					 * lo prendo.
					 */
					container = loggerMap.get(connectionId);

					/*
					 * aggiungo la richiesta o la risposta.
					 */
					do {
						if (type.equals(HttpDataType.HTTP_REQUEST)) {
							container.pushRequest((HttpRequest) httpData);
							break;
						}
						if (type.equals(HttpDataType.HTTP_RESPONSE)) {
							container.pushResponse((HttpResponse) httpData);
							break;
						}

						/*
						 * qui non ci si dovrebbe mai arrivare, se dovesse
						 * essere, ritorno senza fare nulla.
						 */
						return;
					} while (false);

					/*
					 * dopo aver aggiunto, blocco il container.
					 */
					synchronized (container) {

						/*
						 * se c'è un worker che sta lavorando sul container...
						 */
						if (container.isServed()) {

							/*
							 * gli notifico che qualcosa è stato aggiunto.
							 */
							container.notify();
						}
					}
				} else {

					/*
					 * creo un nuovo container per la connessione.
					 */
					container = new HttpDataContainer(connectionId);

					/*
					 * aggiungo la richiesta o la risposta.
					 */
					do {
						if (type.equals(HttpDataType.HTTP_REQUEST)) {
							container.pushRequest((HttpRequest) httpData);
							break;
						}
						if (type.equals(HttpDataType.HTTP_RESPONSE)) {
							container.pushResponse((HttpResponse) httpData);
							break;
						}

						/*
						 * qui non ci si dovrebbe mai arrivare, se dovesse
						 * essere, ritorno senza fare nulla.
						 */
						return;
					} while (false);

					/*
					 * aggiungo il container alla lista del logger.
					 */
					loggerMap.put(connectionId, container);

					/*
					 * notifico al thread che gestisce la lista del logger, che
					 * è stato aggiunto un nuovo elemento.
					 */
					loggerMap.notify();
				}
			}
		}
	}

	@Override
	public void log(UnknownData unknownData,
			ProxyConnectionHandler proxyConnectionHandler) {
		synchronized (getLock()) {
			String separator = "---------------------------------------\n";
			try {
				fileWriter.write(separator);
				fileWriter.write(String.format("Connection UUID >>> %s\n",
						proxyConnectionHandler.getConnectionId().toString()));
				fileWriter.write("Protocollo non gestito!\n");
				fileWriter.write(separator);
				fileWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void finalize() {
		if (fileWriter != null) {
			try {
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void closingConnection(ProxyConnectionHandler proxyConnectionHandler) {
		/*
		 * ottengo l'id della connessione che si sta per chiudere.
		 */
		UUID connectionId = proxyConnectionHandler.getConnectionId();
		/*
		 * prendo il lock sulla mappa.
		 */
		synchronized (loggerMap) {
			if (!loggerMap.isEmpty()) {
				/*
				 * se trovo l'HttpDataContainer relativo a questa connessione...
				 */
				if (loggerMap.containsKey(connectionId)) {
					HttpDataContainer container = loggerMap.get(connectionId);
					/*
					 * avverto chi è in ascolto che la connessione sta per
					 * essere chiusa.
					 */
					synchronized (container) {
						container.setClosingConnection(true);
					}
					/*
					 * la elimino.
					 */
					loggerMap.remove(connectionId);
				}
			}
		}
	}
}
