package org.ksa14.webhard.sftp;

import java.io.*;
import java.math.*;
import java.net.*;
import java.security.*;

public class SftpUtil {
	public SftpUtil() {
		
	}
	
	private static String md5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte enc[] = md.digest(input.getBytes());
			BigInteger hash = new BigInteger(1, enc);
			return hash.toString(16);
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
