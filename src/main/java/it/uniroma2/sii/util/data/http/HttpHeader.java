package it.uniroma2.sii.util.data.http;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rappresenta l'header HTTP.
 * 
 * @author Emanuele Altomare
 */
public class HttpHeader {

	private Map<String, String> httpHeaderFields = null;

	private byte[] byteHeader = null;

	/**
	 * Costruttore di default.
	 */
	public HttpHeader() {
		httpHeaderFields = new LinkedHashMap<String, String>();
	}

	/**
	 * Consente di ottenere tutti gli headers presenti.
	 * 
	 * @return
	 */
	public Map<String, String> getHeaders() {
		return httpHeaderFields;
	}

	/**
	 * Permette di ritornare un preciso valore dell'header a partire dal nome
	 * dell'header che funge da chiave.
	 * 
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return httpHeaderFields.get(key);
	}

	/**
	 * Consente di aggiungere headers.
	 * 
	 * @param header
	 * @param value
	 */
	public void set(String header, String value) {
		if (header != null && value != null) {
			httpHeaderFields.put(header, value);
		}
	}

	/**
	 * Consente di inglobare un HttpHeader. Aggiunge gli headers presenti
	 * nell'oggetto {@link HttpHeader} in input all'interno del chiamante.
	 * 
	 * @param headers
	 */
	public void addHeaders(HttpHeader headers) {
		httpHeaderFields.putAll(headers.getHeaders());
	}

	/**
	 * Restituisce l'header in bytes.
	 * 
	 * @return
	 */
	public byte[] getBytes() {
		if (byteHeader == null) {
			byteHeader = toString().getBytes();
		}
		return byteHeader;
	}

	public String toString() {
		String result = "";

		for (Map.Entry<String, String> entry : httpHeaderFields.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			result += String.format("%s: %s\r\n", key, value);
		}

		return result + "\r\n";
	}
}
