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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Factory per i {@link OnionURLGenerator}.
 * 
 * @author Andrea Mayer Mayer, Emanuele Altomare
 *
 */
@Component
public class OnionURL {
	@Value("${onion.generator.dir.path}")
	public String dirToSavePath;

	/**
	 * Costruttore.
	 */
	public OnionURL() {
	}

	public static void main(String[] args) throws NoSuchAlgorithmException,
			IOException, InvalidKeySpecException, CertificateException {
		// final String publicKey =
		// "MIGJAoGBAJ/SzzgrXPxTlFrKVhXh3buCWv2QfcNgncUpDpKouLn3AtPH5Ocys0jEa"
		// +
		// "ZSKdvaiQ62md2gOwj4x61cFNdi05tdQjS+2thHKEm/KsB9BGLSLBNJYY356bupgI5gQozM65E"
		// + "NelfxYlysBjJ52xSDBd8C4f/p9umdzaaaCmzXG/nhzAgMBAAE=";
		//
		// byte[] asn1PublickKeyBytes = Base64.decodeBase64(publicKey
		// .getBytes("US-ASCII"));
		//
		// final RSAPublicKeyStructure ans1PublicKey = new
		// RSAPublicKeyStructure(
		// (ASN1Sequence) ASN1Sequence.fromByteArray(asn1PublickKeyBytes));
		//
		// byte[] der = ans1PublicKey.getDEREncoded();
		//
		// MessageDigest md = MessageDigest.getInstance("SHA-1");
		// byte[] digets = md.digest(der);
		// Base32 b32 = new Base32();
		// System.out.println(b32.encodeAsString(digets).toLowerCase()
		// .substring(0, 16));
		OnionURLGenerator generator = new OnionURLGenerator("/tmp");
		System.out.println("ONION ADDRESS:");
		System.out.println(generator.generateOnionAddress());
		System.out.println("PUBLIC KEY:");
		System.out.println(convertToPem(generator.getPubKeyInDer()));
		System.out.println("PRIVATE KEY:");
		System.out.println(convertToPem(generator.getPrivKeyInDer()));
	}

	/**
	 * Consente di creare un {@link OnionURLGenerator}.
	 * 
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public OnionURLGenerator createOnionURLGenerator()
			throws NoSuchAlgorithmException, IOException {
		return new OnionURLGenerator(dirToSavePath);
	}

	/**
	 * Converte un array di bytes in un {@link DERObject}.
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public static DERObject toDERObject(byte[] data) throws IOException {
		ByteArrayInputStream inStream = null;
		ASN1InputStream asnInputStream = null;
		try {
			inStream = new ByteArrayInputStream(data);
			asnInputStream = new ASN1InputStream(inStream);
			return asnInputStream.readObject();
		} finally {
			if (asnInputStream != null) {
				asnInputStream.close();
			}
			if (inStream != null) {
				inStream.close();
			}
		}
	}

	/**
	 * Converte un {@link DERObject} in una stringa contenente il certificato in
	 * formato PEM.
	 * 
	 * @param cert
	 * @return
	 * @throws CertificateEncodingException
	 */
	public static String convertToPem(DERObject cert)
			throws CertificateEncodingException {
		Base64 encoder = new Base64(64);
		String BEGIN = "-----BEGIN CERTIFICATE-----\n";
		String END = "-----END CERTIFICATE-----";

		byte[] derCert = cert.getDEREncoded();
		String pemCertPre = new String(encoder.encode(derCert));
		String pemCert = String.format("%s%s%s", BEGIN, pemCertPre, END);
		return pemCert;
	}
}
