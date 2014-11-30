package at.ac.tuwien.aic.ws14.group2.onion.node.common.cells;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;

/**
 * Created by Thomas on 30.11.2014.
 */
public enum CreateStatus {
    Success((byte)1),
    CircuitIDAlreadyExists((byte)2);

    byte value;

    private CreateStatus(byte value) {
        this.value = value;
    }

    byte toByte() {
        return value;
    }

    public static CreateStatus fromByte(byte value) throws DecodeException {
        switch (value) {
            case 1:
                return Success;
            case 2:
                return CircuitIDAlreadyExists;
            default:
                throw new DecodeException("Invalid CreateStatus value: " + value);
        }
    }
}
