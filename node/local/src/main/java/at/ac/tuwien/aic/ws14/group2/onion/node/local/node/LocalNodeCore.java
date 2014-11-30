package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.*;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.EncryptException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ErrorCode;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LocalNodeCore {
    static final Logger logger = LogManager.getLogger(LocalNodeCore.class.getName());

    private Random random;
    private ConcurrentSkipListSet<Short> circuitIDs;
    private ConcurrentHashMap<Short, ChainMetaData> chains;
    private ConcurrentHashMap<Short, SocksCallBack> callbacks;

    public LocalNodeCore(ConcurrentSkipListSet<Short> circuitIDs, ConcurrentHashMap<Short, ChainMetaData> chains, ConcurrentHashMap<Short, SocksCallBack> callbacks) {
        this.random = new Random();
        this.circuitIDs = circuitIDs;
        this.chains = chains;
        this.callbacks = callbacks;
    }

    public LocalNodeCore() {
        this.random = new Random();
        this.circuitIDs = new ConcurrentSkipListSet<>();
        this.chains = new ConcurrentHashMap<>();
        this.callbacks = new ConcurrentHashMap<>();
    }

    public ChainMetaData getChainMetaData(Short circuitID) {
        return chains.get(circuitID);
    }

    public SocksCallBack getCallBack(Short circuitID) {
        return callbacks.get(circuitID);
    }

    public void createChain(ChainMetaData chainMetaData, SocksCallBack callBack) {
        // TODO spawn Thread instead to start Chainbuilding
        // TODO add check that chain is long enough according to configuration
        if (chainMetaData == null || callBack == null) {
            throw new NullPointerException("Parameters must not be NULL");
        }
        ChainNodeMetaData firstNode = chainMetaData.getNodes().get(0);
        Circuit circuit = new Circuit(getAndReserveFreeCircuitID(), firstNode.getEndPoint());

        callbacks.putIfAbsent(circuit.getCircuitID(), callBack);
        chains.putIfAbsent(circuit.getCircuitID(), chainMetaData);

        DHKeyExchange keyExchange;
        try {
            keyExchange = new DHKeyExchange();
        } catch (NoSuchProviderException e) {
            logger.warn("Could not initialize DHKeyExchange object, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Could not initalize DHKeyExchange object, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        }
        BigInteger p = DHKeyExchange.generateRelativePrime();
        BigInteger g = DHKeyExchange.generateRelativePrime();
        byte[] publicKey;
        try {
            publicKey = keyExchange.initExchange(p, g);
        } catch (InvalidAlgorithmParameterException e) {
            logger.warn("Could not initialize key exchange, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        } catch (NoSuchProviderException e) {
            logger.warn("Could not initialize key exchange, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Could not initialize key exchange, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        } catch (InvalidKeyException e) {
            logger.warn("Could not initialize key exchange, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        }

        circuit.setDHKeyExchange(keyExchange);

        EncryptedDHHalf encryptedDHHalf;
        try {
            encryptedDHHalf = new DHHalf(g, p, publicKey).encrypt(firstNode.getPublicKey());
        } catch (EncryptException e) {
            logger.warn("Could not encrypt DH half, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        }
        CreateCell cell = new CreateCell(circuit.getCircuitID(), firstNode.getEndPoint(), encryptedDHHalf);
        try {
            sendCell(cell, circuit.getEndpoint());
        } catch (IOException e) {
            logger.warn("Encountered IOException while trying to send cell to Circuit {}: {}", circuit, e.getMessage());
            callBack.error(ErrorCode.CW_FAILURE);
        }
    }

    public boolean connectTo(Short circuitID, Endpoint endpoint) {
        ConnectCommand connectCommand = new ConnectCommand(endpoint);
        Cell cell = encryptCommandForChain(circuitID, connectCommand);
        if (cell == null) {
            return false;
        } else {
            try {
                sendCell(cell, endpoint);
            } catch (IOException e) {
                logger.warn("Encountered IOException while trying to send cell.");
                logger.catching(Level.DEBUG, e);
                return false;
            }
        }
        return true;
    }

    public boolean sendData(Short circuitID, byte[] data) {
        ChainMetaData chainMetaData = getChainMetaData(circuitID);
        synchronized (chainMetaData) {
            int sequenceNumber = chainMetaData.incrementAndGetSequenceNumber();
            //TODO create and send DataCommand in RelayCell

        }
        return true;
    }

    public short getAndReserveFreeCircuitID() {
        Short circuitId = (short) random.nextInt(Short.MAX_VALUE);
        while (!circuitIDs.add(circuitId)) {
            circuitId = (short) random.nextInt(Short.MAX_VALUE);
        }
        return circuitId;
    }

    public boolean removeChain(Short circuitID) {
        boolean success = true;
        if(!circuitIDs.remove(circuitID)) {
            success = false;
        }
        if(chains.remove(circuitID) == null) {
            success = false;
        }
        if(callbacks.remove(circuitID) == null) {
            success = false;
        }
        return success;
    }

    protected Cell encryptCommandForChain(Short circuitID, Command command) {
        ChainMetaData metaData = getChainMetaData(circuitID);
        SocksCallBack callBack = getCallBack(circuitID);

        ConcurrentHashMap<Integer, ChainNodeMetaData> nodes = metaData.getNodes();
        int nextNodeIndex = metaData.getNextNode();
        if (nodes != null) {
            ChainNodeMetaData nextNode = nodes.get(nextNodeIndex);
            RelayCellPayload payload = new RelayCellPayload(command);
            for (int i = 0; i < nextNodeIndex; i++) {
                ChainNodeMetaData currentNode = nodes.get(i);
                try {
                    payload = payload.encrypt(currentNode.getSessionKey());
                } catch (EncryptException e) {
                    logger.warn("Failed to encrypt ExtendCommand, aborting Chain creation");
                    logger.catching(Level.DEBUG, e);
                    callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
                    return null;
                }
            }
            return new RelayCell(circuitID, payload);
        }
        return null;
    }

    protected void sendCell(Cell cell, Endpoint endpoint) throws IOException {
        ConnectionWorker connectionWorker = ConnectionWorkerFactory.getInstance().getConnectionWorker(endpoint);
        connectionWorker.sendCell(cell);
    }
}
