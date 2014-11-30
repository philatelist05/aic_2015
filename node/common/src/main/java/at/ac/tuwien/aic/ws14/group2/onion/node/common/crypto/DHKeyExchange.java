package at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.KeyExchangeException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class DHKeyExchange {
    static final Logger logger = LogManager.getLogger(DHKeyExchange.class.getName());

    private KeyPairGenerator keyPairGenerator;
    private KeyAgreement keyAgreement;
    private KeyFactory keyFactory;


    public DHKeyExchange() throws KeyExchangeException {
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("DH", "BC");
            keyAgreement = KeyAgreement.getInstance("DH", "BC");
            keyFactory =  KeyFactory.getInstance("DH", "BC");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Could not find BouncyCastle provider.");
            logger.catching(Level.DEBUG, e);
            throw new KeyExchangeException(e);
        } catch (NoSuchProviderException e) {
            logger.warn("Could not find DH algorithm.");
            logger.catching(Level.DEBUG, e);
            throw new KeyExchangeException(e);
        }
    }

    public static BigInteger generateRelativePrime() {
        return BigInteger.probablePrime(256, new SecureRandom());
    }

    public byte[] initExchange(BigInteger p, BigInteger g) throws KeyExchangeException {
        DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g);
        try {
            keyPairGenerator.initialize(dhParameterSpec);
        } catch (InvalidAlgorithmParameterException e) {
            logger.warn("Failed to initialize KeyPairGenerator: {}", e.getMessage());
            logger.catching(Level.DEBUG, e);
            throw new KeyExchangeException(e);
        }

        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        try {
            keyAgreement.init(keyPair.getPrivate());
        } catch (InvalidKeyException e) {
            logger.warn("Invalid Key for key exchange.");
            logger.catching(Level.DEBUG, e);
            throw new KeyExchangeException(e);
        }

        return keyPair.getPublic().getEncoded();
    }

    public byte[] completeExchange(byte[] publicKeyBytes) throws KeyExchangeException {
        PublicKey publicKey = null;
        try {
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
        } catch (InvalidKeySpecException e) {
            logger.warn("Invalid KeySpec for key exchange");
            logger.catching(Level.DEBUG, e);
            throw new KeyExchangeException(e);
        }
        try {
            keyAgreement.doPhase(publicKey, true);
        } catch (InvalidKeyException e) {
            logger.warn("Invalid Key for key exchange.");
            logger.catching(Level.DEBUG, e);
            throw new KeyExchangeException(e);
        }

        return keyAgreement.generateSecret();
    }
}
