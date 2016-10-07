package main;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class AES {
	
	public static SecretKey generateKey(String key) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
	    byte[] passphrase = null;
		try {
			passphrase = digest.digest(key.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    
	    passphrase = Arrays.copyOf(passphrase, 16);
	    return new SecretKeySpec(passphrase, "AES");
    }
	
    public static String encrypt(String message, String key) throws Exception {
    	byte[] data = message.getBytes("UTF-8");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
        byte[] encryptData = cipher.doFinal(data);
        
        // System.out.println(byteArrayToHexString(encryptData));

        return byteArrayToHexString(encryptData);
    }

    public static String decrypt(String v, String key) throws Exception {
    	byte[] tmp = hexStringToByteArray(v);
        SecretKeySpec spec = new SecretKeySpec(generateKey(key).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, spec);
        String result;
        try {
        	result = new String(cipher.doFinal(tmp), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException e) {
        	result = v;
        }
        // System.out.println(result);
        return result;

    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	public static String byteArrayToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length*2];
	    int v;
	
	    for(int j=0; j < bytes.length; j++) {
	        v = bytes[j] & 0xFF;
	        hexChars[j*2] = hexArray[v>>>4];
	        hexChars[j*2 + 1] = hexArray[v & 0x0F];
	    }
	
	    return new String(hexChars);
	}
}