package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.junit.Assert;
import org.junit.Test;
import sun.security.pkcs.PKCS7;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class KeyExchangeTest {

    @Test
    public void testInitExchange() throws Exception {
        KeyExchange keyExchange = new KeyExchange();
        keyExchange.initExchange(KeyExchange.generateRelativePrime(),KeyExchange.generateRelativePrime());
    }

    @Test
    public void testSharedSecret() throws Exception {
        BigInteger p = KeyExchange.generateRelativePrime();
        BigInteger g = KeyExchange.generateRelativePrime();

        KeyExchange keyExchangeA = new KeyExchange();
        KeyExchange keyExchangeB = new KeyExchange();

        byte[] publicKeyA = keyExchangeA.initExchange(p,g);
        byte[] publicKeyB = keyExchangeB.initExchange(p,g);

        assertNotNull(publicKeyA);
        assertNotNull(publicKeyB);



        byte[] sharedSecretA = keyExchangeA.completeExchange(publicKeyB);
        byte[] sharedSecretB = keyExchangeB.completeExchange(publicKeyA);

        Assert.assertArrayEquals(sharedSecretA, sharedSecretB);
    }

    @Test(expected = NullPointerException.class)
    public void testCompleteExchangeWithNullBytes_ShouldFail() throws Exception {
        KeyExchange keyExchange = new KeyExchange();
        keyExchange.completeExchange(null);
    }

    @Test
    public void testCrypting() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException, InvalidCipherTextException {
        KeyExchange keyExchange = new KeyExchange();
        keyExchange.initExchange(KeyExchange.generateRelativePrime(),KeyExchange.generateRelativePrime());

        BigInteger p = KeyExchange.generateRelativePrime();
        BigInteger g = KeyExchange.generateRelativePrime();

        KeyExchange keyExchangeA = new KeyExchange();
        KeyExchange keyExchangeB = new KeyExchange();

        byte[] publicKeyA = keyExchangeA.initExchange(p,g);
        byte[] publicKeyB = keyExchangeB.initExchange(p,g);

       // assertNotNull(publicKeyA);
        //assertNotNull(publicKeyB);



        byte[] sharedSecretA = keyExchangeA.completeExchange(publicKeyB);
        byte[] sharedSecretB = keyExchangeB.completeExchange(publicKeyA);

        AESAlgorithm aes = new AESAlgorithm();
        aes.setKey(sharedSecretA);
        aes.setPadding(new PKCS7Padding());

        String str = "Test";
        byte[] bytes = str.getBytes();
        byte[] encrypt = aes.encrypt(bytes);

        assertFalse(Arrays.equals(encrypt, bytes));

        byte[] decrypt = aes.decrypt(encrypt);
        String str1 = new String(decrypt);
        assertTrue(str1.startsWith(str));
       //assertEquals(str1, str);
    }
}