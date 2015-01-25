package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.TargetInfo;

/**
 * Created by Stefan on 25.01.15.
 */
public class TargetRequestInfo implements RequestInfo<TargetInfo> {
    private final TargetInfo info;

    public TargetRequestInfo(TargetInfo info) {
        this.info = info;
    }

    @Override
    public TargetInfo getInfo() {
        return info;
    }
}
