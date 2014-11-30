package at.ac.tuwien.aic.ws14.group2.onion.node.chain.node;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.Cell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.CreateResponseCell;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.cells.DHHalf;
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
        if (circuit == null && cell instanceof CreateCell) {
            try {
                connectionWorker.sendCell(handleCreateCell());
            } catch (IOException e) {
                logger.warn("Could not send response/forwarded cell: {}", e.getMessage());
                logger.catching(Level.DEBUG, e);
            }
        }
        //TODO implement other cells
    }

    private Cell handleCreateCell() {
        CreateCell createCell = (CreateCell) cell;
        Circuit newCircuit = new Circuit(createCell.getCircuitID(), createCell.getEndpoint());
        try {
            connectionWorker.addCircuit(newCircuit);
        } catch (CircuitIDExistsAlreadyException e) {
            logger.warn("Circuit ID race condition happened for node at {}", createCell.getEndpoint());
            //TODO send ErrorCell
            return null;
        }

        DHHalf dhHalf = createCell.getDHHalf().decrypt(this.privateKey);

        byte[] sharedSecret;
        byte[] dhPublicKey;
        try {
            DHKeyExchange keyExchange = new DHKeyExchange();
            dhPublicKey = keyExchange.initExchange(dhHalf.getP(), dhHalf.getG());
            sharedSecret = keyExchange.completeExchange(dhHalf.getPublicKey());
        } catch (NoSuchProviderException e) {
            logger.warn("Could not find BouncyCastle provider: {}", e.getMessage());
            //TODO send ErrorCell
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Could not find DiffieHellman algorithm: {}", e.getMessage());
            //TODO send ErrorCell
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            logger.warn("Invalid p and q for DH exchange: {}", e.getMessage());
            //TODO send ErrorCell
            return null;
        } catch (InvalidKeyException e) {
            logger.warn("Invalid key for DH exchange: {}", e.getMessage());
            //TODO send ErrorCell
            return null;
        } catch (InvalidKeySpecException e) {
            logger.warn("Invalid keyspec for DH exchange: {}", e.getMessage());
            //TODO send ErrorCell
            return null;
        }

        newCircuit.setSessionKey(sharedSecret);
        return new CreateResponseCell(newCircuit.getCircuitID(), dhPublicKey, RSASignAndVerify.signData(dhPublicKey, this.privateKey));
    }
}
