package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.TargetInfo;

/**
 * Created by Stefan on 25.01.15.
 */
public class TargetRequestInfo implements RequestInfo<Endpoint> {
    private final Endpoint info;

    public TargetRequestInfo(Endpoint info) {
        this.info = info;
    }

    @Override
    public Endpoint getInfo() {
        return info;
    }
}
