package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Circuit;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorker;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.ConnectionWorkerFactory;
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
        BigInteger q = DHKeyExchange.generateRelativePrime();
        byte[] publicKey;
        try {
            publicKey = keyExchange.initExchange(p, q);
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

        //FIXME update when CreateCell is updated to incorporate p, q and public key
        CreateCell cell = new CreateCell(circuit.getCircuitID(), publicKey, firstNode.getEndPoint());
        sendCell(cell, circuit, callBack);
    }

    public void sendCell(Cell cell, Circuit circuit, SocksCallBack callBack) {
        // TODO use a ThreadPool for this for performance?
        try {
            ConnectionWorker connectionWorker = ConnectionWorkerFactory.getInstance().getConnectionWorker(circuit.getEndpoint());
            connectionWorker.sendCell(cell);
        } catch (IOException e) {
            logger.warn("Encountered IOException while trying to send cell to Circuit {}: {}", circuit, e.getMessage());
            callBack.error(ErrorCode.CW_FAILURE);
        }
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
        if(chains.remove(circuitID) == null) {
            success = false;
        }
        if(callbacks.remove(circuitID) == null) {
            success = false;
        }
        return success;
    }
}
