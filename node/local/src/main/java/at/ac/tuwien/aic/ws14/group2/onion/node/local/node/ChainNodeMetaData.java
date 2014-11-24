package at.ac.tuwien.aic.ws14.group2.onion.node.local.node;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class ChainNodeMetaData {
    static final Logger logger = LogManager.getLogger(ChainNodeMetaData.class.getName());

    private PublicKey publicKey;
    private Endpoint endPoint;
    private byte[] sessionKey;

    public ChainNodeMetaData(PublicKey publicKey, Endpoint endPoint, byte[] sessionKey) {
        this.publicKey = publicKey;
        this.endPoint = endPoint;
        this.sessionKey = sessionKey;
    }

    //TODO FG throw exception instead of returning null?
    public static ChainNodeMetaData fromChainNodeInformation(ChainNodeInformation information) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA", "BC");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("Could not find RSA algorithm: {}", e.getMessage());
            return null;
        } catch (NoSuchProviderException e) {
            logger.warn("Could not find BouncyCastle provider: {}", e.getMessage());
            return null;
        }

        byte[] decodedPublicKey = Base64.decode(information.getPublicRsaKey());
        PublicKey publicKey;
        try {
            publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(decodedPublicKey));
        } catch (InvalidKeySpecException e) {
            logger.warn("Invalid KeySpec encountered: {}", e.getMessage());
            return null;
        }

        InetAddress address = null;
        try {
            address = InetAddress.getByName(information.getAddress());
        } catch (UnknownHostException e) {
            logger.warn("Could not find endpoint host: {}", e.getMessage());
            return null;
        }
        Endpoint endpoint = new Endpoint(address, information.getPort());

        return new ChainNodeMetaData(publicKey, endpoint, null);
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public Endpoint getEndPoint() {
        return endPoint;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }
}
