package at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation;

/**
 * Created by Stefan on 25.01.15.
 */
public class ErrorRequestInfo implements RequestInfo<String> {
    private final String error;

    public ErrorRequestInfo(String error) {
        this.error = error;
    }

    @Override
    public String getInfo() {
        return error;
    }
}
