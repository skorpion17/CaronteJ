/*******************************************************************************
 * Copyright (c) 2015 Emanuele Altomare, Andrea Mayer
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
/**
 * 
 */
package it.uniroma2.sii.util.data.http.exception;

import it.uniroma2.sii.util.data.http.HttpData;

import java.io.IOException;

/**
 * Rappresenta le possibili eccezioni che si possono verificare nel costruire od
 * usare oggetti {@link HttpData}.
 * 
 * @author Emanuele Altomare
 */
public class HttpException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2945418280764711062L;

	/**
	 * Costruisce l'eccezione con un messaggio dato in input.
	 * 
	 * @param message
	 */
	public HttpException(String message) {
		super(message);
	}
}
