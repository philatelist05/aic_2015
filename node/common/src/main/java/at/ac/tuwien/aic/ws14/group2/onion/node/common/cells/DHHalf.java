package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSAEncryptDecrypt;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PublicKey;

/**
 * Created by Thomas on 27.11.2014.
 *
 * Represents an unencrypted half of a Diffie Hellman Exchange.
 */
public class DHHalf {
    private byte[] dhPublicKey;

    /**
     * Decodes this object from the specified buffer at the current position and advances the position.
     */
    DHHalf(ByteBuffer input) {
        dhPublicKey = EncodingUtil.readByteArray(input);
    }

    public DHHalf(byte[] publicKey) {
        this.dhPublicKey = publicKey;
    }

    public byte[] getPublicKey() {
        return dhPublicKey;
    }

    /**
     * Encodes this object to the specified buffer at the current position and advances the position.
     */
    public void encode(ByteBuffer output) {
        EncodingUtil.writeByteArray(dhPublicKey, output);
    }

    /**
     * Encrypts this DH half using an RSA public key.
     */
    public EncryptedDHHalf encrypt(BigInteger prime1, BigInteger prime2, PublicKey publicKey) {
        byte[] block = RSAEncryptDecrypt.encrypt(this.dhPublicKey, publicKey);
        return new EncryptedDHHalf(block);
    }
}
