package at.ac.tuwien.aic.ws14.group2.onion.common;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * Created by stefan on 30.11.14.
 */
public class ConfigurationFactory {
	public static final String CONFIG_FILE = "config.xml";

	public static Configuration createConfig() throws ConfigurationException {
		try {
			XMLConfiguration config = new XMLConfiguration();
			config.setFileName(CONFIG_FILE);
			config.setSchemaValidation(true);

			config.load();

			return mapConfig(config);
		} catch (org.apache.commons.configuration.ConfigurationException e) {
			throw new ConfigurationException(e);
		}
	}

	private static Configuration mapConfig(XMLConfiguration xmlConfiguration) {
		String nodeCommonHost = xmlConfiguration.getString("node.common.host", "localhost");
		int nodeCommonPort = xmlConfiguration.getInt("node.common.port", 9090);
		int localNodeServerPort = xmlConfiguration.getInt("node.local.server-port", 1080);
		String localNodeListeningHost = xmlConfiguration.getString("node.local.listening-host", "localhost");
		int localNodeNumCellWorkers = xmlConfiguration.getInt("node.local.cellworkers-per-connectionworker", 5);
		long chainNodeHeartbeatInterval = xmlConfiguration.getLong("node.chain.heartbeat-interval", 2000);
		int chainNodeNumCellWorkers = xmlConfiguration.getInt("node.chain.cellworkers-per-connectionworker", 20);
		long directoryNodeHeartbeatTimeout = xmlConfiguration.getLong("node.directory.heartbeat-timeout", 7000);
		int directoryNodeMinThriftWorker = xmlConfiguration.getInt("node.directory.thriftworker.min", 3);
		int directoryNodeMaxThriftWorker = xmlConfiguration.getInt("node.directory.thriftworker.max", 16);
		String targetServiceHost = xmlConfiguration.getString("node.target-service.host", "localhost");
		int targetServicePort = xmlConfiguration.getInt("node.target-service.port", 8080);

		return new Configuration(nodeCommonHost, nodeCommonPort, localNodeServerPort, localNodeListeningHost,
				localNodeNumCellWorkers, chainNodeHeartbeatInterval, chainNodeNumCellWorkers,
				directoryNodeHeartbeatTimeout, directoryNodeMinThriftWorker, directoryNodeMaxThriftWorker,
				targetServiceHost, targetServicePort);
	}
}
