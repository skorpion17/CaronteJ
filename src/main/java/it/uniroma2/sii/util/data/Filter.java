package it.uniroma2.sii.util.data;

import it.uniroma2.sii.log.Logger;

/**
 * Rappresenta le operazioni che si possono applicare ad un oggetto di tipo
 * {@link Data}.
 * 
 * @author Emanuele Altomare
 */
public interface Filter {

	/**
	 * Consente di effettuare il log per il tipo Data
	 */
	public void log(Logger logger);

}
