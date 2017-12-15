package com.efraespada.stringcarelibrary;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by efrainespada on 02/10/2016.
 */

public class SC {

    private static final int LENGTH = 16;
    private static final String CODIFICATION = "UTF-8";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    private static final String TAG = SC.class.getSimpleName();
    private static Context context;

    public static void init(Context c) {
        context = c;
    }

    private static String getCertificateSHA1Fingerprint() {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        InputStream input = new ByteArrayInputStream(cert);
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        String hexString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(c.getEncoded());
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException | CertificateEncodingException e1) {
            e1.printStackTrace();
        }
        return hexString;
    }

    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }

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
    public static String getString(int id) {
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

}
