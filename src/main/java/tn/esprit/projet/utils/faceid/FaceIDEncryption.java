package tn.esprit.projet.utils.faceid;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Face ID Encryption Utility
 * AES-256-GCM encryption for face embeddings
 */
public class FaceIDEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    // In production, load this from secure key management system
    private static final String MASTER_KEY_B64 = "YourSecure256BitKeyHere1234567890=="; // 32 bytes base64

    private static SecretKey getMasterKey() {
        try {
            // In production, use proper key management (AWS KMS, Azure Key Vault, etc.)
            byte[] keyBytes = MASTER_KEY_B64.getBytes();
            if (keyBytes.length < 32) {
                // Generate a proper key if not set
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(AES_KEY_SIZE);
                return keyGen.generateKey();
            }
            
            // Use first 32 bytes as key
            byte[] key = new byte[32];
            System.arraycopy(keyBytes, 0, key, 0, Math.min(32, keyBytes.length));
            return new SecretKeySpec(key, "AES");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get master key", e);
        }
    }

    /**
     * Encrypt face embedding
     * @return [encryptedData, iv, tag] all base64 encoded
     */
    public static String[] encryptEmbedding(double[] embedding) throws Exception {
        if (embedding == null || embedding.length != 512) {
            throw new IllegalArgumentException("Embedding must be 512D");
        }

        // Convert double[] to byte[]
        ByteBuffer buffer = ByteBuffer.allocate(embedding.length * 8);
        for (double d : embedding) {
            buffer.putDouble(d);
        }
        byte[] plainBytes = buffer.array();

        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        // Encrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKey(), spec);
        
        byte[] ciphertext = cipher.doFinal(plainBytes);

        // Split ciphertext and tag
        // In GCM mode, the tag is appended to the ciphertext
        int ciphertextLength = ciphertext.length - (GCM_TAG_LENGTH / 8);
        byte[] encrypted = new byte[ciphertextLength];
        byte[] tag = new byte[GCM_TAG_LENGTH / 8];
        
        System.arraycopy(ciphertext, 0, encrypted, 0, ciphertextLength);
        System.arraycopy(ciphertext, ciphertextLength, tag, 0, tag.length);

        // Return base64 encoded
        return new String[]{
            Base64.getEncoder().encodeToString(encrypted),
            Base64.getEncoder().encodeToString(iv),
            Base64.getEncoder().encodeToString(tag)
        };
    }

    /**
     * Decrypt face embedding
     */
    public static double[] decryptEmbedding(String encryptedB64, String ivB64, String tagB64) throws Exception {
        // Decode from base64
        byte[] encrypted = Base64.getDecoder().decode(encryptedB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] tag = Base64.getDecoder().decode(tagB64);

        // Combine encrypted data and tag for GCM
        byte[] ciphertext = new byte[encrypted.length + tag.length];
        System.arraycopy(encrypted, 0, ciphertext, 0, encrypted.length);
        System.arraycopy(tag, 0, ciphertext, encrypted.length, tag.length);

        // Decrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec);
        
        byte[] plainBytes = cipher.doFinal(ciphertext);

        // Convert byte[] back to double[]
        ByteBuffer buffer = ByteBuffer.wrap(plainBytes);
        double[] embedding = new double[512];
        for (int i = 0; i < 512; i++) {
            embedding[i] = buffer.getDouble();
        }

        return embedding;
    }
}
