package it.uniroma2.sii.util.transaction;

import it.uniroma2.sii.util.transaction.exception.SimpleTransactionException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Transazione programmatica semplificata.
 * 
 * Esempio di utilizzo:
 * 
 * Viene utilizzata quando è necessario bloccare una certa sezione critica ed al
 * suo interno si vuole fare in modo che parta la transazione. Al completamento
 * della transazione viene rilasciata la sezione critica. Dopo la sezione
 * critica si è sicuri che la transazione è completata.
 * 
 * Utilizzando le transazioni di Spring dichiarative essendo queste poste sul
 * metodo non si ha il pieno controllo sulla trasazione avviata. Essa termina
 * solo dopo la chiusura del metodo.
 * 
 * 
 * @author Andrea Mayer Mayer
 *
 */
public class SimpleTransaction extends DefaultTransactionDefinition {
	private static final long serialVersionUID = 6224284148439442861L;
	/**
	 * PlatformTransactionManager di spring.
	 */
	private final PlatformTransactionManager platformTransactionManager;
	/**
	 * La transazione corrente una volta avviata con begin.
	 */
	private TransactionStatus transactionStatus;

	/**
	 * Costruttore privato.
	 * 
	 * @param platformTransactionManager
	 */
	private SimpleTransaction(
			final PlatformTransactionManager platformTransactionManager) {
		this(platformTransactionManager,
				DefaultTransactionDefinition.PROPAGATION_REQUIRED);
	}

	/**
	 * Costruttore privato.
	 * 
	 * @param platformTransactionManager
	 * @param propagationBehavior
	 */
	private SimpleTransaction(
			final PlatformTransactionManager platformTransactionManager,
			int propagationBehavior) {
		super(propagationBehavior);
		this.platformTransactionManager = platformTransactionManager;
	}

	/**
	 * Avvia la transazione.
	 * 
	 * @return
	 */
	public SimpleTransaction open() throws SimpleTransactionException {
		try {
			/* Avvia la transazione e la passa a transactionStatus */
			transactionStatus = platformTransactionManager.getTransaction(this);
			return this;
		} catch (final RuntimeException re) {
			throw new SimpleTransactionException(re);
		}
	}

	/**
	 * Effettua il rollback.
	 * 
	 * @throws SimpleTransactionException
	 */
	public void rollback() throws SimpleTransactionException {
		if (transactionStatus == null) {
			throw new SimpleTransactionException(
					"Transaction has not been starte yet.");
		}
		try {
			platformTransactionManager.rollback(transactionStatus);
		} catch (final RuntimeException e) {
			throw new SimpleTransactionException(e);
		}
	}

	/**
	 * Viene effettuato il commit.
	 * 
	 * @throws SimpleTransactionException
	 */
	public void commit() throws SimpleTransactionException {
		if (transactionStatus == null) {
			throw new SimpleTransactionException(
					"Transaction has not been starte yet.");
		}
		try {
			platformTransactionManager.commit(transactionStatus);
		} catch (final RuntimeException e) {
			throw new SimpleTransactionException(e);
		}
	}

	/**
	 * Ottiene una transazione con propagazione di default
	 * {@link TransactionDefinition#PROPAGATION_REQUIRED}.
	 * 
	 * @param platformTransactionManager
	 * @return
	 */
	public static SimpleTransaction factory(
			final PlatformTransactionManager platformTransactionManager) {
		return factory(platformTransactionManager,
				DefaultTransactionDefinition.PROPAGATION_REQUIRED);
	}

	/**
	 * Ottiene una nuova transazione.
	 * 
	 * @param platformTransactionManager
	 * @param propagationBehavior
	 * @return
	 */
	public static SimpleTransaction factory(
			final PlatformTransactionManager platformTransactionManager,
			int propagationBehavior) {
		/* Creo l'oggetto SimpleTransaction */
		final SimpleTransaction simpleTransaction = new SimpleTransaction(
				platformTransactionManager, propagationBehavior);
		/*
		 * Ritorna l'oggetto simpleTransaction. La transazione deve essere
		 * esplicitamente aperta dall'esterno. In questo modo ho il pieno
		 * controllo sulla transazione stessa.
		 */
		return simpleTransaction;
	}
}
