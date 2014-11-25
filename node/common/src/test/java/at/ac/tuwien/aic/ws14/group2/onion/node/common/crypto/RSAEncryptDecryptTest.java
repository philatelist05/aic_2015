package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import static org.junit.Assert.*;

public class RSAEncryptDecryptTest {

    static final Logger logger = LogManager.getLogger(RSAEncryptDecryptTest.class.getName());

    @BeforeClass
    public static void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testEncrypt(){
        RSAKeyGenerator keyGenerator = null;
        try {
            keyGenerator = new RSAKeyGenerator();
        } catch (NoSuchProviderException e) {
            logger.warn("There is no such provider");
            logger.catching(Level.DEBUG, e);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot find RSA algorithm");
            logger.catching(Level.DEBUG, e);
        }

        KeyPair keyPair = null;
        try {
            keyPair = keyGenerator.generateKeys(777);
        } catch (NoSuchProviderException e) {
            logger.warn("There is no such provider");
            logger.catching(Level.DEBUG, e);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot find RSA algorithm");
            logger.catching(Level.DEBUG, e);
        }

        final String text = "Original text";

        RSAEncryptDecrypt a = new RSAEncryptDecrypt();

        byte[] encText = a.encrypt(text, keyPair.getPublic());

        final String decText = a.decrypt(encText, keyPair.getPrivate());

        assertEquals(text, decText);
    }

    @Test
    public void testDecrypt() throws Exception {

    }
}