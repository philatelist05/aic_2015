package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;

/**
 * Created by Stefan on 25.01.15.
 */
public class ChainEstablishedInfo implements RequestInfo<ChainMetaData> {
    private final ChainMetaData chainMetaData;

    public ChainEstablishedInfo(ChainMetaData metaData){
        this.chainMetaData = metaData;
    }
    @Override
    public ChainMetaData getInfo() {
        return chainMetaData;
    }
}
