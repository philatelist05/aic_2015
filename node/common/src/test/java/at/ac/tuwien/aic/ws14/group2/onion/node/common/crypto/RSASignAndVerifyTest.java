package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

public class RSASignAndVerifyTest {

    @Test
    public void testSignData() throws Exception {

        RSAKeyGenerator keysGenerate = new RSAKeyGenerator();
        KeyPair keyPair = keysGenerate.generateKeys(999);

        byte[] data = { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74 };

        RSASignAndVerify signAndVerify = new RSASignAndVerify();
        byte[] digitalSignature = signAndVerify.signData(data, keyPair.getPrivate());

        boolean verified = false;

        verified = signAndVerify.verifySig(data, keyPair.getPublic(), digitalSignature);
        assertTrue(verified);

        keyPair = keysGenerate.generateKeys(888);
        verified = signAndVerify.verifySig(data, keyPair.getPublic(), digitalSignature);
        assertFalse(verified);
    }

    @Test
    public void testVerifySig() throws Exception {

    }
}