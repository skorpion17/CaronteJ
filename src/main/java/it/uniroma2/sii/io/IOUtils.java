package it.uniroma2.sii.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Utilities per I/O.
 * 
 * @author andrea
 *
 */
public class IOUtils {

	private IOUtils() {

	}

	/**
	 * Chiude la socket {@code socket} in modo aggraziato.
	 * 
	 * @param socket
	 */
	public static void closeQuitely(final Socket socket) {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Chiude l'outputStream {@code outputStream} in modo aggraziato.
	 * 
	 * @param outputStream
	 */
	public static void closeQuitely(final OutputStream outputStream) {
		/* Si chiudono gli stream */
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Chiude l'outputStream {@code inputStream} in modo aggraziato.
	 * 
	 * @param inputStream
	 */
	public static void closeQuitely(final InputStream inputStream) {
		/* Si chiudono gli stream */
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
