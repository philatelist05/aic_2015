package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by Milan (thanks to Fabian) on 12.11.2014.
 */
public class KeyExchange {
    private KeyPairGenerator keyPairGenerator;
    private KeyAgreement keyAgreement;

    public KeyExchange() throws NoSuchProviderException, NoSuchAlgorithmException {
        keyPairGenerator = KeyPairGenerator.getInstance("DH", "BC");
        keyAgreement = KeyAgreement.getInstance("DH", "BC");
    }

    public static BigInteger generateRelativePrime() {

        return BigInteger.probablePrime(256, new SecureRandom());
    }

    public byte[] initExchange(BigInteger p, BigInteger g) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException {
        DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g);
        keyPairGenerator.initialize(dhParameterSpec);

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        keyAgreement.init(keyPair.getPrivate());

        return keyPair.getPublic().getEncoded();
    }

    public byte[] completeExchange(byte[] publicKeyBytes) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        KeyFactory keyFactory = KeyFactory.getInstance("DH", "BC");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        keyAgreement.doPhase(publicKey, true);

        return keyAgreement.generateSecret();
    }
}
