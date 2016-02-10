package it.uniroma2.sii.repository;

import it.uniroma2.sii.model.OnionBinder;

import java.io.Serializable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO per OnionBinder.
 * 
 * @author andrea
 *
 */
@Repository
public interface OnionBinderRepository extends
		CrudRepository<OnionBinder, Integer>, OnionBinderRepositoryCustom {

	public OnionBinder findByOnionName(final String onionName);

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
