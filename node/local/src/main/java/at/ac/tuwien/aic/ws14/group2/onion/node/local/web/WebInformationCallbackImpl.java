package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Stefan on 25.01.15.
 */
public class WebInformationCallbackImpl implements WebInformationCallback {
	private static final Logger logger = LogManager.getLogger(WebInformationCallbackImpl.class.getName());

	private ConcurrentHashMap<Long, ConcurrentLinkedQueue<RequestInfo>> requestInfoMap;

	public WebInformationCallbackImpl() {
		this.requestInfoMap = new ConcurrentHashMap<>();
	}

	@Override
	public void chainEstablished(long requestId, ChainMetaData chainMetaData) {
		logger.info("Received chainEstablished with id " + requestId + " and " + chainMetaData );
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		ChainEstablishedInfo chainRequestInfo = new ChainEstablishedInfo(chainMetaData);
		queue.add(chainRequestInfo);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(chainRequestInfo);
	}

	@Override
	public void chainBuildUp(long requestId, ChainMetaData chainMetaData) {
		logger.info("Received chainBuildUp with id " + requestId + " and " + chainMetaData );
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		ChainBuildUpRequest chainBuildUpRequest = new ChainBuildUpRequest(chainMetaData);
		queue.add(chainBuildUpRequest);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(chainBuildUpRequest);
	}

	@Override
	public void establishedTargetConnection(long requestId, Endpoint endpoint) {
		logger.info("Received establishedTargetConnection with id " + requestId + " and " + endpoint );
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		TargetRequestInfo targetRequestInfo = new TargetRequestInfo(endpoint);
		queue.add(targetRequestInfo);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(targetRequestInfo);
	}


	@Override
	public void dataSent(long requestId, byte[] data) {
		logger.info("Received dataSent with id " + requestId + " and " + data );
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		DataSentRequest dataSentRequest = new DataSentRequest(data);
		queue.add(dataSentRequest);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(dataSentRequest);
	}

	@Override
	public void dataReceived(long requestId, byte[] data) {
		logger.info("Received dataSent with id " + requestId + " and " + data );
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		DataReceivedRequest dataReceivedRequest = new DataReceivedRequest(data);
		queue.add(dataReceivedRequest);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(dataReceivedRequest);
	}

	@Override
	public void chainDestroyed(long requestId) {
		logger.info("Received chainDestroyed with id " + requestId);
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		ChainDestroyedRequest chainDestroyedRequest = new ChainDestroyedRequest();
		queue.add(chainDestroyedRequest);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(chainDestroyedRequest);
	}

	@Override
	public void error(long requestId, String errormsg) {
		logger.info("Received error with id " + requestId + " and " + errormsg );
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		ErrorRequestInfo errorRequestInfo = new ErrorRequestInfo(errormsg);
		queue.add(errorRequestInfo);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(errorRequestInfo);
	}

	@Override
	public void info(long requestId, String msg) {
		logger.info("Received error with id " + requestId + " and " + msg);
		ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
		InfoRequest infoRequest = new InfoRequest(msg);
		queue.add(infoRequest);

		ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
		if (oldInfo != null)
			oldInfo.add(infoRequest);
	}

	public Enumeration<Long> getIds() {
		return requestInfoMap.keys();
	}
}
