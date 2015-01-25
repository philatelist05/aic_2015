package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

/**
 * Created by Stefan on 25.01.15.
 */
public class DataSentRequest implements RequestInfo<byte[]> {
    private final byte[] data;

    public DataSentRequest(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getInfo() {
        return data;
    }
}
