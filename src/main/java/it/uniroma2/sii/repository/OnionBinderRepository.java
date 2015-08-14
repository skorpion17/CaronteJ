package it.uniroma2.sii.repository;

import java.io.Serializable;

import it.uniroma2.sii.model.OnionBinder;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

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

	/**
	 * Ottiene il massimo degli indirizzi assegnati agli OnionBinder.
	 * 
	 * @return
	 */
	@Query("SELECT max(T.address) FROM #{#entityName} T")
	public Serializable findMaxOnionBinderAddress();
}
