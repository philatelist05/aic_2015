package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import java.io.IOException;

/**
 * Created by Stefan on 02.12.14.
 */
public interface TargetForwarder {
    public void forward(byte[] data) throws IOException;
    void setTargetWorkerCallback(TargetWorker targetWorker);
}
