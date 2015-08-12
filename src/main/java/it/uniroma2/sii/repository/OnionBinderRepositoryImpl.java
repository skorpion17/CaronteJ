package it.uniroma2.sii.repository;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.model.OnionBinder;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class OnionBinderRepositoryImpl implements OnionBinderRepositoryCustom {
	@Autowired
	private OnionBinderRepository onionBinderRepository;
	private Object lock = new Object();

	/**
	 * Rende persistente un OnionBinder a partire dal suo onionName.
	 * 
	 * @param onionName
	 * @return
	 */
	@Transactional
	private OnionBinder persistOnionByName(final String onionName) {
		OnionBinder storedOnionBinder = onionBinderRepository
				.findByOnionName(onionName);
		if (storedOnionBinder == null) {
			storedOnionBinder = new OnionBinder();
			/* Bisogna inserire l'onionBinder perche non è presente */
			final Serializable maxAddressObject = onionBinderRepository
					.findMaxOnionBinderAddress();
			long maxAddress = 0L;
			if (maxAddressObject != null) {
				maxAddress = Long.parseLong(maxAddressObject.toString());
			}
			if (maxAddress < OnionBinderConfig.ONION_BINDER_ADDRESS_4_BYTE_START) {
				/* Non c'è alcun indirizzo valido nel db */
				maxAddress += OnionBinderConfig.ONION_BINDER_ADDRESS_4_BYTE_START;
			}
			/* Si incrementa l'indirizzo per ottenere il successivo */
			maxAddress += OnionBinderConfig.ONION_BINDER_ADDRESS_INCREMENT;
			System.out.printf("\t >>> register OnionBinderAddress: %d <<<\n",
					maxAddress);
			/* Imposta il nuovo indirizzo */
			storedOnionBinder.setAddress(maxAddress);
			storedOnionBinder.setOnionName(onionName);
			return onionBinderRepository.save(storedOnionBinder);
		} else {
			/* Gia presente nel repository */
			return storedOnionBinder;
		}
	}

	/**
	 * Permette di registrare un .onion sul repository interno attraverso
	 * {@code onionName}.
	 * 
	 * XXX: Per far si che registerOnionByName sia acceduto in accesso
	 * mutuamente esclusivo, è necessario wrappare un un metodo che si
	 * interfaccia col repository annotato con @Transactional.
	 */
	@Override
	public OnionBinder registerOnionByName(final String onionName) {
		synchronized (lock) {
			return persistOnionByName(onionName);
		}
	}
}
