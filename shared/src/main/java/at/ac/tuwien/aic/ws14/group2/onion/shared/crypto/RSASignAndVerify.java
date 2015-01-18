package at.ac.tuwien.aic.ws14.group2.onion.shared.crypto;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;

public class RSASignAndVerify {

    static final Logger logger = LogManager.getLogger(RSASignAndVerify.class.getName());

    public RSASignAndVerify() {
    }

    public static byte[] signData(byte[] data, PrivateKey key) {

        byte[] signature = null;
        try {
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(key);
            signer.update(data);
            signature = signer.sign();
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot find RSA Algorithm.");
            logger.catching(Level.DEBUG, e);
        } catch (SignatureException e) {
            logger.warn("Encountered SignatureException.");
            logger.catching(Level.DEBUG, e);
        } catch (InvalidKeyException e) {
            logger.warn("Key is invalid.");
            logger.catching(Level.DEBUG, e);
        }
        return signature;
    }

    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) {
        boolean valid = false;
        try {
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initVerify(key);
            signer.update(data);
            valid = signer.verify(sig);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Cannot find RSA Algorithm.");
            logger.catching(Level.DEBUG, e);
        } catch (SignatureException e) {
            logger.warn("Encountered SignatureException.");
            logger.catching(Level.DEBUG, e);
        } catch (InvalidKeyException e) {
            logger.warn("Key is invalid.");
            logger.catching(Level.DEBUG, e);
        }
        return valid;
    }
}
