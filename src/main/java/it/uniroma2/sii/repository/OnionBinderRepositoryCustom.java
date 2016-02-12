package it.uniroma2.sii.repository;

import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.exception.NoMoreOnionBinderAddressAvailableException;

import java.io.IOException;

/**
 * Repository custom per {@link OnionBinder}.
 * 
 * @author andrea
 *
 */
public interface OnionBinderRepositoryCustom {
	/**
	 * Permette di registrare una coppia <onion,indirizzo_ip_privato> in modo
	 * tale che la connect e le altre operazioni di open di glibc non vedano in
	 * alcun modo l'indirizzo .onion. La traduzione da indirizzo ip privato
	 * associato al particolare .onion verrà effettuata nel proxy in modo
	 * completamente trasparente per l'applicazione client.
	 * 
	 * @param onionName
	 * @return
	 * @throws NoMoreOnionBinderAddressAvailableException
	 * @throws IOException
	 */
	public OnionBinder registerOnionBinderByNameWithExpirationTime(
			final String onionName)
			throws NoMoreOnionBinderAddressAvailableException, IOException;

	/**
	 * Permette di ottenere un oggetto {@link OnionBinder} identificato da
	 * {@code ipv4} se presente nello strato di storage. Inoltre questo metodo
	 * una volta chiamato va ad aggiornare il contenuto della cache rimuovendo
	 * le coppie <address,onoin> scadute e facendo il refresh di quella a cui
	 * l'eventuale onion recuperato si riferisce.
	 * 
	 * @param ipv4
	 * @return
	 * @throws IOException
	 */
	public OnionBinder findOnionBindByAddressAndDoCacheRefreshing(final int ipv4)
			throws IOException;

	/**
	 * Verifica se {@link OnionBinder} {@code onion} esiste senza rinfrescare il
	 * suo expiration_time.
	 * 
	 * @param onion
	 * @return
	 */
	public boolean checkIfOnionBinderExistsWithoutCacheRefreshing(
			final String onion);
}
