package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.*;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CircuitIDExistsAlreadyException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.DecodeException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.SocksCallBack;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.socks.exceptions.ErrorCode;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.WebInformationCallback;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.EncryptException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.KeyExchangeException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LocalNodeCore {
    static final Logger logger = LogManager.getLogger(LocalNodeCore.class.getName());

    private Endpoint fakedEndpoint;
    private Random random;
    private ConcurrentSkipListSet<Short> circuitIDs;
    private ConcurrentHashMap<Short, ChainMetaData> chains;
    private ConcurrentHashMap<Short, SocksCallBack> callbacks;
    private Endpoint lastExitNode;
    private WebInformationCallback webCallback;

    public LocalNodeCore(Endpoint endpoint, ConcurrentSkipListSet<Short> circuitIDs, ConcurrentHashMap<Short, ChainMetaData> chains, ConcurrentHashMap<Short, SocksCallBack> callbacks) {
        this.fakedEndpoint = endpoint;
        this.random = new Random();
        this.circuitIDs = circuitIDs;
        this.chains = chains;
        this.callbacks = callbacks;
    }

    public LocalNodeCore(Endpoint endpoint) {
        this.fakedEndpoint = endpoint;
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

    /**
     * If this method is called a twice and the chain for the second call has the same exit node as the first one, then returns false.
     * Otherwise it returns true.
     */
    public boolean checkExitNode(ChainMetaData chainMetaData) {
        logger.debug("check chain's exit node");

        Endpoint exitNode = chainMetaData.getNodes().get(chainMetaData.getNodes().size() - 1).getEndPoint();

        logger.debug("lastExitNode: " + lastExitNode);
        logger.debug("exitNode: " + exitNode);

        if (exitNode.equals(lastExitNode)) {
            logger.debug("same exit node received twice");
            return false;
        }

        lastExitNode = exitNode;

        return true;
    }

    public void createChain(ChainMetaData chainMetaData, SocksCallBack callBack) {
        logger.info("Creating new chain");
        logger.debug("Chain metadata: {}", chainMetaData);
        // TODO spawn Thread instead to start Chainbuilding
        // TODO add check that chain is long enough according to configuration
        if (chainMetaData == null || callBack == null) {
            throw new NullPointerException("Parameters must not be NULL");
        }
        ChainNodeMetaData firstNode = chainMetaData.getNodes().get(0);
        Circuit circuit = new Circuit(getAndReserveFreeCircuitID(), firstNode.getEndPoint());
        chainMetaData.setCircuitID(circuit.getCircuitID());

        if (this.hasWebCallback())
            this.getWebCallback().chainBuildUp(circuit.getCircuitID(), chainMetaData);

        callbacks.putIfAbsent(circuit.getCircuitID(), callBack);
        chains.putIfAbsent(circuit.getCircuitID(), chainMetaData);

        DHKeyExchange keyExchange;
        try {
            keyExchange = new DHKeyExchange();
        } catch (KeyExchangeException e) {
            logger.warn("Could not initialize DHKeyExchange object, aborting Chain creation.");
            logger.catching(Level.DEBUG, e);
            callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
            return;
        }
        BigInteger p = DHKeyExchange.generateRelativePrime();
        BigInteger g = DHKeyExchange.generateRelativePrime();
        byte[] publicKey;
        try {
            publicKey = keyExchange.initExchange(p, g);
        } catch (KeyExchangeException e) {
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

        CreateCell cell = new CreateCell(circuit.getCircuitID(), fakedEndpoint, encryptedDHHalf);
        try {
            addCircuitToConnectionWorker(circuit);
            sendCell(cell, circuit.getEndpoint());
        } catch (IOException e) {
            logger.warn("Encountered IOException while trying to get ConnectionWorker for Circuit @ : {}", circuit.getEndpoint(), e.getMessage());
            callBack.error(ErrorCode.CW_FAILURE);
        } catch (CircuitIDExistsAlreadyException e) {
            logger.warn("Duplicate circuit ID, this should never ever happen!");
            callBack.error(ErrorCode.CW_FAILURE);
        }
    }

    public void destroyChain(short circuitID) {
        ChainMetaData chainMetaData = getChainMetaData(circuitID);
        SocksCallBack callBack = getCallBack(circuitID);
        synchronized (chainMetaData) {
            Endpoint endpoint = chainMetaData.getNodes().get(0).getEndPoint();

            logger.info("Sending DestroyCell to node {} over circuit {}", endpoint, circuitID);

            try {
                sendCell(new DestroyCell(circuitID), endpoint);
            } catch (IOException e) {
                callBack.error(ErrorCode.CW_FAILURE);
            }
        }

        removeChain(circuitID);
    }

    public void connectTo(Short circuitID, Endpoint endpoint) {
        logger.info("Sending ConnectCommand to target {} over circuit {}", endpoint, circuitID);

        ChainMetaData chainMetaData = getChainMetaData(circuitID);
        SocksCallBack callBack = getCallBack(circuitID);

        ConnectCommand connectCommand = new ConnectCommand(endpoint);
        Cell cell = null;
        try {
            cell = encryptCommandForChain(circuitID, connectCommand);
        } catch (EncryptException e) {
            callBack.error(ErrorCode.ENCRYPTION_FAILURE);
            return;
        }

        try {
            sendCell(cell, chainMetaData.getNodes().get(0).getEndPoint());
        } catch (IOException e) {
            callBack.error(ErrorCode.CW_FAILURE);
            return;
        }

        if (this.hasWebCallback())
            this.getWebCallback().establishedTargetConnection(circuitID, endpoint);
    }

    public void sendData(Short circuitID, byte[] data) {
        ChainMetaData chainMetaData = getChainMetaData(circuitID);
        SocksCallBack callBack = getCallBack(circuitID);
        synchronized (chainMetaData) {
            long sequenceNumber = chainMetaData.incrementAndGetSequenceNumber();
            DataCommand payload;
            try {
                payload = new DataCommand(sequenceNumber, data);
            } catch (DecodeException e) {
                callBack.error(ErrorCode.TOO_MUCH_DATA);
                return;
            }
            Cell cell;
            try {
                cell = encryptCommandForChain(circuitID, payload);
            } catch (EncryptException e) {
                callBack.error(ErrorCode.ENCRYPTION_FAILURE);
                return;
            }
            try {
                sendCell(cell, chainMetaData.getNodes().get(0).getEndPoint());
            } catch (IOException e) {
                callBack.error(ErrorCode.CW_FAILURE);
            }
        }
    }

    public short getAndReserveFreeCircuitID() {
        Short circuitId;
        do {
            circuitId = (short) random.nextInt(Short.MAX_VALUE);
        } while (!circuitIDs.add(circuitId));
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

    protected Cell encryptCommandForChain(Short circuitID, Command command) throws EncryptException {
        ChainMetaData metaData = getChainMetaData(circuitID);
        SocksCallBack callBack = getCallBack(circuitID);

        ConcurrentHashMap<Integer, ChainNodeMetaData> nodes = metaData.getNodes();
        if (nodes == null)
            throw new EncryptException("No nodes in the chain");

        RelayCellPayload payload = new RelayCellPayload(command);
        for (int i = metaData.getLastNode(); i >= 0; i--) {
            ChainNodeMetaData currentNode = nodes.get(i);
            try {
                payload = payload.encrypt(currentNode.getSessionKey());
            } catch (EncryptException e) {
                logger.warn("Failed to encrypt ExtendCommand, aborting Chain creation");
                logger.catching(Level.DEBUG, e);
                callBack.error(ErrorCode.KEY_EXCHANGE_FAILED);
                throw e;
            }
        }
        return new RelayCell(circuitID, payload);
    }

    protected void sendCell(Cell cell, Endpoint endpoint) throws IOException {
        logger.debug("Sending cell {} to Endpoint {}", cell, endpoint);
        ConnectionWorker connectionWorker = ConnectionWorkerFactory.getInstance().getConnectionWorker(endpoint);
        connectionWorker.sendCell(cell);
    }

    protected void addCircuitToConnectionWorker(Circuit circuit) throws IOException, CircuitIDExistsAlreadyException {
        ConnectionWorker connectionWorker = ConnectionWorkerFactory.getInstance().getConnectionWorker(circuit.getEndpoint());
        connectionWorker.addCircuit(circuit);
    }

    public WebInformationCallback getWebCallback() {
        return webCallback;
    }

    public void setWebCallback(WebInformationCallback webCallback) {
        this.webCallback = webCallback;
    }

    public boolean hasWebCallback() {
        return webCallback == null;
    }
}
