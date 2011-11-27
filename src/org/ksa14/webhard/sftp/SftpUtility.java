package org.ksa14.webhard.sftp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Contains utility features related to Sftp connection.
 * 
 * @author Jongwook
 */
public class SftpUtility {
	public static String md5(String input) {
		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(input.getBytes());
			return String.format("%1$032x", new BigInteger(1, hash));
		} catch (NoSuchAlgorithmException nsae) {
			// This should never happen
			assert false;
			return null;
		}
	}
 
	public static String[] getKeys(String id) {
		try {
			URL url = new URL("http://webhard.ksa14.org/key.php?id=" + id);
			URLConnection con = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
			
			String pubkey = reader.readLine() + "\n", line;			
			StringBuffer pvtkey = new StringBuffer();
			while ((line = reader.readLine()) != null) 
				pvtkey.append(line + "\n");
			
			if (pvtkey.length() == 0)
				return null;
			
			String[] keys = {pubkey, pvtkey.toString()};
			return keys;
		} catch (Exception e) {
			return null;
		}
	}
}
