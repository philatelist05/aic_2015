package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainNodeMetaData;

/**
 * Created by Stefan on 25.01.15.
 */
public class ChainBuildUp {
    private int stepNumber;
    private ChainNodeMetaData node;
    private boolean requestOrResponse;
    private boolean success;

    public ChainBuildUp(int stepNumber, ChainNodeMetaData node, boolean requestOrResponse, boolean success) {
        this.stepNumber = stepNumber;
        this.node = node;
        this.requestOrResponse = requestOrResponse;
        this.success = success;
    }
}
