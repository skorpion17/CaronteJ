package it.uniroma2.sii.gen;

import it.uniroma2.sii.config.OnionBinderConfig;
import it.uniroma2.sii.model.OnionBinder;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * Generatore di indirizzi per OnionBinder.
 * 
 * @author andrea
 *
 */
@Deprecated
public class OnionBinderGenerator implements IdentifierGenerator {
	public static final String ONION_BINDER_GENERATOR_NAME_SEQUENCE_ID = "sequence_onion_binder_id";
	public static final String ONION_BINDER_GENERATOR_STRATEGY = "it.uniroma2.sii.gen.OnionBinderGenerator";

	@Override
	public Serializable generate(SessionImplementor session, Object obj)
			throws HibernateException {
		final Connection connection = session.connection();
		try {
			final PreparedStatement statement = connection
					.prepareStatement("SELECT MAX(address) AS address FROM OnionBinder");
			final ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				/*
				 * Viene ritornato il massimo degli indirizzi; a questo punto
				 * viene ritornato il massimo degli indirizzi a cui viene
				 * sommato 1. Se gli ultimi 2 byte terminano con 0000, allora si
				 * parte da 1023 = 0x3FF
				 */
				long maxAddress = resultSet
						.getLong(OnionBinder.ONION_BINDER_ADDRESS_FIELD_NAME);
				if (maxAddress < OnionBinderConfig.ONION_BINDER_ADDRESS_4_BYTE_START) {
					/* Non c'è alcun indirizzo valido nel db */
					maxAddress += OnionBinderConfig.ONION_BINDER_ADDRESS_4_BYTE_START;
				}
				// if ((maxAddress & OnionBinderConfig.PORT_MASK) == 0x0000)
				// {
				// /*
				// * gli ultimi 2 byte sono 0000, allora si aggiunge il bias
				// * 0x3ff
				// */
				// maxAddress +=
				// OnionBinderConfig.ONION_BINDER_ADDRESS_PORT_6_BYTE_START;
				// }

				/* Si incrementa l'indirizzo per ottenere il successivo */
				maxAddress += OnionBinderConfig.ONION_BINDER_ADDRESS_INCREMENT;
				System.out.printf(
						"\t >>> Generated OnionBinderAddress: %d <<<\n",
						maxAddress);
				return maxAddress;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}