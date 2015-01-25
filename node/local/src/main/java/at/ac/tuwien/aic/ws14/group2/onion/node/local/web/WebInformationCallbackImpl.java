package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainNodeMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Stefan on 25.01.15.
 */
public class WebInformationCallbackImpl implements  WebInformationCallback {
    
    private ConcurrentHashMap<Long, ConcurrentLinkedQueue<RequestInfo>> requestInfoMap;
    
    public WebInformationCallbackImpl() {
        this.requestInfoMap = new ConcurrentHashMap<>();
    }

    @Override
    public void chainRequestResponse(long requestId, List<ChainNodeInformation> info) {
        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
        ChainRequestInfo chainRequestInfo = new ChainRequestInfo(info);
        queue.add(chainRequestInfo);
        
        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
        oldInfo.add(chainRequestInfo);
    }

    @Override
    public void chainBuildUpStep(long requestId, int stepNumber, ChainNodeMetaData node, boolean requestOrResponse, boolean success) {
        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
        ChainBuildUp chainBuildUp = new ChainBuildUp(stepNumber, node, requestOrResponse, success);
        ChainBuildUpRequest chainBuildUpRequest = new ChainBuildUpRequest(chainBuildUp);
        queue.add(chainBuildUpRequest);

        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
        oldInfo.add(chainBuildUpRequest);
    }

    @Override
    public void establishedTargetConnection(long requestId, TargetInfo info) {
        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
        TargetRequestInfo targetRequestInfo = new TargetRequestInfo(info);
        queue.add(targetRequestInfo);

        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
        oldInfo.add(targetRequestInfo);
    }

    @Override
    public void data(long requestId, byte[] data, boolean sentOrReceived) {
    }

    @Override
    public void error(long requestId, String errormsg) {
        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
        ErrorRequestInfo errorRequestInfo = new ErrorRequestInfo(errormsg);
        queue.add(errorRequestInfo);

        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
        oldInfo.add(errorRequestInfo);
    }
}
