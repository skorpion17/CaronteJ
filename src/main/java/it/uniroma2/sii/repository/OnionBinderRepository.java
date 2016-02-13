package it.uniroma2.sii.repository;

import it.uniroma2.sii.model.OnionBinder;

import java.io.Serializable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository per OnionBinder. Il repository permette l'accesso ai
 * {@link OnionBinder} che sono disponibili attualmente nel DB.
 * 
 * @author Andrea Mayer
 *
 */
@Repository
public interface OnionBinderRepository extends
		CrudRepository<OnionBinder, Integer>, OnionBinderRepositoryCustom {

	/**
	 * Ottiene {@link OnionBinder} attraverso il suo URL {@link onionName}.
	 * 
	 * @param onionName
	 * @return
	 */
	@Transactional
	public OnionBinder findByOnionName(final String onionName);

	/**
	 * Ottiene {@link OnionBinder} attraverso il suo indirizzo ip associato
	 * durante la risoluzione interna.
	 * 
	 * @param address
	 * @return
	 */
	@Transactional
	public OnionBinder findByAddress(final int address);

	/**
	 * Ottiene il massimo degli indirizzi assegnati agli OnionBinder.
	 * 
	 * @return
	 */
	@Transactional
	@Query("SELECT max(T.address) FROM #{#entityName} T")
	public Serializable findMaxOnionBinderAddress();
}
