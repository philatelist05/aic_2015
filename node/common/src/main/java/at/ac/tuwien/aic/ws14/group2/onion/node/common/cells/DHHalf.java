package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.RSAEncryptDecrypt;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.EncryptException;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PublicKey;

/**
 * Created by Thomas on 27.11.2014.
 *
 * Represents an unencrypted half of a Diffie Hellman Exchange.
 */
public class DHHalf {
    private BigInteger g;
    private BigInteger p;
    private byte[] dhPublicKey;

    /**
     * Decodes this object from the specified buffer at the current position and advances the position.
     */
    DHHalf(ByteBuffer input) {
        g = new BigInteger(EncodingUtil.readByteArray(input));
        p = new BigInteger(EncodingUtil.readByteArray(input));
        dhPublicKey = EncodingUtil.readByteArray(input);
    }

    public DHHalf(BigInteger g, BigInteger p, byte[] publicKey) {
        this.g = g;
        this.p = p;
        this.dhPublicKey = publicKey;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getP() {
        return p;
    }

    public byte[] getPublicKey() {
        return dhPublicKey;
    }

    /**
     * Encrypts this DH half using an RSA public key.
     */
    public EncryptedDHHalf encrypt(PublicKey publicKey) throws EncryptException {
        byte[] gBytes = g.toByteArray();
        byte[] pBytes = p.toByteArray();

        byte[] data = new byte[gBytes.length + pBytes.length + dhPublicKey.length + 3 * EncodingUtil.LENGTH_FIELD_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        EncodingUtil.writeByteArray(gBytes, buffer);
        EncodingUtil.writeByteArray(pBytes, buffer);
        EncodingUtil.writeByteArray(dhPublicKey, buffer);

        byte[] block = RSAEncryptDecrypt.encrypt(data, publicKey);
        return new EncryptedDHHalf(block);
    }
}
