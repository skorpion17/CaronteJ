package it.uniroma2.sii.generator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;

/**
 * Generatore di .onion.
 * 
 * @author andrea
 *
 */
public class OnionURLGenerator {
	private static final String RSA_KEY_GENERATOR = "RSA";
	private static final int KEY_SIZE_IN_BIT = 1024;
	private final KeyPairGenerator keyGenerator;
	private final KeyPair keyPair;

	/**
	 * Costruttore.
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public OnionURLGenerator() throws NoSuchAlgorithmException {
		/* Ottengo il generatore delle chiavi. */
		keyGenerator = KeyPairGenerator.getInstance(RSA_KEY_GENERATOR);
		/* Inizializza il generatore ponendo il numero di bit 1024 */
		keyGenerator.initialize(KEY_SIZE_IN_BIT);
		/* Ottiene una coppia di chiavi generate: pubblica e privata. */
		keyPair = keyGenerator.genKeyPair();
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

	public static void main(String[] args) throws NoSuchAlgorithmException,
			IOException, InvalidKeySpecException {
		// try {
		// final OnionURLGenerator g = new OnionURLGenerator();
		// final PublicKey pk = g.getPublicKey();
		// final DERObject derObject = toDERObject(pk.getEncoded());
		// byte[] der = derObject.getDEREncoded();
		//
		// MessageDigest md = MessageDigest.getInstance("SHA-1");
		// byte[] sha = md.digest(der);
		//
		// System.out.println(new Base32().encodeAsString(sha));
		//
		// } catch (final NoSuchAlgorithmException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		final String publicKey = "MIGJAoGBAJ/SzzgrXPxTlFrKVhXh3buCWv2QfcNgncUpDpKouLn3AtPH5Ocys0jEa"
				+ "ZSKdvaiQ62md2gOwj4x61cFNdi05tdQjS+2thHKEm/KsB9BGLSLBNJYY356bupgI5gQozM65E"
				+ "NelfxYlysBjJ52xSDBd8C4f/p9umdzaaaCmzXG/nhzAgMBAAE=";

		byte[] asn1PublickKeyBytes = Base64.decodeBase64(publicKey
				.getBytes("US-ASCII"));

		final RSAPublicKeyStructure ans1PublicKey = new RSAPublicKeyStructure(
				(ASN1Sequence) ASN1Sequence.fromByteArray(asn1PublickKeyBytes));
		
		byte[] der = ans1PublicKey.getDEREncoded();

//		MessageDigest md = MessageDigest.getInstance("SHA-1");
//		byte[] digets = md.digest(der);
//		Base32 b32 = new Base32();
//		System.out.println(b32.encodeAsString(digets).toLowerCase()
//				.substring(0, 16));

		// RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
		// ans1PublicKey.getModulus(), ans1PublicKey.getPublicExponent());
		// KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		// PublicKey pp = keyFactory.generatePublic(keySpec);

		

		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] digets = md.digest(der);
		Base32 b32 = new Base32();
		System.out.println(b32.encodeAsString(digets).toLowerCase()
				.substring(0, 16));

		// DERObject derObj = toDERObject(publicKey.getBytes());
		// byte[] der = derObj.getDEREncoded();
		//
		// MessageDigest md = MessageDigest.getInstance("SHA-1");
		// byte[] sha = md.digest(der);
		// byte[] half = new byte[10];
		// System.arraycopy(sha, 0, half, 0, 10);
		// Base32 b32 = new Base32();
		// System.out.println(b32.encodeAsString(half));

	}

	private static DERObject toDERObject(byte[] data) throws IOException {
		ByteArrayInputStream inStream = new ByteArrayInputStream(data);
		ASN1InputStream asnInputStream = new ASN1InputStream(inStream);

		return asnInputStream.readObject();
	}
}
