package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

/**
 * Created by Stefan on 25.01.15.
 */
public class InfoRequest implements RequestInfo<String> {
    private final String msg;

    public InfoRequest(String msg) {

        this.msg = msg;
    }

    @Override
    public String getInfo() {
        return msg;
    }
}
