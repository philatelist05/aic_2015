package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAEncryptDecrypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by Thomas on 29.11.2014.
 *
 * Represents one half of a Diffie Hellman Exchange.
 * Contains two primes and an encrypted DH public key.
 */
public class EncryptedDHHalf {
    private byte[] encryptedPublicKey;

    EncryptedDHHalf(byte[] encryptedPublicKey) {
        this.encryptedPublicKey = encryptedPublicKey;
    }

    /**
     * Decodes this object from the specified buffer at the current position and advances the position.
     */
    public EncryptedDHHalf(ByteBuffer input) {
        encryptedPublicKey = EncodingUtil.readByteArray(input);
    }

    /**
     * Encodes this object to the specified buffer at the current position and advances the position.
     */
    public void encode(ByteBuffer output) {
        EncodingUtil.writeByteArray(encryptedPublicKey, output);
    }

    /**
     * Decrypts this encrypted DH half using an RSA private key.
     */
    public DHHalf decrypt(PrivateKey privateKey) {
        byte[] publicKey = RSAEncryptDecrypt.decrypt(encryptedPublicKey, privateKey);
        return new DHHalf(publicKey);
    }
}
