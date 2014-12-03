package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.io.IOException;

/**
 * Created by Stefan on 02.12.14.
 */
public interface TargetForwarder {
    /**
     * Connects to the specified endpoint.
     * Must only be called once.
     */
    void connect(Endpoint target) throws IOException;

    /**
     * Sends data to the endpoint this TargetForwarder has previously been connected to.
     * The connect method must be called beforehand.
     */
    void forward(byte[] data) throws IOException;

    void setTargetWorkerCallback(TargetWorker targetWorker);
}
