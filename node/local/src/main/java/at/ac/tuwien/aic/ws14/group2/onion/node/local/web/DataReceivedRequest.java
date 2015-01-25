package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation.RequestInfo;

/**
 * Created by Stefan on 25.01.15.
 */
public class DataReceivedRequest implements RequestInfo<byte[]> {
    private final byte[] data;

    public DataReceivedRequest(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getInfo() {
        return data;
    }
}
