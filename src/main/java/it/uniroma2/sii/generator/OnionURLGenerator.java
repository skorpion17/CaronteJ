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
package it.uniroma2.sii.generator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;

import org.apache.commons.codec.binary.Base32;
import org.bouncycastle.asn1.DERObject;

/**
 * Rappresenta il generatore di indirizzi chiavi RSA ed indirizzo .onion
 * associato.
 *
 * @author Adrea Mayer, Emanuele Altomare
 */
public class OnionURLGenerator {
	private static final String RSA_KEY_GENERATOR = "RSA";
	private static final int KEY_SIZE_IN_BIT = 1024;
	private final KeyPairGenerator keyGenerator;
	private final KeyPair keyPair;
	private final DERObject pubKeyInDer;
	private final DERObject privKeyInDer;
	private final String dirToSavePath;

	/**
	 * Costruttore.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	protected OnionURLGenerator(String dirToSavePath)
			throws NoSuchAlgorithmException, IOException {
		this.dirToSavePath = dirToSavePath;
		/* Ottengo il generatore delle chiavi. */
		keyGenerator = KeyPairGenerator.getInstance(RSA_KEY_GENERATOR);
		/* Inizializza il generatore ponendo il numero di bit 1024 */
		keyGenerator.initialize(KEY_SIZE_IN_BIT);
		/* Ottiene una coppia di chiavi generate: pubblica e privata. */
		keyPair = keyGenerator.genKeyPair();
		/* salvo in fomrato DER le chiavi. */
		pubKeyInDer = OnionURL.toDERObject(keyPair.getPublic().getEncoded());
		privKeyInDer = OnionURL.toDERObject(keyPair.getPrivate().getEncoded());
	}

	/**
	 * Ottiene la chiave pubblica.
	 * 
	 * @return
	 */
	public PublicKey getPublicKey() {
		return keyPair.getPublic();
	}

	/**
	 * Ottiene la chiave privata.
	 * 
	 * @return
	 */
	public PrivateKey getPrivateKey() {
		return keyPair.getPrivate();
	}

	/**
	 * @return the pubKeyInDer
	 */
	public DERObject getPubKeyInDer() {
		return pubKeyInDer;
	}

	/**
	 * @return the privKeyInDer
	 */
	public DERObject getPrivKeyInDer() {
		return privKeyInDer;
	}

	/**
	 * @return the dirToSavePath
	 */
	public String getDirToSavePath() {
		return dirToSavePath;
	}

	/**
	 * Restituisce l'indirizzo .onion.
	 * 
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 */
	public String generateOnionAddress() throws IOException,
			NoSuchAlgorithmException, CertificateException {
		byte[] der = pubKeyInDer.getDEREncoded();

		/* creo il digest SHA1 della chiave pubblica. */
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] digets = md.digest(der);

		/* lo converto in base32 */
		Base32 b32 = new Base32();

		/* prendo i primi 16 caratteri e lo formatto con un indirizzo onion. */
		String onion = String.format("%s.onion", b32.encodeAsString(digets)
				.toLowerCase().substring(0, 16));
		return onion;
	}

	/**
	 * Permette di memorizzare l'indirizzo .onion generato.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public void saveOnionAddress() throws NoSuchAlgorithmException,
			CertificateException, IOException {
		/*
		 * creo il writer.
		 */
		BufferedWriter pubKeyFileWriter = null;
		BufferedWriter privKeyFileWriter = null;
		BufferedWriter onionAddressFileWriter = null;
		Charset charset = Charset.forName("UTF-8");
		Path pubKeyFile = Paths.get(URI.create("file://" + dirToSavePath
				+ "/cert.pub"));
		Path privKeyFile = Paths.get(URI.create("file://" + dirToSavePath
				+ "/cert.key"));
		Path onionAddressFile = Paths.get(URI.create("file://" + dirToSavePath
				+ "/onion.txt"));
		try {
			pubKeyFileWriter = Files.newBufferedWriter(pubKeyFile, charset,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			privKeyFileWriter = Files.newBufferedWriter(privKeyFile, charset,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
			onionAddressFileWriter = Files.newBufferedWriter(onionAddressFile,
					charset, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);

			/* scrivo i vari files */
			pubKeyFileWriter.write(OnionURL.convertToPem(pubKeyInDer));
			privKeyFileWriter.write(OnionURL.convertToPem(privKeyInDer));
			onionAddressFileWriter.write(generateOnionAddress());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pubKeyFileWriter != null) {
				pubKeyFileWriter.close();
			}
			if (privKeyFileWriter != null) {
				privKeyFileWriter.close();
			}
			if (onionAddressFileWriter != null) {
				onionAddressFileWriter.close();
			}
		}
	}
}