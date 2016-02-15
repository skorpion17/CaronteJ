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
package it.uniroma2.sii.repository.exception;

import java.io.IOException;

/**
 * Questa eccezione notifica che sono terminati gli indirizzi usati per
 * costruire la tabella di mapping tra .onion e indirizzi privati.
 * 
 * @author Andrea Mayer
 *
 */
public class NoMoreOnionBinderAddressAvailableException extends IOException {
	private static final long serialVersionUID = 8857020505939584179L;

	public NoMoreOnionBinderAddressAvailableException() {
	}

	public NoMoreOnionBinderAddressAvailableException(String message) {
		super(message);
	}

	public NoMoreOnionBinderAddressAvailableException(Throwable cause) {
		super(cause);
	}

	public NoMoreOnionBinderAddressAvailableException(String message,
			Throwable cause) {
		super(message, cause);
	}
}
