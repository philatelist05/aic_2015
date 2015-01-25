package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation.ErrorRequestInfo;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.web.webInformation.RequestInfo;

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
    public void chainEstablished(long requestId, ChainMetaData chainMetaData) {

    }

//    @Override
//    public void chainRequestResponse(long requestId, List<ChainNodeInformation> info) {
//        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
//        ChainRequestInfo chainRequestInfo = new ChainRequestInfo(info);
//        queue.add(chainRequestInfo);
//
//        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
//        oldInfo.add(chainRequestInfo);
//    }

    @Override
    public void chainBuildUp(long requestId, ChainMetaData chainMetaData) {

    }

    //    @Override
//    public void chainBuildUpStep(long requestId, int stepNumber, ChainNodeMetaData node, boolean requestOrResponse, boolean success) {
//        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
//        ChainBuildUp chainBuildUp = new ChainBuildUp(stepNumber, node, requestOrResponse, success);
//        ChainBuildUpRequest chainBuildUpRequest = new ChainBuildUpRequest(chainBuildUp);
//        queue.add(chainBuildUpRequest);
//
//        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
//        oldInfo.add(chainBuildUpRequest);
//    }
    @Override
    public void establishedTargetConnection(long requestId, Endpoint endpoint) {
//        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
//        TargetRequestInfo targetRequestInfo = new TargetRequestInfo(info);
//        queue.add(targetRequestInfo);
//
//        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
//        oldInfo.add(targetRequestInfo);
    }


    @Override
    public void dataSent(long requestId, byte[] data) {

    }

    @Override
    public void dataReceived(long requestId, byte[] data) {

    }

    @Override
    public void chainDestroyed(long requestId) {

    }

    @Override
    public void error(long requestId, String errormsg) {
        ConcurrentLinkedQueue<RequestInfo> queue = new ConcurrentLinkedQueue<>();
        ErrorRequestInfo errorRequestInfo = new ErrorRequestInfo(errormsg);
        queue.add(errorRequestInfo);

        ConcurrentLinkedQueue<RequestInfo> oldInfo = requestInfoMap.putIfAbsent(requestId, queue);
        oldInfo.add(errorRequestInfo);
    }

    @Override
    public void info(long requestId, String msg) {

    }
}
