package com.stringcare.library;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.StringRes;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by efrainespada on 02/10/2016.
 */

public class SC {

    private static final int LENGTH = 16;
    private static Class buildConfig;
    private static final String CODIFICATION = "UTF-8";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    private static final String TAG = SC.class.getSimpleName();
    private static Context context;

    public static void initOnModule(Context c, Class bc) {
        context = c;
        buildConfig = bc;
    }

    private static String getCertificateSHA1Fingerprint() {
        return getKey(buildConfig);
    }

    private static native String getKey(Object buildConfig);

    private static SecretKey generateKey(String key) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] passphrase = null;
        try {
            passphrase = digest.digest(key.getBytes(CODIFICATION));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        passphrase = Arrays.copyOf(passphrase, 16);
        return new SecretKeySpec(passphrase, "AES");
    }

    private static String encrypt(String message, String key) throws Exception {
        byte[] data = message.getBytes(CODIFICATION);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(key));
        byte[] encryptData = cipher.doFinal(data);

        return byteArrayToHexString(encryptData);
    }

    private static String decrypt(String message, String key) throws Exception {
        byte[] tmp = hexStringToByteArray(message);
        SecretKeySpec spec = new SecretKeySpec(generateKey(key).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, spec);

        String result = new String(cipher.doFinal(tmp), CODIFICATION);
        return result;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2){
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length*2];
        int v;

        for(int j=0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j*2] = hexArray[v>>>4];
            hexChars[j*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    /**
     * returns a decrypted value for the given string resource
     * @param id
     * @return String
     */
    public static String getString(@StringRes int id) {
        if (context == null) {
            Log.e(TAG, "Library not initialized: SC.init(Context)");
            return null;
        }

        String hash = getCertificateSHA1Fingerprint();
        try {
            return decrypt(context.getString(id), hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return context.getString(id); // returns original value, maybe not encrypted
    }

    public static String getString(@StringRes int id, Object... formatArgs) {
        String value = getString(id);
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = Resources.getSystem().getConfiguration().locale;
        }
        return String.format(locale, value, formatArgs);
    }

    /**
     * encrypts the given value
     * @param value
     * @return String
     */
    public static String encryptString(String value) {
        if (context == null) {
            Log.e(TAG, "Library not initialized: SC.init(Context)");
            return null;
        }

        String hash = getCertificateSHA1Fingerprint();
        try {
            return encrypt(value, hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * decrypts the given encrypted value
     * @param value
     * @return String
     */
    public static String decryptString(String value) {
        if (context == null) {
            Log.e(TAG, "Library not initialized: SC.init(Context)");
            return null;
        }

        String hash = getCertificateSHA1Fingerprint();
        try {
            return decrypt(value, hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        System.loadLibrary("malacaton-lib");
    }

}
