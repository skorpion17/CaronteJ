package it.uniroma2.sii.log;

import it.uniroma2.sii.service.tor.web.server.ProxyConnectionHandler;
import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.response.HttpResponse;
import it.uniroma2.sii.util.data.unknown.UnknownData;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * Consente di gestire il log, potendo usare più tipi diversi di Logger.
 * 
 * @author Emanuele Altomare
 */
@Component
public class LoggerHandler implements Logger {

	@Value("${logger.path}")
	private String loggerPath;
	@Value("${logger.classes}")
	private String loggerString;
	@Autowired
	private AutowireCapableBeanFactory beanFactory;
	private List<LoggerAbstract> loggerList = null;

	/**
	 * Costruttore di default.
	 */
	public LoggerHandler() {
		loggerPath = null;
		loggerString = null;
		loggerList = new LinkedList<LoggerAbstract>();
	}

	@PostConstruct
	private void init() {
		if (loggerPath == null || loggerPath.isEmpty()) {
			System.out
					.println("You have to set the path of the loggers package, the logging operations will be off");
			return;
		}
		if (loggerString == null || loggerString.isEmpty()) {
			System.out
					.println("No one logger is selected, the logging operations will be off.");
			return;
		}

		/*
		 * splitto la stringa contenente le classi Logger da istanziare.
		 */
		String[] loggerStringArray = loggerString.split(",");

		if (loggerStringArray != null) {
			/*
			 * pulisco da eventuali spazi il path.
			 */
			loggerPath = loggerPath.trim();

			for (int i = 0; i < loggerStringArray.length; ++i) {
				/*
				 * pulisco da eventuali spazi.
				 */
				loggerStringArray[i] = loggerStringArray[i].trim();

				/*
				 * cerco di istanziare l'oggetto ed aggiungerlo alla lista.
				 */
				try {

					/*
					 * costruisco il nome completo della classe di tipo
					 * LoggerAbstract da istanziare.
					 */
					@SuppressWarnings("unchecked")
					Class<? extends LoggerAbstract> clazz = (Class<? extends LoggerAbstract>) Class
							.forName(loggerPath + "." + loggerStringArray[i]);

					/*
					 * dico a Spring di creare il bean realativo al logger.
					 */
					LoggerAbstract that = (LoggerAbstract) beanFactory
							.createBean(clazz);

					/*
					 * lo aggiungo alla lista dei loggers.
					 */
					loggerList.add(that);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void log(UnknownData unknownData,
			ProxyConnectionHandler proxyConnectionHandler) {
		if (!loggerList.isEmpty()) {
			for (LoggerAbstract logger : loggerList) {
				logger.log(unknownData, proxyConnectionHandler);
			}
		}
	}

	@Override
	public void closingConnection(ProxyConnectionHandler proxyConnectionHandler) {
		if (!loggerList.isEmpty()) {
			for (LoggerAbstract logger : loggerList) {
				logger.closingConnection(proxyConnectionHandler);
			}
		}
	}

	@Override
	public void log(HttpRequest httpRequest,
			ProxyConnectionHandler proxyConnectionHandler) {
		if (!loggerList.isEmpty()) {
			for (LoggerAbstract logger : loggerList) {
				logger.log(httpRequest, proxyConnectionHandler);
			}
		}

	}

	@Override
	public void log(HttpResponse httpResponse,
			ProxyConnectionHandler proxyConnectionHandler) {
		if (!loggerList.isEmpty()) {
			for (LoggerAbstract logger : loggerList) {
				logger.log(httpResponse, proxyConnectionHandler);
			}
		}
	}
}