package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecryptException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.EncryptException;
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
    public void testEncrypt() throws EncryptException, DecryptException {
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

        final byte[] clearText = "Original text".getBytes();

        byte[] encText = RSAEncryptDecrypt.encrypt(clearText, keyPair.getPublic());

        final byte[] decText = RSAEncryptDecrypt.decrypt(encText, keyPair.getPrivate());

        assertArrayEquals(clearText, decText);
    }

    @Test
    public void testEncryptLong() throws Exception {
        RSAKeyGenerator keyGenerator = new RSAKeyGenerator();

        KeyPair keyPair = keyGenerator.generateKeys(0);

        final byte[] clearText = new byte[214];   // maximum length

        byte[] encText = RSAEncryptDecrypt.encrypt(clearText, keyPair.getPublic());

        final byte[] decText = RSAEncryptDecrypt.decrypt(encText, keyPair.getPrivate());

        assertArrayEquals(clearText, decText);
    }
}