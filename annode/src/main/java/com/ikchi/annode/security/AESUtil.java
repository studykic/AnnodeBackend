package com.ikchi.annode.security;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class AESUtil {

    private static final String ALGORITHM = "AES";


    private static final int KEYLENGTH = 16;

    public static String encrypt(String value, String email) {

        try {
            String key = padEmail(email);
            Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static String decrypt(String value, String email) throws Exception {
        String key = padEmail(email);
        Key secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedValue = cipher.doFinal(Base64.getDecoder().decode(value));
        return new String(decryptedValue, StandardCharsets.UTF_8);
    }


    // 이메일을 KEYLENGTH에 맞게 조절
    private static String padEmail(String email) {

        // 이메일이 너무 짧을경우 길에에 맞춰 채워주기
        if (email.length() < KEYLENGTH) {
            StringBuilder paddedEmail = new StringBuilder(email);
            while (paddedEmail.length() < KEYLENGTH) {
                paddedEmail.append("*");
            }
            return paddedEmail.toString().substring(0, KEYLENGTH);
        }
        return email.substring(0, KEYLENGTH);
    }
}
