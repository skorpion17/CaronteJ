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
package it.uniroma2.sii.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Utilities per I/O.
 * 
 * @author Andrea Mayer
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

	/**
	 * Chiude la serverSocket {@code serverSocket} in modo aggraziato.
	 * 
	 * @param serverSocket
	 */
	public static void closeQuitely(ServerSocket serverSocket) {
		/* Si chiude la ServerSocket */
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
