package tn.esprit.projet.security.faceid;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM Encryption for Face Embeddings
 * Professional-grade encryption with authentication
 */
public class EmbeddingEncryption {

    private static final String ENCRYPTION_KEY = 
        "2cd597a8f34bd09303bf8b0a3a24428de34f86d8b8b9df1d3d8e89c638488cd5";
    
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 16;

    /**
     * Encrypted embedding container
     */
    public static class EncryptedData {
        public final String encryptedB64;
        public final String ivB64;
        public final String tagB64;

        public EncryptedData(String encrypted, String iv, String tag) {
            this.encryptedB64 = encrypted;
            this.ivB64 = iv;
            this.tagB64 = tag;
        }
    }

    /**
     * Encrypt a 512D embedding vector
     */
    public static EncryptedData encrypt(double[] embedding) throws Exception {
        // Convert double[] to byte[]
        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 8);
        for (double value : embedding) {
            buffer.putDouble(value);
        }
        byte[] data = buffer.array();

        // Derive 256-bit key from secret
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8));

        // Generate random IV
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // Encrypt with AES-256-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, 
                   new SecretKeySpec(key, "AES"), 
                   new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        
        byte[] encrypted = cipher.doFinal(data);

        // Split encrypted data and authentication tag
        int ciphertextLength = encrypted.length - 16;
        byte[] ciphertext = Arrays.copyOf(encrypted, ciphertextLength);
        byte[] tag = Arrays.copyOfRange(encrypted, ciphertextLength, encrypted.length);

        return new EncryptedData(
            Base64.getEncoder().encodeToString(ciphertext),
            Base64.getEncoder().encodeToString(iv),
            Base64.getEncoder().encodeToString(tag)
        );
    }

    /**
     * Decrypt an encrypted embedding
     */
    public static double[] decrypt(String encryptedB64, String ivB64, String tagB64) throws Exception {
        byte[] ciphertext = Base64.getDecoder().decode(encryptedB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] tag = Base64.getDecoder().decode(tagB64);

        // Derive key
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8));

        // Combine ciphertext and tag
        byte[] combined = new byte[ciphertext.length + tag.length];
        System.arraycopy(ciphertext, 0, combined, 0, ciphertext.length);
        System.arraycopy(tag, 0, combined, ciphertext.length, tag.length);

        // Decrypt
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, 
                   new SecretKeySpec(key, "AES"), 
                   new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        
        byte[] decrypted = cipher.doFinal(combined);

        // Convert byte[] back to double[]
        ByteBuffer buffer = ByteBuffer.wrap(decrypted);
        double[] embedding = new double[decrypted.length / 8];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = buffer.getDouble();
        }

        return embedding;
    }
}
