package at.ac.tuwien.aic.ws14.group2.onion.directory.worker;

import at.ac.tuwien.aic.ws14.group2.onion.directory.ChainNodeRegistry;
import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.NodeUsage;
import at.ac.tuwien.aic.ws14.group2.onion.shared.Configuration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*;
import com.google.common.io.ByteStreams;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class ChainNodeMonitor implements Runnable {
    static final Logger logger = LogManager.getLogger(ChainNodeMonitor.class.getName());

    private ChainNodeRegistry chainNodeRegistry;
    private long timeout;
    private short numberOfNodes = -1;
    private String region = null;
    private String userData;
    private AmazonEC2Client ec2Client;
    private Image image;
    private SecurityGroup securityGroup;
    private boolean test = true;

    public ChainNodeMonitor(ChainNodeRegistry chainNodeRegistry, Configuration config) {
        this.chainNodeRegistry = chainNodeRegistry;
        this.timeout = config.getDirectoryNodeHeartbeatTimeout();
        if (config.isLocalMode() || !config.isDirectoryAutoStart()) {
            logger.info("Starting chain node monitor without loadbalancing.");
        } else {
            logger.info("Starting chain node monitor with loadbalancing: {} nodes in region '{}'", config.getDirectoryNumberOfNodes(), config.getDirectoryAutoStartRegion());
            this.numberOfNodes = config.getDirectoryNumberOfNodes();
            this.region = config.getDirectoryAutoStartRegion();

            if (this.numberOfNodes > 0 && this.region != null) {
                this.ec2Client = new AmazonEC2Client(new ProfileCredentialsProvider());
                ec2Client.setEndpoint(this.region);

                DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withFilters(new Filter("description").withValues("G2-T3-template"));
                DescribeImagesResult describeImagesResult = ec2Client.describeImages(describeImagesRequest);
                logger.debug("Images:");
                for (Image image : describeImagesResult.getImages()) {
                    logger.debug(image.toString());
                }
                this.image = describeImagesResult.getImages().get(0);

                DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest().withFilters(new Filter("group-name").withValues("G2-T3"));
                DescribeSecurityGroupsResult describeSecurityGroupsResult = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest);
                logger.debug("Security groups:");
                for (SecurityGroup securityGroup : describeSecurityGroupsResult.getSecurityGroups()) {
                    logger.debug(securityGroup.toString());
                }
                this.securityGroup = describeSecurityGroupsResult.getSecurityGroups().get(0);

                InputStream startScript = this.getClass().getClassLoader().getResourceAsStream("startchainnode.sh");

                try {
                    this.userData = Base64.getEncoder().encodeToString(ByteStreams.toByteArray(startScript));
                } catch (IOException e) {
                    logger.warn("Startscript not found - disabling loadbalancing..");
                    this.numberOfNodes = 0;
                    this.region = null;
                }
            }
        }
    }

    @Override
    public void run() {
        logger.info("Starting health check");

        Set<Integer> activeNodes = chainNodeRegistry.getActiveNodeIDs();

        if (activeNodes == null || activeNodes.isEmpty()) {
            logger.warn("No active ChainNodes, nothing to do..");
        } else {
            logger.info("Found {} active nodes.", activeNodes.size());
            for (Integer nodeID : activeNodes) {
                NodeUsage usage = chainNodeRegistry.getLastNodeUsage(nodeID);
                if (usage != null) {
                    try {
                        LocalDateTime then = LocalDateTime.parse(usage.getEndTime(), DateTimeFormatter.ISO_DATE_TIME);
                        LocalDateTime now = LocalDateTime.now();
                        if (then.until(now, ChronoUnit.MILLIS) > timeout) {
                            chainNodeRegistry.deactivate(nodeID);
                        }
                    } catch (DateTimeParseException e) {
                        logger.warn("Cannot parse endDate '{}' of NodeUsageSummary for ChainNode '{}'", usage.getEndTime(), nodeID);
                        logger.debug(e.getStackTrace());
                    } catch (ArithmeticException e) {
                        logger.warn("Overflow occurred while calculation timeout, deactivating..");
                        logger.catching(Level.DEBUG, e);
                        chainNodeRegistry.deactivate(nodeID);
                    }
                }
            }
        }

        if (this.region != null && this.numberOfNodes > 0) {
            activeNodes = chainNodeRegistry.getActiveNodeIDs();
            if (activeNodes.size() < this.numberOfNodes) {
                logger.info("Not enough active nodes, starting new instances..");
                RunInstancesRequest request = new RunInstancesRequest()
                        .withImageId(image.getImageId())
                        .withSecurityGroupIds(securityGroup.getGroupId())
                        .withInstanceType(InstanceType.T2Micro)
                        .withMinCount(1)
                        .withMaxCount(this.numberOfNodes - activeNodes.size())
                        .withUserData(this.userData);
                if (this.test) {
                    logger.info("Request: {}", request.toString());
                    RunInstancesResult result = ec2Client.runInstances(request);
                    this.test = false;
                    for (Instance instance : result.getReservation().getInstances()) {
                        Collection<Tag> tags = instance.getTags();
                        CreateTagsRequest tagsRequest = new CreateTagsRequest().withTags(new Tag("Name", "G2-T3-chainnode-" + UUID.randomUUID().toString())).withResources(instance.getInstanceId());
                        ec2Client.createTags(tagsRequest);
                    }
                    logger.info("Result: {}", result.toString());
                }
            }

        }

        logger.info("Finished health check");
    }
}
