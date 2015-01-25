package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;

import java.util.List;

/**
 * Created by Stefan on 25.01.15.
 */
public class ChainRequestInfo implements RequestInfo<List<ChainNodeInformation>> {
    private final List<ChainNodeInformation> list;

    public ChainRequestInfo(List<ChainNodeInformation> list){
        this.list = list;
    }
    @Override
    public List<ChainNodeInformation> getInfo() {
        return list;
    }
}
