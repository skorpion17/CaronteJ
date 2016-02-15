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
package it.uniroma2.sii.util.transaction.exception;

import java.io.IOException;

/**
 * Eccezione sulla transazione. Questa eccezione è checked.
 * 
 * @author Andrea Mayer Mayer
 *
 */
public class SimpleTransactionException extends IOException {
	private static final long serialVersionUID = -5428425460234419495L;

	public SimpleTransactionException() {
	}

	public SimpleTransactionException(String message) {
		super(message);
	}

	public SimpleTransactionException(Throwable cause) {
		super(cause);
	}

	public SimpleTransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
