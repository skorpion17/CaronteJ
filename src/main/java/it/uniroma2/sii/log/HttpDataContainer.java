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
package it.uniroma2.sii.log;

import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.response.HttpResponse;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * È un ogetto di utlità che contiene due liste con richieste e risposte
 * appartenenti alla stessa conessione
 * 
 * @author Emanuele Altomare
 */
public class HttpDataContainer {

	/*
	 * indica se un thread worker del logger sta monitorando questo container.
	 */
	private boolean isServed;

	/*
	 * indica se la connessione sta per essere chiusa.
	 */
	private boolean closingConnection;

	private final UUID connectionId;

	Object requestQueueLock;
	private List<HttpRequest> requestQueue;

	Object responseQueueLock;
	private List<HttpResponse> responseQueue;

	public HttpDataContainer(UUID connectionId) {
		this.connectionId = connectionId;
		setServed(false);
		setClosingConnection(false);
		requestQueueLock = new Object();
		responseQueueLock = new Object();
		requestQueue = new LinkedList<HttpRequest>();
		responseQueue = new LinkedList<HttpResponse>();
	}

	public UUID getConnectionId() {
		return connectionId;
	}

	/**
	 * Indica se la coda delle richieste è vuota in maniera thread-safe.
	 * 
	 * @return
	 */
	public boolean requestQueueIsEmpty() {
		synchronized (requestQueueLock) {
			return requestQueue.isEmpty();
		}
	}

	/**
	 * Indica se la coda delle risposte è vuota in maniera thread-safe.
	 * 
	 * @return
	 */
	public boolean responseQueueIsEmpty() {
		synchronized (responseQueueLock) {
			return responseQueue.isEmpty();
		}
	}

	/**
	 * Aggiunge una richiesta in coda in maniera thread-safe.
	 * 
	 * @param request
	 */
	public void pushRequest(HttpRequest request) {
		synchronized (requestQueueLock) {
			requestQueue.add(request);
		}
	}

	/**
	 * Aggiunge una risposta in coda in maniera thread-safe.
	 * 
	 * @param response
	 */
	public void pushResponse(HttpResponse response) {
		synchronized (responseQueueLock) {
			responseQueue.add(response);
		}
	}

	/**
	 * Rimuove una richiesta dalla coda in maniera thread-safe.
	 * 
	 * @return la richiesta rimossa.
	 */
	public HttpRequest popRequest() {
		HttpRequest returnValue = null;
		synchronized (requestQueueLock) {
			if (!requestQueue.isEmpty()) {
				returnValue = requestQueue.get(0);
				requestQueue.remove(0);
			}
		}
		return returnValue;
	}

	/**
	 * Rimuove una risposta dalla coda in maniera thread-safe.
	 * 
	 * @return la risposta rimossa.
	 */
	public HttpResponse popResponse() {
		HttpResponse returnValue = null;
		synchronized (responseQueueLock) {
			if (!responseQueue.isEmpty()) {
				returnValue = responseQueue.get(0);
				responseQueue.remove(0);
			}
		}
		return returnValue;
	}

	/**
	 * Consente di capire se questo container è servito già da un worker del
	 * logger. NON è thread-safe.
	 * 
	 * @return
	 */
	public boolean isServed() {
		return isServed;
	}

	/**
	 * Consente di settare il valore quando un thread worker del logger monitora
	 * il container. NON è thread-safe.
	 * 
	 * @param isServed
	 */
	public void setServed(boolean isServed) {
		this.isServed = isServed;
	}

	/**
	 * Consente di capire se la connessione sta per essere chiusa, il worker che
	 * sta lavorando su questa istanza è in ascolto su questa variabile e se
	 * verifica che è true, termina. NON è thread-safe.
	 * 
	 * @return
	 */
	public boolean isClosingConnection() {
		return closingConnection;
	}

	/**
	 * Consente di impostare la variabile che indica la chiusura della
	 * connessione. NON è thread-safe.
	 * 
	 * @param closingConnection
	 */
	public void setClosingConnection(boolean closingConnection) {
		this.closingConnection = closingConnection;
	}
}