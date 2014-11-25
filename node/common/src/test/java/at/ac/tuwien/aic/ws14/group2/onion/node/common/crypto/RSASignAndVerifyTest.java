package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.KeyPair;
import java.security.Security;

import static org.junit.Assert.*;

public class RSASignAndVerifyTest {

    @BeforeClass
    public static void init() {
        Security.addProvider(new BouncyCastleProvider());
    }

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