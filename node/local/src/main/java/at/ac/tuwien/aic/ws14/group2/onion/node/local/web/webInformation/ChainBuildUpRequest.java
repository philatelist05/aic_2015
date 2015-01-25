package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

/**
 * Created by Stefan on 25.01.15.
 */
public class ChainBuildUpRequest implements RequestInfo<ChainBuildUp> {
    private final ChainBuildUp chainBuildUp;

    public ChainBuildUpRequest(ChainBuildUp chainBuildUp) {
        this.chainBuildUp = chainBuildUp;
    }

    @Override
    public ChainBuildUp getInfo() {
        return chainBuildUp;
    }
}

