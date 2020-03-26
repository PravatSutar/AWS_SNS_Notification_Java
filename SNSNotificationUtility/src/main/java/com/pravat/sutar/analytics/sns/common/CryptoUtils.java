package com.pravat.sutar.analytics.sns.common;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
	private static final String aesEncryptAlgo = "AES";
    private static final String charEncoding = "UTF-8";
    private static final String cipherTransform = "AES/CBC/PKCS5PADDING";    
    private static final String encKey = "@@@@@#$rtSSST";
    
    /**
     * Method for Encrypt Plain String Data which returns an encrypted string.
     * 
     * @param text
     * @return encryptedText
     */
    public static String encrypt(String text) {
        String encryptedText = "";
        try {
            Cipher cipher   = Cipher.getInstance(cipherTransform);
            byte[] key      = encKey.getBytes(charEncoding);
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptAlgo);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            byte[] cipherText = cipher.doFinal(text.getBytes("UTF8"));
            Base64.Encoder encoder = Base64.getEncoder();
            encryptedText = encoder.encodeToString(cipherText);
            logger.info("Encrypted String: "+encryptedText);

        } catch (Exception e) {
             logger.error("Exception while encrypting the string: "+e.getMessage());
        }
        return encryptedText;
    }

    /**
     * Method decrypt which takes encrypted text string as a parameter and returns the 
     * decrypted/original string.
     * 
     * @param encText
     * @return decryptedText
     */
    public static String decrypt(String encText) {
        String decryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance(cipherTransform);
            byte[] key = encKey.getBytes(charEncoding);
            SecretKeySpec secretKey = new SecretKeySpec(key, aesEncryptAlgo);
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] cipherText = decoder.decode(encText.getBytes("UTF8"));
            decryptedText = new String(cipher.doFinal(cipherText), "UTF-8");
            logger.info("Decrypted String: "+decryptedText);

        } catch (Exception e) {
        	  logger.error("Exception while descrypting the string:"+e.getMessage());
        }
        return decryptedText;
    }
    
    /**
     * Main method for testing manually
     * 
     * @param args
     */
    public static void main(String[] args) {
       
        String plainString = "pravathadoop12#";
        String encyptStr   = encrypt(plainString);
        String decryptStr  = decrypt(encyptStr);
        
        System.out.println("Plain   String: "+plainString);
        System.out.println("Encrypted String: "+encyptStr);
        System.out.println("Decrypted String: "+decryptStr);
        
    }     
	
}
