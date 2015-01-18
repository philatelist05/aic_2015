package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.*;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.RSASignAndVerify;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.DecryptException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.EncryptException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.KeyExchangeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.CellWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ErrorCode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

public class LocalCellWorker implements CellWorker {
    static final Logger logger = LogManager.getLogger(LocalCellWorker.class.getName());

    private final ConnectionWorker connectionWorker;
    private final Cell cell;
    private final Circuit circuit;
    private final LocalNodeCore nodeCore;

    public LocalCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit, LocalNodeCore nodeCore) {
        this.connectionWorker = connectionWorker;
        this.cell = cell;
        this.circuit = circuit;
        this.nodeCore = nodeCore;
    }

    @Override
    public void run() {
        logger.debug("Got cell {} on circuit {}", cell, circuit);
        if (circuit == null || cell instanceof CreateCell) {
            logger.warn("Received CreateCell or Cell for non-existing Circuit - this should not happen in LocalNode!");
            return;
        }
        if (cell instanceof CreateResponseCell) {
            handleCreateResponseCell((CreateResponseCell) cell);
        } else if (cell instanceof RelayCell) {
            handleRelayCell((RelayCell) cell);
        } else if (cell instanceof DestroyCell) {
            handleDestroyCell((DestroyCell) cell);
        } else {
            logger.warn("Unsupported Cell received: {}", cell);
            return;
        }
    }

    private void handleDestroyCell(DestroyCell destroyCell) {
        logger.debug("Handling DestroyCell");

        SocksCallBack callBack = nodeCore.getCallBack(circuit.getCircuitID());

        nodeCore.removeChain(circuit.getCircuitID());
        callBack.chainDestroyed();
    }

    private void handleRelayCell(RelayCell relayCell) {
        logger.debug("Handling RelayCell");

        SocksCallBack callBack = nodeCore.getCallBack(circuit.getCircuitID());
        ChainMetaData metaData = this.nodeCore.getChainMetaData(this.circuit.getCircuitID());

        int lastNode = metaData.getLastNode();
        logger.debug("Current last node in chain is at position {}", lastNode);
        RelayCellPayload payload = relayCell.getPayload();
        for (int i = 0; i <= lastNode; i++) {
            ChainNodeMetaData currentNode = metaData.getNodes().get(i);
            try {
                payload = payload.decrypt(currentNode.getSessionKey());
            } catch (DecryptException e) {
                logger.warn("Failed to decrypt RelayCellPayload");
                logger.catching(Level.DEBUG, e);
                callBack.error(ErrorCode.DECRYPTION_FAILURE);
                return;
            }
        }

        logger.debug("Decrypted payload: {}", payload);

        try {
            Command relayedCommand = payload.decode();
            if (relayedCommand instanceof ExtendResponseCommand) {
                handleExtendResponse((ExtendResponseCommand) relayedCommand);
            } else if (relayedCommand instanceof ConnectResponseCommand) {
                handleConnectResponse((ConnectResponseCommand) relayedCommand);
            } else if (relayedCommand instanceof DataCommand) {
                handleDataCommand((DataCommand) relayedCommand);
            }
        } catch (DecodeException e) {
            logger.warn("Failed to decode RelayCellPayload");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.DECODING_FAILURE);
        }
    }

    private void handleDataCommand(DataCommand dataCommand) {
        logger.debug("Handling DataCommand");

        SocksCallBack callback = nodeCore.getCallBack(circuit.getCircuitID());

        callback.responseData(dataCommand.getSequenceNumber(), dataCommand.getData());
    }

    private void handleConnectResponse(ConnectResponseCommand connectResponseCommand) {
        logger.debug("Received ConnectResponseCommand: '{}'", connectResponseCommand);
    }

    private void handleExtendResponse(ExtendResponseCommand extendResponseCommand) {
        logger.debug("Handling ExtendResponseCommand");

        SocksCallBack callback = nodeCore.getCallBack(circuit.getCircuitID());
        ChainMetaData metaData = nodeCore.getChainMetaData(this.circuit.getCircuitID());

//        if (extendResponseCommand.getStatus() != CreateStatus.Success) {
//            retryExtendCommand(metaData, callback);
//            return;
//        }

        byte[] dhPublicKey = extendResponseCommand.getDHPublicKey();
        PublicKey publicKey = metaData.getNodes().get(metaData.getNextNode()).getPublicKey();
        if(!RSASignAndVerify.verifySig(dhPublicKey, publicKey, extendResponseCommand.getSignature())) {
            logger.warn("Signature check failed, aborting extend.");
            //TODO abort extend and call error callback
            return;
        }

        //TODO maybe move the non-Create DHKeyExchange to the ChainMetaData or ChainNodeMetaData?
        DHKeyExchange keyExchange = circuit.getDHKeyExchange();
        if (keyExchange == null) {
            logger.warn("No key exchange available, ignoring extendResponseCommand");
            return;
        }

        byte[] sessionKey = null;
        try {
            sessionKey = keyExchange.completeExchange(dhPublicKey);
        } catch (KeyExchangeException e) {
            logger.warn("Key exchange failed.");
        }

        if(sessionKey == null) {
            if (callback != null) {
                callback.error(ErrorCode.KEY_EXCHANGE_FAILED);
                return;
            }
        }

        metaData.growChain(sessionKey);
        nextChainBuildingStep(metaData, callback);
    }

    private void retryExtendCommand(ChainMetaData metaData, SocksCallBack callback) {
        //TODO check if this is really needed
    }

    private void handleCreateResponseCell(CreateResponseCell createResponseCell) {
        logger.debug("Handling CreateResponseCell");

        SocksCallBack callback = nodeCore.getCallBack(circuit.getCircuitID());
        ChainMetaData metaData = nodeCore.getChainMetaData(circuit.getCircuitID());

        if (createResponseCell.getStatus() != CreateStatus.Success) {
            retryCreateCell(metaData, callback);
            return;
        }

        byte[] dhPublicKey = createResponseCell.getDhPublicKey();
        PublicKey publicKey = metaData.getNodes().get(metaData.getNextNode()).getPublicKey();
        if(!RSASignAndVerify.verifySig(dhPublicKey, publicKey, createResponseCell.getSignature())) {

            if (circuit.getSessionKey() == null) {
                logger.warn("Signature check failed, retrying create.");
                retryCreateCell(metaData, callback);
            } else {
                logger.warn("Received CreateResponseCell with wrong Signature even though first Circuit has already been established, ignoring cell.");
            }
            return;
        }

        DHKeyExchange keyExchange = circuit.getDHKeyExchange();
        if (keyExchange == null || circuit.getSessionKey() != null) {
            logger.warn("No KeyExchange available or first Circuit already established, ignoring CreateResponseCell");
            return;
        }

        byte[] sessionKey = null;
        try {
            sessionKey = keyExchange.completeExchange(createResponseCell.getDhPublicKey());
        } catch (KeyExchangeException e) {
            logger.warn("Key exchange failed.");
        }

        if(sessionKey == null) {
            if (callback != null) {
                callback.error(ErrorCode.KEY_EXCHANGE_FAILED);
                return;
            }
        }

        circuit.setSessionKey(sessionKey);
        circuit.setDHKeyExchange(null);

        metaData.growChain(sessionKey);
        nextChainBuildingStep(metaData, callback);
    }

    private void retryCreateCell(ChainMetaData metaData, SocksCallBack callback) {
        nodeCore.removeChain(circuit.getCircuitID());
        nodeCore.createChain(metaData, callback);
    }

    private void nextChainBuildingStep(ChainMetaData metaData, SocksCallBack callBack) {
        ConcurrentHashMap<Integer, ChainNodeMetaData> nodes = metaData.getNodes();
        int nextNodeIndex = metaData.getNextNode();
        if (nodes == null) return;

        ChainNodeMetaData nextNode = nodes.get(nextNodeIndex);
        if (nextNode == null) {
            logger.info("Chain established, calling callback!");
            logger.debug("Updated chain metadata: {}", metaData);
            callBack.chainEstablished(metaData);
        } else {
            DHKeyExchange keyExchange;
            try {
                keyExchange = new DHKeyExchange();
            } catch (KeyExchangeException e) {
                logger.warn("Key exchange failed.");
                callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
                return;
            }
            BigInteger p = DHKeyExchange.generateRelativePrime();
            BigInteger g = DHKeyExchange.generateRelativePrime();
            byte[] publicKey;
            try {
                publicKey = keyExchange.initExchange(p, g);
            } catch (KeyExchangeException e) {
                logger.warn("Key exchange failed.");
                callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
                return;
            }

            circuit.setDHKeyExchange(keyExchange);
            EncryptedDHHalf encryptedDHHalf = null;
            try {
                encryptedDHHalf = new DHHalf(g, p, publicKey).encrypt(nextNode.getPublicKey());
            } catch (EncryptException e) {
                logger.warn("Could not encrypt DH half, aborting Chain creation.");
                logger.catching(Level.DEBUG, e);
                callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
                return;
            }
            ExtendCommand command = new ExtendCommand(nextNode.getEndPoint(), p, g, encryptedDHHalf);
            RelayCellPayload payload = new RelayCellPayload(command);
            logger.debug("Decrypted payload: {}", payload);
            for (int i = nextNodeIndex - 1; i >= 0; i--) {
                ChainNodeMetaData currentNode = nodes.get(i);
                logger.debug("Encrypting payload with session key for node {}", currentNode);
                try {
                    payload = payload.encrypt(currentNode.getSessionKey());
                    logger.debug("Payload after encryption: {}", payload);
                } catch (EncryptException e) {
                    logger.warn("Failed to encrypt ExtendCommand, aborting Chain creation");
                    logger.catching(Level.DEBUG, e);
                    callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
                    return;
                }
            }

            RelayCell relayCell = new RelayCell(circuit.getCircuitID(), payload);
            try {
                connectionWorker.sendCell(relayCell);
            } catch (IOException e) {
                logger.warn("Could not send RelayCell, aborting Chain creation");
                logger.catching(Level.DEBUG, e);
                callBack.error(ErrorCode.CW_FAILURE);
            }
        }
    }
}
