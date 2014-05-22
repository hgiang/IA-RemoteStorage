package utilities.hash;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.util.encoders.Hex;

public class Sha1Hash {
	
	
	/**
	 * Generate SHA-1 hash value for a String
	 * */
	public BigInteger hashGen(String content){
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(content.getBytes());
			BigInteger hash = new BigInteger(Hex.encode(md.digest()));
			return hash;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
