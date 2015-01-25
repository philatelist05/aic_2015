package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;

/**
 * Created by Stefan on 25.01.15.
 */
public class TargetInfo {

    private String quote;
    private Endpoint endpoint;
    private int port;

    public TargetInfo(String quote, Endpoint endpoint, int port) {
        this.quote = quote;
        this.endpoint = endpoint;
        this.port = port;
    }


    public String getQuote() {
        return quote;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public int getPort() {
        return port;
    }
}
