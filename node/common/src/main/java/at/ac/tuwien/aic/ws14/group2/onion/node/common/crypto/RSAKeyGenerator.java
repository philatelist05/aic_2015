package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;

import java.security.Security;

public class RSAKeyGenerator {

    private KeyPairGenerator keyPairGenerator;

    public RSAKeyGenerator() throws NoSuchProviderException, NoSuchAlgorithmException {
        keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
    }

    public KeyPair generateKeys(long seed) throws NoSuchProviderException, NoSuchAlgorithmException {

           SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            secureRandom.setSeed(seed);
            keyPairGenerator.initialize(2048, secureRandom);

            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            return keyPair;
    }

}
