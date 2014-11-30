package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.AESAlgorithm;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecryptException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.EncryptException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.paddings.PKCS7Padding;

import java.util.Arrays;

/**
 * Created by Thomas on 09.11.2014.
 */
public class RelayCellPayload {
    private AESAlgorithm aes = new AESAlgorithm();
    private byte[] payload;

    public RelayCellPayload(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Creates a Relay Cell Payload by encoding the specified Command.
     * Changes in the Command object will not be reflected in this payload.
     */
    public RelayCellPayload(Command command) {
        payload = command.encode();
    }

    public RelayCellPayload decrypt(byte[] sessionKey) throws DecryptException {
        try {
            aes.setKey(sessionKey);
            return new RelayCellPayload(aes.decrypt(payload));
        } catch (InvalidCipherTextException ex) {
            throw new DecryptException(ex);
        }
    }

    public RelayCellPayload encrypt(byte[] sessionKey) throws EncryptException {
        try {
            aes.setKey(sessionKey);
            return new RelayCellPayload(aes.encrypt(payload));
        } catch (InvalidCipherTextException ex) {
            throw new EncryptException(ex);
        }
    }

    /**
     * Decodes this Relay Payload assuming that it is not encrypted.
     */
    public Command decode() throws DecodeException {
        return Command.decode(payload);
    }

    /**
     * @return A byte array of size Cell.CELL_PAYLOAD_BYTES.
     */
    public byte[] encode() {
        return payload;
    }

    @Override
    public String toString() {
        return "RelayCellPayload{" +
                "payload=" + Arrays.toString(payload) +
                '}';
    }
}
