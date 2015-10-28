package it.uniroma2.sii.util.data.http;

import it.uniroma2.sii.util.data.http.exception.HttpException;
import it.uniroma2.sii.util.data.http.exception.HttpMalformedHeaderException;
import it.uniroma2.sii.util.data.http.exception.HttpMalformedResponseStatusCodeException;
import it.uniroma2.sii.util.data.http.exception.HttpNullStartLineException;
import it.uniroma2.sii.util.data.http.exception.HttpVersionNotSupportedException;
import it.uniroma2.sii.util.data.http.request.HttpRequest;
import it.uniroma2.sii.util.data.http.request.HttpRequestLine;
import it.uniroma2.sii.util.data.http.response.HttpResponse;
import it.uniroma2.sii.util.data.http.response.HttpResponseStatusLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe di utilità.
 * 
 * @author Emanuele Altomare
 */
public class HttpUtils {

	public static final byte CR = (byte) 0x0d;
	public static final byte LF = (byte) 0x0a;

	/*
	 * utile per indicare ai metodi statici come valutare la start-line.
	 */
	public enum HttpPacketType {
		REQUEST, RESPONSE
	}

	/**
	 * Restituisce il metodo della richiesta avente per start-line quella
	 * passata in input. È utile anche per capire se la richiesta è valida.
	 * 
	 * @param startLine
	 * @return il metodo della richiesta se quest'ultima è una richiesta valida
	 *         ed ha un metodo supportato, null altrimenti.
	 * @throws HttpException
	 */
	public static String getSupportedRequestMethodByStartLine(String startLine)
			throws HttpException {

		if (startLine == null) {
			throw new HttpException("Start line is null.");
		}

		/*
		 * prendo la possibile request-line e separo i vari elementi in un array
		 * di stringhe.
		 */
		String[] elements = startLine.split(" ");

		/*
		 * prendo il primo elemento che forse contiene un metodo supportato.
		 */
		String httpSupportedRequestMethod = elements[0].trim();

		/*
		 * qui separo le cose per maggiore leggibilità, avrei potuto usare uno
		 * switch su stringa, ma ho evitato per mantenere la compatibilità con
		 * le versioni di java precedenti alla 7.
		 */
		do {
			if (httpSupportedRequestMethod.equals("OPTIONS")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("GET")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("HEAD")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("POST")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("PUT")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("DELETE")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("TRACE")) {
				break;
			}
			if (httpSupportedRequestMethod.equals("CONNECT")) {
				break;
			}

			/*
			 * se sono arrivato qui vuol dire che non è una request-line, oppure
			 * lo è ma presenta un metodo non supportato, quindi metto a null.
			 */

			httpSupportedRequestMethod = null;

		} while (false);

		return httpSupportedRequestMethod;
	}

	/**
	 * Consente di creare un header HTTP a partire dall'input stream.
	 * 
	 * @param inputStream
	 * @return
	 */
	public static HttpHeader createHttpHeader(InputStream inputStream)
			throws IOException {
		if (inputStream == null) {
			throw new IOException("Input stream is null!");
		}

		HttpHeader headers = new HttpHeader();

		byte[] lineBytes;
		String line = "";

		/*
		 * finchè ho linee da leggere...
		 */
		while ((lineBytes = readLineBytesFromInputStream(inputStream)) != null) {
			line = new String(lineBytes, "ASCII");

			String[] couple;

			/*
			 * divido in due la linea dell'header in base al primo carattere di
			 * due punti che trovo.
			 */
			couple = line.split("\\:", 2);

			if (couple.length != 2) {
				throw new HttpMalformedHeaderException(line);
			}

			headers.set(couple[0].trim(), couple[1].trim());
		}

		return headers;
	}

	/**
	 * Consente di leggere una linea dall'oggetto {@link InputStream} in input.
	 * La linea finisce solo se si incontrano i caratteri \r\n (CRLF) in
	 * sequenza.
	 * 
	 * 
	 * @param inputStream
	 * @return i bytes della linea oppure null se la linea è vuota oppure
	 *         l'input stream non ha byte da leggere.
	 * @throws IOException
	 */
	public static byte[] readLineBytesFromInputStream(InputStream inputStream)
			throws IOException {
		if (inputStream == null) {
			throw new IOException("Input stream is null!");
		}

		byte[] lineBytes;

		ByteArrayOutputStream lineOutputStream = new ByteArrayOutputStream();
		byte bite;
		boolean isAfterCr = false;
		while ((bite = (byte) inputStream.read()) != (byte) -1) {
			/*
			 * è CR forse dopo ci sarà LF, vado avanti.
			 */
			if (bite == CR) {
				isAfterCr = true;
				continue;
			}

			/*
			 * se viene dopo CR, sono arrivato in fondo alla linea, esco.
			 */
			if (bite == LF && isAfterCr) {
				break;
			}

			/*
			 * qui non è LF, quindi lo scrivo.
			 */
			lineOutputStream.write(bite);
		}
		lineOutputStream.flush();

		/*
		 * se il buffer è vuoto la linea è vuota oppure l'input stream è vuoto,
		 * quindi assegno null, altrimenti assegno i bytes che rappresentano la
		 * linea.
		 */
		lineBytes = lineOutputStream.size() != 0 ? lineOutputStream
				.toByteArray() : null;

		lineOutputStream.close();
		return lineBytes;
	}

	/**
	 * Consente di estrapolare il body HTTP dall'input stream, questa funzione
	 * compie le operazioni standard che vanno bene sia per la richiesta che per
	 * la risposta.
	 * 
	 * @param data
	 * @param inputStream
	 * @return i bytes del body oppure null se non esiste un body.
	 * @throws IOException
	 */
	public static byte[] createHttpBody(HttpData data, InputStream inputStream)
			throws IOException {
		byte[] body = null;
		if (inputStream == null) {
			throw new IOException("The input stream is null.");
		}
		if (data == null) {
			throw new HttpException("data in input is null.");
		}

		String transferEncoding = data.getHeader().get("Transfer-Encoding");
		String contentLength = data.getHeader().get("Content-Length");

		do {

			/*
			 * se l'header Transfer-Encoding è settato ad un valore diverso da
			 * "identity"...
			 */
			if ((transferEncoding != null && !transferEncoding.isEmpty())
					&& !transferEncoding.equals("identity")) {

				/*
				 * devo usare la modalità chunked per ricavare il body.
				 */
				body = makeBodyChunked(data, inputStream);
				break;
			}

			/*
			 * se è settato l'header Content-Length...
			 */
			if (contentLength != null && !contentLength.isEmpty()) {

				/*
				 * uso quel valore per ricavare il body.
				 */
				body = makeBodyContentLength(contentLength, inputStream);
				break;
			}
		} while (false);

		return body;
	}

	/**
	 * Consente di estrapolare il body della richiesta HTTP dall'input stream.
	 * 
	 * @param httpRequest
	 * @param inputStream
	 * @return i bytes del body oppure null se il body non è presente.
	 * @throws IOException
	 */
	public static byte[] createHttpRequestBody(HttpRequest httpRequest,
			InputStream inputStream) throws IOException {
		byte[] body = null;

		if (inputStream == null) {
			throw new IOException("The input stream is null.");
		}
		if (httpRequest == null) {
			throw new HttpException("data in input is null.");
		}

		HttpRequestLine requestLine = (HttpRequestLine) httpRequest
				.getStartLine();

		if (requestLine == null) {
			throw new HttpException("The request line is null!");
		}

		do {

			/*
			 * se il metodo è uno che non prevede un body...
			 */
			if (!requestLine.getMethod().equals("PUT")
					|| !requestLine.getMethod().equals("POST")) {

				/*
				 * esco.
				 */
				break;
			}

			body = createHttpBody(httpRequest, inputStream);

		} while (false);

		return body;
	}

	/**
	 * Consente di estrapolare il body della risposta HTTP dall'input stream.
	 * 
	 * @param httpResponse
	 * @param inputStream
	 * @return i bytes del body oppure null se il body non è presente.
	 * @throws IOException
	 */
	public static byte[] createHttpResponseBody(HttpResponse httpResponse,
			InputStream inputStream) throws IOException {
		byte[] body = null;

		if (inputStream == null) {
			throw new IOException("The input stream is null.");
		}
		if (httpResponse == null) {
			throw new HttpException("data in input is null.");
		}

		HttpResponseStatusLine statusLine = (HttpResponseStatusLine) httpResponse
				.getStartLine();

		if (statusLine == null) {
			throw new HttpException("The status line is null!");
		}

		int statusCode = statusLine.getStatusCode();

		do {

			/*
			 * se la risposta non deve avere un body...
			 */
			if ((statusCode >= 100 && statusCode < 200) || (statusCode == 204)
					|| (statusCode == 304)) {
				break;
			}

			body = createHttpBody(httpResponse, inputStream);

		} while (false);

		return body;
	}

	/**
	 * Consente di ottenere il body usando l'header Content-Length per ottenre
	 * il numero di bytes da leggere.
	 * 
	 * @param contentLength
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private static byte[] makeBodyContentLength(String contentLength,
			InputStream inputStream) throws IOException {

		byte[] body = null;

		if (inputStream == null) {
			throw new IOException("The input stream is null.");
		}

		if (contentLength == null) {
			throw new IOException("Content-Length is null.");
		}

		/*
		 * se il valore dell'header Content-Length è vuoto oppure il valore non
		 * è un intero...
		 */

		if (contentLength.isEmpty() && !contentLength.matches("[0-9]*")) {

			/*
			 * lancio l'eccezione.
			 */
			throw new HttpException("Content-Length value is malformed.");
		}

		/*
		 * trasformo l'intero.
		 */
		int size = Integer.parseInt(contentLength);
		body = new byte[size];

		/*
		 * leggo dall'input stream i bytes del body
		 */
		for (int i = 0; i < size; ++i) {
			body[i] = (byte) inputStream.read();
		}

		return body;
	}

	/**
	 * Consente di ottenere il body nel caso il formato di transfer encoding sia
	 * chunked.
	 * 
	 * @param inputStream
	 * @return i bytes del body oppure null se il body non è presente.
	 * @throws IOException
	 */
	private static byte[] makeBodyChunked(HttpData data, InputStream inputStream)
			throws IOException {
		byte[] body = null;

		if (inputStream == null) {
			throw new IOException("The input stream is null.");
		}

		/*
		 * creo l'oggetto che conterrà i bytes del body e la stringa e l'array
		 * che conterranno la size del chunk attuale.
		 */
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String sizeChunkString = null;
		byte[] sizeChunkBytes = null;

		while (true) {

			/*
			 * leggo la prima linea che è relativa al chunk-size, che indica di
			 * quanti byte è composto il chunk-data senza considerare i
			 * caratteri CRLF finali.
			 */
			sizeChunkBytes = readLineBytesFromInputStream(inputStream);

			/*
			 * se quella che dovrebbe essere la chunk-size è una linea vuota,
			 * vuol dire che non c'è nessun chunk.
			 */
			if (sizeChunkBytes == null) {
				throw new HttpException(
						"The body is empty when header Transfer-Encoding is set.");
			}

			/*
			 * li scrivo subito.
			 */
			outputStream.write(sizeChunkBytes);

			/*
			 * riaggiungo i caratteri di fine linea eliminati in lettura.
			 */
			outputStream.write(new byte[] { CR, LF });

			/*
			 * genero la stringa a partire dai bytes letti.
			 */
			sizeChunkString = new String(sizeChunkBytes, "ASCII");

			/*
			 * se la Stringa che rappresenta il chunk-size è vuota oppure non è
			 * un valore esadecimale lancio un'eccezione.
			 */
			if (sizeChunkString == null || sizeChunkString.isEmpty()
					|| !sizeChunkString.matches("[0-9a-fA-F]*")) {
				throw new HttpException("Error in chunk size -> "
						+ sizeChunkString);
			}

			/*
			 * trasformo il valore esadecimale in un intero.
			 */
			int sizeChunk = Integer.parseInt(sizeChunkString, 16);

			/*
			 * se la dimensione del chunk è zero...
			 */
			if (sizeChunk == 0) {

				/*
				 * ho letto tutti i chunk quindi interrompo il ciclo.
				 */
				break;
			}

			/*
			 * copio tutti i byte specificati dalla size-chunk più i caratteri
			 * CRLF di fine linea.
			 */
			for (int i = 0; i < sizeChunk + 2; ++i) {
				outputStream.write(inputStream.read());
			}
		}

		/*
		 * leggo eventuali headers aggiunti in fondo dopo il body e li aggiungo
		 * agli altri nell'oggetto HttpHeader di data.
		 */
		data.getHeader().addHeaders(createHttpHeader(inputStream));

		/*
		 * aggiungo una linea vuota per dire che il body è finito.
		 */
		outputStream.write(new byte[] { CR, LF });
		outputStream.flush();

		/*
		 * setto il body e chiudo lo stream.
		 */
		body = outputStream.toByteArray();
		outputStream.close();

		return body;
	}

	/**
	 * Consente di ottenere, a partire dalla start-line e dalla posizione della
	 * parte relativa al protocollo nella start-line, la versione del protocollo
	 * Http presente nel pacchetto HTTP, se è fra quelli supportati, oppure null
	 * se non lo è. Utile per testare se il pacchetto HTTP è valido ed usa una
	 * versione del protocollo supportata.
	 * 
	 * @param startLine
	 * @param httpPacketType
	 *            il tipo di pacchetto HTTP.
	 * @return la versione del protocollo Http se è supportato.
	 * @throws HttpNullStartLineException
	 *             se la start-line in input risulta essere nulla.
	 * @throws HttpVersionNotSupportedException
	 *             se il protocollo non è supportato o è in un formato non
	 *             comprensibile.
	 */
	public static String getSupportedHttpVersionByStartLineString(
			String startLine, HttpPacketType httpPacketType)
			throws HttpNullStartLineException, HttpVersionNotSupportedException {

		if (startLine == null) {
			throw new HttpNullStartLineException("The start-line is null!");
		}
		int httpVersionStringPositionOnStartLine;
		String httpVersion = null;

		/*
		 * splitto la linea in elementi.
		 */
		String[] elements = startLine.split(" ");

		/*
		 * se gli elementi sono meno di 3...
		 */
		if (elements.length < 3) {

			/*
			 * quella in uso non è una versione di protocollo HTTP supportata,
			 * poichè in HTTP/1.[01] la start-line deve avere 3 campi. Lancio
			 * un'eccezione.
			 */
			throw new HttpVersionNotSupportedException(
					"The elements of start-line are less then expected.");
		}

		/*
		 * assegno la posizione del campo realativo al protocollo HTTP sulla
		 * start-line in base al tipo di pacchetto HTTP.
		 */
		switch (httpPacketType) {
		case REQUEST:

			/*
			 * l'elemento che si riferisce al protocollo è il terzo.
			 */
			httpVersionStringPositionOnStartLine = 2;
			break;
		default:
		case RESPONSE:

			/*
			 * l'elemento che si riferisce al protocollo è il primo.
			 */
			httpVersionStringPositionOnStartLine = 0;
		}

		/*
		 * prendo l'elemento della start-line che forse è relativo ad un
		 * protocollo HTTP supportato.
		 */
		String aMaybeSupportedHttpVersion = elements[httpVersionStringPositionOnStartLine]
				.trim();

		/*
		 * se non è HTTP/1.0 oppure HTTP/1.1...
		 */
		if (!aMaybeSupportedHttpVersion.matches("HTTP/1\\.[01]")) {

			/*
			 * lancio un'eccezione.
			 */
			throw new HttpVersionNotSupportedException(
					aMaybeSupportedHttpVersion);
		}
		httpVersion = aMaybeSupportedHttpVersion;
		return httpVersion;
	}

	/**
	 * Consente di ottenere il codice di stato della risposta a partire dalla
	 * start-line.
	 * 
	 * @param startLine
	 * @return
	 * @throws HttpNullStartLineException
	 *             se la start-line in input risulta essere nulla.
	 * @throws HttpMalformedResponseStatusCodeException
	 *             se il codice di stato non è corretto.
	 */
	public static int getValidResponseStatusCodeByStartLine(String startLine)
			throws HttpNullStartLineException,
			HttpMalformedResponseStatusCodeException {

		if (startLine == null) {
			throw new HttpNullStartLineException("The start-line is null!");
		}

		/*
		 * suddivido la linea nei sui elementi.
		 */
		String[] elements = startLine.split(" ");

		/*
		 * prendo l'elemento che dovrebbe contentere lo status-code.
		 */
		String maybeACorrectStatusCode = elements[1].trim();

		/*
		 * se non è uno status-code valido...
		 */
		if (!maybeACorrectStatusCode.matches("[1-5][0-9][0-9]")) {

			/*
			 * lancio un'eccezione.
			 */
			throw new HttpMalformedResponseStatusCodeException(
					maybeACorrectStatusCode);
		}

		/*
		 * trasformo la stringa in un intero e assegno il codice.
		 */
		return Integer.parseInt(maybeACorrectStatusCode);
	}
}
