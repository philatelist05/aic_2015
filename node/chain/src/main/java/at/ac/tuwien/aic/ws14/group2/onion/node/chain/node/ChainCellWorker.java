package at.ac.tuwien.aic.ws14.group2.onion.node.chain.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.chain.heartbeat.UsageCollector;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.*;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CircuitIDExistsAlreadyException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.*;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.RSASignAndVerify;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.DecryptException;
import at.ac.tuwien.aic.ws14.group2.onion.shared.exception.EncryptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.*;
import java.util.Arrays;

public class ChainCellWorker implements CellWorker {
    static final Logger logger = LogManager.getLogger(ChainCellWorker.class.getName());

    private final Circuit circuit;
    private final Cell cell;
    private final ConnectionWorker connectionWorker;
    private final PrivateKey privateKey;
    private final ConnectionWorkerFactory connectionWorkerFactory;
    private final Endpoint endpoint;

    public ChainCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit, Endpoint endpoint, PrivateKey privateKey) {
        this(connectionWorker, cell, circuit, endpoint, privateKey, ConnectionWorkerFactory.getInstance());
    }

    ChainCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit, Endpoint endpoint, PrivateKey privateKey, ConnectionWorkerFactory connectionWorkerFactory) {
        this.endpoint = endpoint;
        this.connectionWorker = connectionWorker;
        this.cell = cell;
        this.circuit = circuit;
        this.privateKey = privateKey;
        this.connectionWorkerFactory = connectionWorkerFactory;
    }

    @Override
    public void run() {
        logger.info("Got cell {} on circuit {}", cell, circuit);

        try {
            if (circuit == null && cell instanceof CreateCell) {
                handleCreateCell();
            } else if (cell instanceof CreateResponseCell) {
                handleCreateResponseCell();
            } else if (cell instanceof RelayCell) {
                handleRelayCell();
            } else if (cell instanceof DestroyCell) {
                handleDestroyCell();
            } else {
                logger.error("Cannot handle cell {}, so shutting down chain.", cell.getClass().getName());

                shutdownChain(circuit);
            }
        } catch (Exception e) {
            logger.error("Exception while handling cell, so shutting down chain.", e);

            shutdownChain(circuit);
        }
    }

    private void handleCreateCell() throws IOException, DecryptException {
        UsageCollector.incrementCreateCounter();

        CreateCell createCell = (CreateCell) cell;
        Circuit newCircuit = new Circuit(createCell.getCircuitID(), createCell.getEndpoint());
        try {
            connectionWorker.addCircuit(newCircuit);
            UsageCollector.setCircuitCount(connectionWorker.getCircuitCount());
        } catch (CircuitIDExistsAlreadyException e) {
            logger.warn("Circuit ID race condition happened for node at {}", createCell.getEndpoint());

            connectionWorker.sendCell(new CreateResponseCell(createCell.getCircuitID(), CreateStatus.CircuitIDAlreadyExists));
            return;
        }

        DHHalf dhHalf = createCell.getDHHalf().decrypt(this.privateKey);

        byte[] sharedSecret;
        byte[] dhPublicKey;
        try {
            DHKeyExchange keyExchange = new DHKeyExchange();
            dhPublicKey = keyExchange.initExchange(dhHalf.getP(), dhHalf.getG());
            sharedSecret = keyExchange.completeExchange(dhHalf.getPublicKey());
        } catch (Exception e) {
            logger.error("Cannot initiate DH key exchange, so shutting down chain.", e);

            // unrecoverable error
            shutdownChain(circuit);
            return;
        }

        UsageCollector.incrementChainCounter();

        newCircuit.setSessionKey(sharedSecret);
        connectionWorker.sendCell(new CreateResponseCell(newCircuit.getCircuitID(), dhPublicKey, RSASignAndVerify.signData(dhPublicKey, this.privateKey)));
    }

    private void handleCreateResponseCell() throws IOException {
        CreateResponseCell createResponseCell = (CreateResponseCell)cell;
        Circuit assocCircuit = circuit.getAssociatedCircuit();

        if (createResponseCell.getStatus() == CreateStatus.CircuitIDAlreadyExists) {
            connectionWorker.removeCircuit(circuit);

            extendChain(connectionWorker, assocCircuit, circuit.getEndpoint(), circuit.getDHHalf());
        } else {
            ExtendResponseCommand cmd = new ExtendResponseCommand(createResponseCell.getDhPublicKey(), createResponseCell.getSignature());
            RelayCellPayload payload = new RelayCellPayload(cmd);
            try {
                payload = payload.encrypt(assocCircuit.getSessionKey());
            } catch (EncryptException e) {
                logger.warn("encryption failed");
                connectionWorker.removeCircuit(circuit);
                extendChain(connectionWorker, assocCircuit, circuit.getEndpoint(), circuit.getDHHalf());
            }
            RelayCell cell = new RelayCell(assocCircuit.getCircuitID(), payload);

            ConnectionWorker incomingConnectionWorker = connectionWorkerFactory.getConnectionWorker(assocCircuit.getEndpoint());
            incomingConnectionWorker.sendCell(cell);
        }

        UsageCollector.setCircuitCount(connectionWorker.getCircuitCount());
    }

    private void handleRelayCell() throws Exception {
        UsageCollector.incrementRelayCounter();

        RelayCell relayCell = (RelayCell)cell;

        if (circuit.getAssociatedCircuit() == null) {   // unencrypted payload coming from local node
            logger.debug("Final destination of relay cell");
            logger.debug("Encrypted payload: {}", relayCell.getPayload());
            logger.debug("Decrypting with {} as session key", Arrays.toString(circuit.getSessionKey()));
            RelayCellPayload decryptedPayload = relayCell.getPayload().decrypt(circuit.getSessionKey());
            logger.debug("Decrypted payload: {}", decryptedPayload);
            Command cmd = decryptedPayload.decode();
            if (cmd instanceof ExtendCommand) {
                handleExtendCommand((ExtendCommand)cmd);
            } else if (cmd instanceof ConnectCommand) {
                handleConnectCommand((ConnectCommand)cmd);
            } else if (cmd instanceof DataCommand) {
                handleDataCommand((DataCommand)cmd);
            } else {
                logger.error("Chain node is in invalid state in order to handle command {}, so shutting down chain.", cmd.getClass().getName());

                shutdownChain(circuit);
            }
        } else if (circuit.getSessionKey() == null) {   // coming from target
            logger.info("Adding another layer of encryption to relay cell");
            // add layer of encryption
            Circuit assocCircuit = circuit.getAssociatedCircuit();
            RelayCellPayload newPayload = relayCell.getPayload().encrypt(assocCircuit.getSessionKey());
            RelayCell newRelayCell = new RelayCell(assocCircuit.getCircuitID(), newPayload);

            // forward
            ConnectionWorker assocConnectionWorker = connectionWorkerFactory.getConnectionWorker(assocCircuit.getEndpoint());
            assocConnectionWorker.sendCell(newRelayCell);
        } else {   // coming from local node
            logger.info("Stripping another layer of encryption from the relay cell");
            logger.debug("Encrypted payload: {}", relayCell.getPayload());
            // remove layer of encryption and forward
            Circuit assocCircuit = circuit.getAssociatedCircuit();
            logger.debug("Decrypting with {} as session key", Arrays.toString(circuit.getSessionKey()));
            RelayCellPayload decryptedPayload = relayCell.getPayload().decrypt(circuit.getSessionKey());
            logger.debug("Decrypted payload: {}", decryptedPayload);
            RelayCell newRelayCell = new RelayCell(assocCircuit.getCircuitID(), decryptedPayload);

            // forward
            ConnectionWorker assocConnectionWorker = connectionWorkerFactory.getConnectionWorker(assocCircuit.getEndpoint());
            assocConnectionWorker.sendCell(newRelayCell);
        }
    }

    private void handleDestroyCell() throws IOException {
        logger.info("Handling DestroyCell");

        DestroyCell receivedCell = (DestroyCell)cell;
        Circuit assocCircuit = circuit.getAssociatedCircuit();

        if (assocCircuit != null)
            connectionWorker.sendCell(new DestroyCell(assocCircuit.getCircuitID()));

        connectionWorker.removeCircuit(circuit);
        connectionWorker.removeTargetWorker(circuit);

        if (assocCircuit != null) {
            connectionWorker.removeCircuit(assocCircuit);
            connectionWorker.removeTargetWorker(assocCircuit);
        }

        UsageCollector.setCircuitCount(connectionWorker.getCircuitCount());
        UsageCollector.setTargetCount(connectionWorker.getTargetWorkerCount());
    }

    private void handleExtendCommand(ExtendCommand cmd) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        logger.info("Handling ExtendCommand: {}", cmd);
        ConnectionWorker outgoingConnectionWorker = connectionWorkerFactory.getConnectionWorker(cmd.getEndpoint());
        extendChain(outgoingConnectionWorker, circuit, cmd.getEndpoint(), cmd.getDHHalf());
    }

    private void handleConnectCommand(ConnectCommand cmd) throws IOException {
        logger.info("Handling ConnectCommand: {}", cmd);
        connectionWorker.getOrCreateTargetWorker(circuit, cmd.getEndpoint());
        UsageCollector.setTargetCount(connectionWorker.getTargetWorkerCount());
    }

    private void handleDataCommand(DataCommand cmd) throws IOException {
        logger.info("Handling DataCommand: {}", cmd);

        TargetWorker tw = connectionWorker.getOrCreateTargetWorker(circuit, null);
        UsageCollector.setTargetCount(connectionWorker.getTargetWorkerCount());

        tw.sendData(cmd.getData(), cmd.getSequenceNumber());
    }

    private void extendChain(ConnectionWorker connectionWorker, Circuit incomingCircuit, Endpoint nextNode, EncryptedDHHalf dhHalf) throws IOException {
        // create circuit
        Circuit outgoingCircuit = connectionWorker.createAndAddCircuit(nextNode);
        outgoingCircuit.setAssociatedCircuit(incomingCircuit);
        incomingCircuit.setAssociatedCircuit(outgoingCircuit);

        UsageCollector.setCircuitCount(connectionWorker.getCircuitCount());

        // remember DH half in case we have to retry the operation
        outgoingCircuit.setDHHalf(dhHalf);

        CreateCell createCell = new CreateCell(outgoingCircuit.getCircuitID(), this.endpoint, dhHalf);

        connectionWorker.sendCell(createCell);
    }

    /**
     * Sends DestroyCells over the specified and its associated circuit and
     * removes the circuits from the connection worker.
     *
     * Used to react on unrecoverable errors.
     */
    private void shutdownChain(Circuit circuit) {
        Circuit assocCircuit = circuit.getAssociatedCircuit();

        try {
            connectionWorker.sendCell(new DestroyCell(circuit.getCircuitID()));
            connectionWorker.removeCircuit(circuit);
            connectionWorker.removeTargetWorker(circuit);

            if (assocCircuit != null) {
                connectionWorker.sendCell(new DestroyCell(assocCircuit.getCircuitID()));
                connectionWorker.removeCircuit(assocCircuit);
                connectionWorker.removeTargetWorker(assocCircuit);
            }
        } catch (IOException e) {
            logger.warn("Could not send DestroyCell during chain destruction.", e);
        }

        UsageCollector.setCircuitCount(connectionWorker.getCircuitCount());
        UsageCollector.setTargetCount(connectionWorker.getTargetWorkerCount());
        UsageCollector.decrementChainCounter();
    }
}
