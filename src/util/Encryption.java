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

    public String encryptMessage(String message, PublicKey key) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //Encrypt using the key given
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessage = cipher.doFinal(message.getBytes());
        return new String(Base64.getEncoder().encode(encryptedMessage));
    }

    public String decryptMessage(String message) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(message.getBytes()));
        return new String(decryptedMessage);
    }
}
