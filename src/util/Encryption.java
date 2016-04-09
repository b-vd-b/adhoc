package util;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.util.Base64;

public class Encryption {

    private static final String ALGORITHM = "RSA";
    private Cipher cipher;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Encryption() {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance(ALGORITHM).generateKeyPair();
            publicKey = keyPair.getPublic();
            privateKey = keyPair.getPrivate();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            cipher = Cipher.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String encryptMessage(String message, PublicKey key) {
        //Encrypt using the key given
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedMessage = cipher.doFinal(message.getBytes());
            return new String(Base64.getEncoder().encode(encryptedMessage));
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        System.out.println("Not supposed to get here");
        return null;
    }

    public String decryptMessage(String message) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(message.getBytes()));
            return new String(decryptedMessage);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        System.out.println("Not supposed to get here");
        return null;
    }

    public static void main(String[] args) {
        String message = "Dit is een test";
        Encryption encryption = new Encryption();
        System.out.println("Message: " + message);
        String encryptedMessage = encryption.encryptMessage(message, encryption.getPublicKey());
        System.out.println("Encrypted message: " + encryptedMessage);
        System.out.println("Decrypted message: " + encryption.decryptMessage(encryptedMessage));
    }
}
