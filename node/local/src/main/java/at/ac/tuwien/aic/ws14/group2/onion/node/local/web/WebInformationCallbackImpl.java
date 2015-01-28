package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Stefan on 25.01.15.
 */
public class WebInformationCallbackImpl implements WebInformationCallback {
	private static final Logger logger = LogManager.getLogger(WebInformationCallbackImpl.class.getName());

	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<ChainMetaData>> chainEstablished;
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<ChainMetaData>> chainBuildUp;
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<Endpoint>> establishedTargetConnection;
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<byte[]>> dataSent;
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<byte[]>> dataReceived;
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<String>> error;
	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<String>> info;
	private ConcurrentHashMap<Long, Date> ids;
	private ConcurrentSkipListSet<Long> chainDestroyed;

	public WebInformationCallbackImpl() {
		this.chainEstablished = new ConcurrentHashMap<>();
		this.chainBuildUp = new ConcurrentHashMap<>();
		this.establishedTargetConnection = new ConcurrentHashMap<>();
		this.dataSent = new ConcurrentHashMap<>();
		this.dataReceived = new ConcurrentHashMap<>();
		this.error = new ConcurrentHashMap<>();
		this.info = new ConcurrentHashMap<>();
		this.chainDestroyed = new ConcurrentSkipListSet<>();
		this.ids = new ConcurrentHashMap<>();
	}

	@Override
	public void chainEstablished(long requestId, ChainMetaData chainMetaData) {
		logger.info("Received chainEstablished with id " + requestId + " and " + chainMetaData );
		ConcurrentLinkedQueue<ChainMetaData> queue = new ConcurrentLinkedQueue<>();
		queue.add(chainMetaData);

		ConcurrentLinkedQueue<ChainMetaData> oldInfo = chainEstablished.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(chainMetaData);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<ChainMetaData> getChainEstablished(long requestId) {
		ConcurrentLinkedQueue<ChainMetaData> infos = chainEstablished.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	@Override
	public void chainBuildUp(long requestId, ChainMetaData chainMetaData) {
		logger.info("Received chainBuildUp with id " + requestId + " and " + chainMetaData );
		ConcurrentLinkedQueue<ChainMetaData> queue = new ConcurrentLinkedQueue<>();
		queue.add(chainMetaData);

		ConcurrentLinkedQueue<ChainMetaData> oldInfo = chainBuildUp.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(chainMetaData);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<ChainMetaData> getChainBuildUp(long requestId) {
		ConcurrentLinkedQueue<ChainMetaData> infos = chainBuildUp.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	@Override
	public void establishedTargetConnection(long requestId, Endpoint endpoint) {
		logger.info("Received establishedTargetConnection with id " + requestId + " and " + endpoint );
		ConcurrentLinkedQueue<Endpoint> queue = new ConcurrentLinkedQueue<>();
		queue.add(endpoint);

		ConcurrentLinkedQueue<Endpoint> oldInfo = establishedTargetConnection.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(endpoint);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<Endpoint> getEstablishedTargetConnection(long requestId) {
		ConcurrentLinkedQueue<Endpoint> infos = establishedTargetConnection.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	@Override
	public void dataSent(long requestId, byte[] data) {
		logger.info("Received dataSent with id " + requestId + " and " + data );
		ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();
		queue.add(data);

		ConcurrentLinkedQueue<byte[]> oldInfo = dataSent.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(data);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<byte[]> getDataSent(long requestId) {
		ConcurrentLinkedQueue<byte[]> infos = dataSent.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	@Override
	public void dataReceived(long requestId, byte[] data) {
		logger.info("Received dataSent with id " + requestId + " and " + data );
		ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<>();
		queue.add(data);

		ConcurrentLinkedQueue<byte[]> oldInfo = dataReceived.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(data);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<byte[]> getDataReceived(long requestId) {
		ConcurrentLinkedQueue<byte[]> infos = dataReceived.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	@Override
	public void chainDestroyed(long requestId) {
		logger.info("Received chainDestroyed with id " + requestId);
		chainDestroyed.add(requestId);
		ids.putIfAbsent(requestId, new Date());
	}

	public boolean isChainDestroyed(long requestId) {
		return chainDestroyed.contains(requestId);
	}

	@Override
	public void error(long requestId, String errormsg) {
		logger.info("Received error with id " + requestId + " and " + errormsg );
		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
		queue.add(errormsg);

		ConcurrentLinkedQueue<String> oldInfo = error.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(errormsg);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<String> getError(long requestId) {
		ConcurrentLinkedQueue<String> infos = error.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	@Override
	public void info(long requestId, String msg) {
		logger.info("Received error with id " + requestId + " and " + msg);
		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
		queue.add(msg);

		ConcurrentLinkedQueue<String> oldInfo = info.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(msg);
		ids.putIfAbsent(requestId, new Date());
	}

	public List<String> getInfo(long requestId) {
		ConcurrentLinkedQueue<String> infos = info.getOrDefault(requestId, new ConcurrentLinkedQueue<>());
		return new LinkedList<>(infos);
	}

	public Map<Long, Date> getIds() {
		return new HashMap<>(ids);
	}
}
