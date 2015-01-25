package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;

/**
 * Created by Stefan on 25.01.15.
 */
public interface WebInformationCallback {

    void chainBuildUp(long requestId, ChainMetaData chainMetaData);

    void chainEstablished(long requestId, ChainMetaData chainMetaData);

    void establishedTargetConnection(long requestId, Endpoint endpoint);

    void dataSent(long requestId, byte[] data);

    void dataReceived(long requestId, byte[] data);

    void chainDestroyed(long requestId);

    void error(long requestId, String errorMsg);

    void info(long requestId, String msg);


}
