package at.ac.tuwien.aic.ws14.group2.onion.node.local.socks;

import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ErrorCode;

/**
 * The SOCKS callback interface
 */
public interface SocksCallBack {
    /**
     * Callback method that will be called when a chain requested using @see{LocalNodeCore.createChain} is completely established.
     * @param chainMetaData the updated chain meta data, including (first) circuit ID and session keys
     */
    void chainEstablished(ChainMetaData chainMetaData);

    /**
     * Callback method that will be called when a Data Cell was received.
     * @param data the raw data contained in the Data cell
     */
    void responseData(byte[] data);

    /**
     * Callback method that will be called when an unrecoverable error has occured.
     * @param errorCode
     */
    void error(ErrorCode errorCode);
}
