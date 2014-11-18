package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class DHKeyExchangeTest {

    @Test
    public void testInitExchange() throws Exception {
        DHKeyExchange keyExchange = new DHKeyExchange();
        keyExchange.initExchange(DHKeyExchange.generateRelativePrime(), DHKeyExchange.generateRelativePrime());
    }

    @Test
    public void testSharedSecret() throws Exception {
        BigInteger p = DHKeyExchange.generateRelativePrime();
        BigInteger g = DHKeyExchange.generateRelativePrime();

        DHKeyExchange keyExchangeA = new DHKeyExchange();
        DHKeyExchange keyExchangeB = new DHKeyExchange();

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
        DHKeyExchange keyExchange = new DHKeyExchange();
        keyExchange.completeExchange(null);
    }

    @Test
    public void testCrypting() throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException, NoSuchProviderException, InvalidKeySpecException, InvalidCipherTextException {
        DHKeyExchange keyExchange = new DHKeyExchange();
        keyExchange.initExchange(DHKeyExchange.generateRelativePrime(), DHKeyExchange.generateRelativePrime());

        BigInteger p = DHKeyExchange.generateRelativePrime();
        BigInteger g = DHKeyExchange.generateRelativePrime();

        DHKeyExchange keyExchangeA = new DHKeyExchange();
        DHKeyExchange keyExchangeB = new DHKeyExchange();

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

    public void testRSASignAndVerify() throws Exception {

        RSAKeysGenerate keysGenerate = new RSAKeysGenerate();
        KeyPair keyPair = keysGenerate.generateKeys(999);

        byte[] data = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74 };

        RSASignAndVerify signAndVerify = new RSASignAndVerify();
        byte[] digitalSignature = signAndVerify.signData(data, keyPair.getPrivate());

        boolean verified = false;

        verified = signAndVerify.verifySig(data, keyPair.getPublic(), digitalSignature);
        assertTrue(verified);

        keyPair = keysGenerate.generateKeys(888);
        verified = signAndVerify.verifySig(data, keyPair.getPublic(), digitalSignature);
        assertTrue(verified);
    }
}