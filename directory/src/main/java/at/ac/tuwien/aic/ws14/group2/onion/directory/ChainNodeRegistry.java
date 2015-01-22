package at.ac.tuwien.aic.ws14.group2.onion.directory;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.directory.exceptions.NoSuchChainNodeAvailable;
import at.ac.tuwien.aic.ws14.group2.onion.shared.crypto.RSASignAndVerify;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Tag;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;

import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;


public class ChainNodeRegistry {
	static final Logger logger = LogManager.getLogger(ChainNodeRegistry.class.getName());

	private final ConcurrentHashMap<Integer, NodeUsage> nodeUsages;   // stores the last NodeUsage for a node
	private final ConcurrentSkipListSet<Integer> activeNodes;
	private final ConcurrentSkipListSet<Integer> inactiveNodes;
	private final ConcurrentHashMap<Integer, ChainNodeInformation> nodeMapping;
	private final AtomicInteger nextNodeID;
	private boolean localMode = true;

	public ChainNodeRegistry() {
		logger.info("Initializing ChainNodeRegistry");
		this.activeNodes = new ConcurrentSkipListSet<>();
		this.inactiveNodes = new ConcurrentSkipListSet<>();
		this.nodeUsages = new ConcurrentHashMap<>();
		this.nodeMapping = new ConcurrentHashMap<>();
		this.nextNodeID = new AtomicInteger();
	}

	public void addNodeUsage(int chainNodeID, NodeUsage usage) throws NoSuchChainNodeAvailable {
		logger.debug("Recording NodeUsage for ChainNode '{}': {}", chainNodeID, usage);

		ChainNodeInformation nodeInformation = nodeMapping.get(chainNodeID);

		if (nodeInformation == null)
			throw new NoSuchChainNodeAvailable("Cannot record NodeUsage for unknown ID " + chainNodeID);

		if (!isSignatureValid(usage, nodeInformation))
			return;

		nodeUsages.put(chainNodeID, usage);
		if (!activeNodes.contains(chainNodeID)) {
			activate(chainNodeID);
		}
	}

	private boolean isSignatureValid(NodeUsage usage, ChainNodeInformation nodeInformation) {
		final String signature = usage.getSignature();
		// Unset signature for calculating the new signature
		usage.unsetSignature();

		final byte[] decodedSignature = Base64.decode(signature);
		try {
            final PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.decode(nodeInformation.getPublicRsaKey())));
            final byte[] data = usage.toString().getBytes(Charset.forName("UTF-8"));

            if (!RSASignAndVerify.verifySig(data, publicKey, decodedSignature)) {
                // Signature is not valid!!
                logger.warn("Signature of heartbeat message is invalid.  Ignoring this heartbeat message.");
				return false;
            }
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            logger.error("Error while checking the signature of the heart beat data. Ignoring this heartbeat message.", e);
			return false;
        }
		return true;
	}

	/**
	 * Adds a chain node to the node map.
	 *
	 * @return new chain node ID
	 */
	public int addNewChainNode(ChainNodeInformation chainNodeInformation) {
		if (nodeMapping.containsValue(chainNodeInformation))
			return -1;

		logger.info("Adding new ChainNode '{}'", chainNodeInformation);

		if (!localMode) {
			try {
				fillInfoFromAWS(chainNodeInformation);
			} catch (IllegalStateException e) {
				logger.catching(Level.WARN, e);
				return -1;
			}
		}
		int nodeID = nextNodeID.getAndIncrement();

		nodeMapping.put(nodeID, chainNodeInformation);
		return nodeID;
	}

	private void fillInfoFromAWS(ChainNodeInformation chainNodeInformation) throws IllegalStateException {
		AmazonEC2Client ec2Client = new AmazonEC2Client(new ProfileCredentialsProvider());
		ec2Client.setRegion(Region.getRegion(Regions.fromName(chainNodeInformation.getRegion())));
		boolean instanceNotYetAvailable = true;
		while (instanceNotYetAvailable) {

			logger.debug("Trying to get instance information for id '{}'", chainNodeInformation.getInstanceId());
			DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(chainNodeInformation.getInstanceId());
			try {
				DescribeInstancesResult result = ec2Client.describeInstances(request);
				for (Instance instance : result.getReservations().get(0).getInstances()) {
					for (Tag tag : instance.getTags()) {
						logger.debug("Instance tagged with: " + tag.toString());
						if (tag.getKey().equals("Name")) {
							chainNodeInformation.setInstanceName(tag.getValue());
						}
					}
				}
				instanceNotYetAvailable = false;
			} catch (AmazonServiceException e) {
				logger.warn("AmazonServiceException: '{}'", e.getMessage());
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e1) {
					throw new IllegalStateException("interrupted while filling info from AWS", e1);
				}
			}
		}
	}

	public void activate(int chainNodeID) {
		logger.info("Activating ChainNode '{}'", chainNodeID);

		synchronized (activeNodes) {
			inactiveNodes.remove(chainNodeID);
			activeNodes.add(chainNodeID);
		}
	}

	public void deactivate(int chainNodeID) {
		logger.info("Deactivating ChainNode '{}'", chainNodeID);

		synchronized (activeNodes) {
			activeNodes.remove(chainNodeID);
			inactiveNodes.add(chainNodeID);
		}
	}

	public Set<Integer> getActiveNodeIDs() {
		logger.debug("Returning active ChainNode IDs");
		return new HashSet<>(activeNodes);
	}

	public Set<ChainNodeInformation> getActiveNodes() {
		logger.debug("Returning active ChainNodes");
		Set<Integer> ids = getActiveNodeIDs();
		Set<ChainNodeInformation> cni = new HashSet<>();
		synchronized (nodeMapping) {
			nodeMapping.forEach((integer, chainNodeInformation) -> {
				if (ids.contains(integer))
					cni.add(chainNodeInformation);
			});
		}
		return cni;
	}

	public NodeUsage getLastNodeUsage(int chainNodeID) {
		return nodeUsages.get(chainNodeID);
	}

	public void setLocalMode(boolean localMode) {
		this.localMode = localMode;
	}
}
