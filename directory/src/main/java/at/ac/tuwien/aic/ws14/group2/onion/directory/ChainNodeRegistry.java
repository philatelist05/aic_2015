package at.ac.tuwien.aic.ws14.group2.onion.directory;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsageSummary;
import at.ac.tuwien.aic.ws14.group2.onion.directory.exceptions.NoSuchChainNodeAvailable;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;


public class ChainNodeRegistry {
    static final Logger logger = LogManager.getLogger(ChainNodeRegistry.class.getName());

    private final ConcurrentHashMap<Integer, ConcurrentLinkedDeque<NodeUsage>> nodeUsages;
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

    //TODO check signature?
    public void addNodeUsage(int chainNodeID, NodeUsage usage) throws NoSuchChainNodeAvailable {
        logger.debug("Recording NodeUsage for ChainNode '{}': {}", chainNodeID, usage);

        ConcurrentLinkedDeque<NodeUsage> usages = nodeUsages.get(chainNodeID);

        if (usages == null) {
            throw new NoSuchChainNodeAvailable("Cannot record NodeUsage for unknown ID " + chainNodeID);
        } else {
            usages.addLast(usage);
            if (!activeNodes.contains(chainNodeID)) {
                activate(chainNodeID);
            }
        }
    }

    public int addNewChainNode(ChainNodeInformation chainNodeInformation) {
        if (nodeMapping.containsValue(chainNodeInformation))
            return -1;

        logger.info("Adding new ChainNode '{}'", chainNodeInformation);

        if (!localMode) {
            AmazonEC2Client ec2Client = new AmazonEC2Client(new ProfileCredentialsProvider());
            ec2Client.setRegion(Region.getRegion(Regions.fromName(chainNodeInformation.getRegion())));
            boolean instanceNotYetAvailable = true;
            logger.info("All instances:");
            for (Reservation reservation: ec2Client.describeInstances().getReservations()) {
                for (Instance instance: reservation.getInstances()) {
                    logger.info(instance.toString());
                }
            }
            while (instanceNotYetAvailable) {

                logger.info("Trying to get instance information for id '{}'", chainNodeInformation.getInstanceId());
                DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(chainNodeInformation.getInstanceId());
                try {
                    DescribeInstancesResult result = ec2Client.describeInstances(request);
                    for (Instance instance : result.getReservations().get(0).getInstances()) {
                        for (Tag tag : instance.getTags()) {
                            logger.info("Instance tagged with: " + tag.toString());
                        }
                        String availabilityZone = instance.getPlacement().getAvailabilityZone();
                        String region = availabilityZone.substring(0, availabilityZone.length()-1);
                        logger.info("Region: " + region);
                    }
                    instanceNotYetAvailable = false;
                } catch (AmazonServiceException e) {
                    logger.info("AmazonServiceException: '{}'", e.getMessage());
                    try {
                        Thread.sleep(60000);
                        continue;
                    } catch (InterruptedException e1) {
                        logger.warn("Interrupted.");
                        return -1;
                    }
                }
            }
        }
        int nodeID = nextNodeID.getAndIncrement();

        nodeMapping.put(nodeID, chainNodeInformation);
        nodeUsages.put(nodeID, new ConcurrentLinkedDeque<>());
        return nodeID;
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

    public Map<ChainNodeInformation, NodeUsageSummary> getActiveStatistics() {
        //TODO implement - calculate summaries here?
        return null;
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
                if(ids.contains(integer))
                    cni.add(chainNodeInformation);
            });
        }
        return cni;
    }

    public NodeUsage getLastNodeUsage(int chainNodeID) {
        ConcurrentLinkedDeque<NodeUsage> usages = nodeUsages.get(chainNodeID);
        return usages == null ? null : usages.getLast();
    }

    public void setLocalMode(boolean localMode) {
        this.localMode = localMode;
    }
}
