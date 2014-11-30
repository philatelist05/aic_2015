package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecryptException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.EncryptException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

public class RSAEncryptDecrypt {

    static final Logger logger = LogManager.getLogger(RSAEncryptDecrypt.class.getName());

    public RSAEncryptDecrypt(){
    }

    public static byte[] encrypt(byte[] clearText, PublicKey publicKey) throws EncryptException{

        byte[] encryptedText = null;

        final Cipher rsa;
        try {
            rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "BC");
            rsa.init(Cipher.ENCRYPT_MODE, publicKey);
            encryptedText = rsa.doFinal(clearText);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot find RSA Algorithm.");
            logger.catching(Level.DEBUG, e);
            throw new EncryptException(e.getMessage());
        } catch (NoSuchProviderException e) {
            logger.warn("There is no such provider");
            logger.catching(Level.DEBUG, e);
            throw new EncryptException(e.getMessage());
        } catch (NoSuchPaddingException e) {
            logger.warn("Cannot find OAEPWithSHA1AndMGF1Padding padding.");
            logger.catching(Level.DEBUG, e);
            throw new EncryptException(e.getMessage());
        } catch (IllegalBlockSizeException e) {
            logger.warn("Illegal block size.");
            logger.catching(Level.DEBUG, e);
            throw new EncryptException(e.getMessage());
        } catch (BadPaddingException e) {
            logger.warn("Padding is bad.");
            logger.catching(Level.DEBUG, e);
            throw new EncryptException(e.getMessage());
        } catch (InvalidKeyException e) {
            logger.warn("Key is invalid.");
            logger.catching(Level.DEBUG, e);
            throw new EncryptException(e.getMessage());
        }

        return encryptedText;
    }

    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws DecryptException{

        byte[] decrypted = null;

        final Cipher rsa;
        try {
            rsa = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding", "BC");
            try {
                rsa.init(Cipher.DECRYPT_MODE, privateKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            try {
                decrypted =  rsa.doFinal(encryptedData);
            } catch (IllegalBlockSizeException e) {
                logger.warn("Illegal block size.");
                logger.catching(Level.DEBUG, e);
                throw new DecryptException(e.getMessage());
            } catch (BadPaddingException e) {
                logger.warn("Padding is bad.");
                logger.catching(Level.DEBUG, e);
                throw new DecryptException(e.getMessage());
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot find RSA Algorithm.");
            logger.catching(Level.DEBUG, e);
            throw new DecryptException(e.getMessage());
        } catch (NoSuchProviderException e) {
            logger.warn("There is no such provider");
            logger.catching(Level.DEBUG, e);
            throw new DecryptException(e.getMessage());
        } catch (NoSuchPaddingException e) {
            logger.warn("Cannot find OAEPWithSHA1AndMGF1Padding padding.");
            logger.catching(Level.DEBUG, e);
            throw new DecryptException(e.getMessage());
        }

        return decrypted;
    }
}
