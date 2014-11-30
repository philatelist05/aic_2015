package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Thomas on 22.11.2014.
 */
public class Circuit {
    private final short circuitID;
    private final Endpoint endpoint;
    private byte[] sessionKey;
    private DHKeyExchange DHKeyExchange;
    private Circuit associatedCircuit;


    /**
     * Creates a new circuit using the specified endpoint and a random circuit ID.
     */
    public Circuit(Endpoint endpoint) {
        circuitID = (short)ThreadLocalRandom.current().nextInt(Short.MAX_VALUE + 1);
        this.endpoint = endpoint;
    }

    public Circuit(short circuitID, Endpoint endpoint) {
        this.circuitID = circuitID;
        this.endpoint = endpoint;
    }

    public short getCircuitID() {
        return circuitID;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Circuit getAssociatedCircuit() {
        return associatedCircuit;
    }

    public void setAssociatedCircuit(Circuit associatedCircuit) {
        this.associatedCircuit = associatedCircuit;
    }

    public DHKeyExchange getDHKeyExchange() {
        return DHKeyExchange;
    }

    public void setDHKeyExchange(DHKeyExchange DHKeyExchange) {
        this.DHKeyExchange = DHKeyExchange;
    }
}
