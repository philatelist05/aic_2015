package at.ac.tuwien.aic.ws14.group2.onion.node.chain.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.*;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.AESAlgorithm;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.DHKeyExchange;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.crypto.RSASignAndVerify;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.exceptions.CircuitIDExistsAlreadyException;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;

public class ChainCellWorker implements CellWorker {
    static final Logger logger = LogManager.getLogger(ChainCellWorker.class.getName());

    private final Circuit circuit;
    private final Cell cell;
    private final ConnectionWorker connectionWorker;
    private final PrivateKey privateKey;

    public ChainCellWorker(ConnectionWorker connectionWorker, Cell cell, Circuit circuit, PrivateKey privateKey) {
        this.connectionWorker = connectionWorker;
        this.cell = cell;
        this.circuit = circuit;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        try {
            if (circuit == null && cell instanceof CreateCell) {
                handleCreateCell();
            } else if (cell instanceof DestroyCell) {
                handleDestroyCell();
            } else {
                logger.error("Cannot handle cell {}", cell.getClass().getName());
            }
        } catch (IOException e) {
            logger.warn("Could not send response/forwarded cell: {}", e.getMessage());
            logger.catching(Level.DEBUG, e);
        }
        //TODO implement other cells
    }

    private void handleCreateCell() throws IOException {
        CreateCell createCell = (CreateCell) cell;
        Circuit newCircuit = new Circuit(createCell.getCircuitID(), createCell.getEndpoint());
        try {
            connectionWorker.addCircuit(newCircuit);
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
            logger.error("Cannot initiate DH key exchange.", e);

            // unrecoverable error
            shutdownChain(circuit);
            return;
        }

        newCircuit.setSessionKey(sharedSecret);
        connectionWorker.sendCell(new CreateResponseCell(newCircuit.getCircuitID(), dhPublicKey, RSASignAndVerify.signData(dhPublicKey, this.privateKey)));
    }

    private void handleDestroyCell() throws IOException {
        DestroyCell receivedCell = (DestroyCell)this.cell;
        Circuit assocCircuit = circuit.getAssociatedCircuit();

        if (assocCircuit != null)
            connectionWorker.sendCell(new DestroyCell(assocCircuit.getCircuitID()));

        connectionWorker.removeCircuit(circuit);
        connectionWorker.removeTargetWorker(circuit);

        if (assocCircuit != null) {
            connectionWorker.removeCircuit(assocCircuit);
            connectionWorker.removeTargetWorker(assocCircuit);
        }
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
            logger.error("Could not send DestroyCell during chain destruction.", e);
        }
    }
}
