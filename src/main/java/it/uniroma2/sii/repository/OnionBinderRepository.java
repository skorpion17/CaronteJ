/*******************************************************************************
 * Copyright (c) 2015, 2016 Emanuele Altomare, Andrea Mayer
 *
 * This file is part of Proxy2Tor.
 * Proxy2Tor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * Proxy2Tor is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proxy2Tor.  If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
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
