package org.ksa14.webhard.sftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthUtil {
	public static String md5(String input) {
		try {
			byte hash[] = MessageDigest.getInstance("MD5").digest(input.getBytes());
			return String.format("%1$032x", new BigInteger(1, hash));
		} catch (NoSuchAlgorithmException nsae) {
			// This should never happen
			assert false;
			return null;
		}
	}
 
	public static String[] GetKeys(String id) {
		try {
			URL url = new URL("http://webhard.ksa14.org/key.php?id=" + id);
			URLConnection connection = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			
			String publicKey = reader.readLine() + "\n", line;			
			StringBuffer privateKey = new StringBuffer();
			while((line = reader.readLine()) != null) 
				privateKey.append(line + "\n");
			
			if(privateKey.length() == 0)
				return null;
			
			String keys[] = {publicKey, privateKey.toString()};
			return keys;
		} catch (Exception e) {
			return null;
		}
	}
}
