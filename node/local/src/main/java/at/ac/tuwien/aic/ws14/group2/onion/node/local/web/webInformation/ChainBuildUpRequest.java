package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainNodeMetaData;

/**
 * Created by Stefan on 25.01.15.
 */
public class ChainBuildUpRequest implements RequestInfo<ChainMetaData> {
    private final ChainMetaData chainNodeMetaData;

    public ChainBuildUpRequest(ChainMetaData chainNodeMetaData) {
        this.chainNodeMetaData = chainNodeMetaData;
    }

    @Override
    public ChainMetaData getInfo() {
        return chainNodeMetaData;
    }
}

