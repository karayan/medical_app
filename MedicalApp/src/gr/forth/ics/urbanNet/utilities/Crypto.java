package gr.forth.ics.urbanNet.utilities;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Crypto {

	static private MessageDigest md = null;

	public static String bin2hex(byte[] data) {
		return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
	}

	public static String getSHA256(String data) {
		
		if(data == null)
			return null;
		
		if(md == null) try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return bin2hex(md.digest(data.getBytes()));
		
	}

	public static KeyPair getRsa() {

		KeyPairGenerator keyGen;
		try {

			keyGen = KeyPairGenerator.getInstance("RSA");

			keyGen.initialize(1024);

			return keyGen.genKeyPair();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;

	}

}
