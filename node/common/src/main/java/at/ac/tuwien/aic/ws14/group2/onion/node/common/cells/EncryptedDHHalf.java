package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAEncryptDecrypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PrivateKey;

/**
 * Created by Thomas on 29.11.2014.
 *
 * Represents one half of a Diffie Hellman Exchange.
 * Contains two primes and an encrypted DH public key.
 */
public class EncryptedDHHalf {
    private byte[] encryptedHalf;

    EncryptedDHHalf(byte[] encryptedHalf) {
        this.encryptedHalf = encryptedHalf;
    }

    /**
     * Decodes this object from the specified buffer at the current position and advances the position.
     */
    public EncryptedDHHalf(ByteBuffer input) {
        encryptedHalf = EncodingUtil.readByteArray(input);
    }

    /**
     * Encodes this object to the specified buffer at the current position and advances the position.
     */
    public void encode(ByteBuffer output) {
        EncodingUtil.writeByteArray(encryptedHalf, output);
    }

    /**
     * Decrypts this encrypted DH half using an RSA private key.
     */
    public DHHalf decrypt(PrivateKey privateKey) {
        byte[] data = RSAEncryptDecrypt.decrypt(encryptedHalf, privateKey);
        return new DHHalf(ByteBuffer.wrap(data));
    }
}
