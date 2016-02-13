package it.uniroma2.sii.repository;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.model.OnionBinder;
import it.uniroma2.sii.repository.exception.NoMoreOnionBinderAddressAvailableException;
import it.uniroma2.sii.util.address.AddressUtils;
import it.uniroma2.sii.util.transaction.SimpleTransaction;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementazione di {@link OnionBinderRepositoryCustom}.
 * 
 * @author Andrea Mayer
 *
 */
@Repository
public class OnionBinderRepositoryImpl implements OnionBinderRepositoryCustom {
	private static final String QUERY_DELETE_FROM_ONION_BINDER_EXPIRATION_TIMESTAMP = "DELETE FROM OnionBinder T WHERE T.expirationTimestamp <= :expirationTimestamp";

	@Autowired
	private OnionBinderConfig onionBinderConfig;
	@Autowired
	private OnionBinderRepository onionBinderRepository;
	@PersistenceContext
	private EntityManager entityManager;
	@Autowired
	private PlatformTransactionManager platformTransactionManager;
	/** Lock interno. */
	private Object lock = new Object();

	/**
	 * Rimuove tutti gli {@link OnionBinder} che hanno un
	 * {@link OnionBinder#getExpirationTimestamp()} >= {@code expirationTime}.
	 * 
	 * @param expirationTimestamp
	 * @return numero di {@link OnionBinder} cancellati a causa della scadenza
	 *         del loro expirationTime.
	 */
	@Transactional(rollbackFor = Exception.class)
	public int deleteOnionBinderWithExpirationTimeLessOrEqualTo(
			final Date expirationTimestamp) {
		final Query deleteQuery = entityManager
				.createQuery(QUERY_DELETE_FROM_ONION_BINDER_EXPIRATION_TIMESTAMP);
		/* Imposta il parametro */
		deleteQuery.setParameter("expirationTimestamp", expirationTimestamp,
				TemporalType.TIMESTAMP);
		/* Esegue la query e ritorna il numero di entry cancellate. */
		final int ndelete = deleteQuery.executeUpdate();
		return ndelete;
	}

	/**
	 * Cerca il prossimo indirizzo libero per il binding tra .onion e indirizzo
	 * ip locale per costruire l'associazione .onion <=> local_ip.
	 * 
	 * @return
	 * @throws NoMoreOnionBinderAddressAvailableException
	 * @throws UnknownHostException
	 */
	@Transactional
	public int findNextFreeOnionBinderAddress()
			throws NoMoreOnionBinderAddressAvailableException,
			UnknownHostException {
		/* Numero totale di indirizzi disponibili */
		final int numberOfAvailableAddresses = (int) Math.pow(2,
				32 - onionBinderConfig.getOnionBinderNumberBitOfNetmask());
		/* Qui puo esserci overflow, ma non è un problema. */
		final int startAddress = onionBinderConfig
				.getOnionBinderAddressStartFrom();
		final int endAddress = startAddress + numberOfAvailableAddresses;
		/*
		 * Si cerca il primo indirizzo disponibile a partire dal basso.
		 * 
		 * XXX: Poichè potrebbero diventare negativi (causa overflow con interi
		 * a 32 bit con segno) i valori di i non si puo usare < ma si deve
		 * iterare fino a quando non si raggiunge il l'indirizzo finale.
		 */
		for (int i = startAddress; i != endAddress; ++i) {
			final OnionBinder onionBinder = onionBinderRepository
					.findByAddress(i);
			if (onionBinder == null) {
				/*
				 * Non c'è alcun onionBinder con l'indirizzo scelto, si ritorna
				 * l'indirizzo. Inoltre si verifica che l'indirizzo che si vuole
				 * ritornare sia valido.
				 */
				AddressUtils.fromIntIPv4ToInetaddress(i);
				if ((i & 0xFF) == 0x00) {
					/* Si salta un indirizzo che termina con .0 */
					continue;
				}
				return i;
			}
		}
		throw new NoMoreOnionBinderAddressAvailableException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniroma2.sii.repository.OnionBinderRepositoryCustom#
	 * findOnionBindByAddressAndDoCacheRefreshing(int)
	 */
	@Override
	public OnionBinder findOnionBindByAddressAndDoCacheRefreshing(final int ipv4)
			throws IOException {
		OnionBinder onionBinder;
		final SimpleTransaction transaction = SimpleTransaction
				.factory(platformTransactionManager);
		synchronized (lock) {
			transaction.open();
			try {
				/* Si pulishe la cache rimuovendo tutti i binding scaduti */
				final Date now = new Date(System.currentTimeMillis());
				deleteOnionBinderWithExpirationTimeLessOrEqualTo(now);
				/*
				 * Si recupera l'onionBinder dal database se è ancora presente
				 * (può essere stato cancellato dal refreshing della cache)
				 */
				onionBinder = onionBinderRepository.findByAddress(ipv4);
			} catch (final Exception e) {
				transaction.rollback();
				/* La rilancio come eccezione di I/O */
				throw new IOException(e);
			}
			transaction.commit();
		}
		return onionBinder;
	}

	/**
	 * Permette di memorizzare l'OnionBinder all'interno dello strato di
	 * persistenza.
	 * 
	 * @param onionName
	 * @return
	 * @throws NoMoreOnionBinderAddressAvailableException
	 * @throws UnknownHostException
	 */
	private OnionBinder persistOnionBinderByNameWithExpirationTime(
			final String onionName)
			throws NoMoreOnionBinderAddressAvailableException,
			UnknownHostException {
		/*
		 * Si richiama come prima cosa il delete per liberare la cache da
		 * OnionBinder scaduti.
		 */
		final Date now = new Date(System.currentTimeMillis());
		deleteOnionBinderWithExpirationTimeLessOrEqualTo(now);
		/* Si cerca una corrispondenza tra .onoin ed indirizzo ip. */
		OnionBinder storedOnionBinder = onionBinderRepository
				.findByOnionName(onionName);
		if (storedOnionBinder == null) {
			/*
			 * Non c'è alcuna corrispondenza, bisogna inserire un nuovo
			 * OnionBinder che utilizzi un indirizzo ip locale privato
			 * disponibile.
			 */
			storedOnionBinder = new OnionBinder();
			/* Ottengo l'indirizzo disponibile e valido. */
			final int maxAddress = findNextFreeOnionBinderAddress();
			/*
			 * Stampo a video l'indirizzo ip che è stato scelto come libero e
			 * pronto per la registrazione.
			 */
			System.out.printf("\t >>> register OnionBinderAddress: %s <<<\n",
					AddressUtils.fromIntIPv4ToInetaddress(maxAddress)
							.getHostAddress());
			/* Imposta il nuovo indirizzo */
			storedOnionBinder.setAddress(maxAddress);
			storedOnionBinder.setOnionName(onionName);
			storedOnionBinder.updateExpirationTimestamp(onionBinderConfig
					.getOnionBinderRelativeExpirationTimeInMillis());
			return onionBinderRepository.save(storedOnionBinder);
		} else {
			/*
			 * Gia presente nel repository, aggiorno l'expirationTime sullo
			 * strato di storage.
			 */
			storedOnionBinder.updateExpirationTimestamp(onionBinderConfig
					.getOnionBinderRelativeExpirationTimeInMillis());
			return storedOnionBinder;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniroma2.sii.repository.OnionBinderRepositoryCustom#
	 * registerOnionBinderByNameWithExpirationTime(java.lang.String)
	 */
	@Override
	public OnionBinder registerOnionBinderByNameWithExpirationTime(
			final String onionName)
			throws NoMoreOnionBinderAddressAvailableException, IOException {
		OnionBinder onionBinder;
		final SimpleTransaction transaction = SimpleTransaction
				.factory(platformTransactionManager);
		/*
		 * Si prende il lock in modo tale che questa operazione di registrazione
		 * sia effettuata in modo serializzato.
		 */
		synchronized (lock) {
			transaction.open();
			try {
				onionBinder = persistOnionBinderByNameWithExpirationTime(onionName);
			} catch (final NoMoreOnionBinderAddressAvailableException noav) {
				transaction.rollback();
				throw noav;
			}
			/* Porto a commit la transazione. */
			transaction.commit();
			return onionBinder;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.uniroma2.sii.repository.OnionBinderRepositoryCustom#
	 * checkIfOnionBinderExistsWithoutCacheRefreshing(java.lang.String)
	 */
	@Override
	@Transactional
	public boolean checkIfOnionBinderExistsWithoutCacheRefreshing(
			final String onion) {
		final OnionBinder onionBinder = onionBinderRepository
				.findByOnionName(onion);
		return (onionBinder != null);
	}
}
