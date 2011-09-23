package org.ksa14.webhard.sftp;

import java.io.*;
import java.math.*;
import java.net.*;
import java.security.*;
import java.util.*;

import com.jcraft.jsch.*;

public class SftpUtil {
	public static final String Username = "apache";
	public static final String Host = "webhard.ksa14.org";

	private JSch SUJsch;
	private Session SUSession;
	private Channel SUChannel;
	
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
	
	public SftpUtil() {		
	}
	
	public boolean Connect(String id, String pw) {
		try {
			String[] RSAKeys = GetKeys(id);
			
			SUJsch = new JSch();
			SUJsch.addIdentity("Key", RSAKeys[1].getBytes(), RSAKeys[0].getBytes(), md5(pw).getBytes());
			
			SUSession = SUJsch.getSession(Username, Host, 22);
			Properties SUConf = new Properties();
			SUConf.put("StrictHostKeyChecking", "no");
			SUSession.setConfig(SUConf);
			
			SUSession.connect();
			
			SUChannel = SUSession.openChannel("sftp");
			SUChannel.connect();
		} catch (JSchException e) {
			return false;
		}
		
		return true;
	}
}
