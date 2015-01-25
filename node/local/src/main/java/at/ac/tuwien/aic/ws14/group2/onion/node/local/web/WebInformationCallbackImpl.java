package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.directory.api.service.ChainNodeInformation;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainNodeMetaData;
import com.sun.xml.internal.fastinfoset.util.CharArrayIntMap;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

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
        ChainBuildUp chainBuildUp = new ChainBuildUp();
        chainBuildUp.stepNumber = stepNumber;
        chainBuildUp.node = node;
        chainBuildUp.requestOrResponse = requestOrResponse;
        chainBuildUp.success = success;
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
    
    private interface RequestInfo <T> {
        T getInfo();
    }

    private class ChainRequestInfo implements RequestInfo<List<ChainNodeInformation>> {
        private final List<ChainNodeInformation> list;

        private ChainRequestInfo(List<ChainNodeInformation> list){
            this.list = list;
        }
        @Override
        public List<ChainNodeInformation> getInfo() {
            return list;
        }
    }

    private class TargetRequestInfo implements RequestInfo<TargetInfo> {
        private final TargetInfo info;

        private TargetRequestInfo(TargetInfo info) {
            this.info = info;
        }

        @Override
        public TargetInfo getInfo() {
            return info;
        }
    }

    private class ErrorRequestInfo implements RequestInfo<String> {
        private final String error;

        private ErrorRequestInfo(String error) {
            this.error = error;
        }

        @Override
        public String getInfo() {
            return error;
        }
    }

    private class ChainBuildUpRequest implements RequestInfo<ChainBuildUp> {
        private final ChainBuildUp chainBuildUp;

        private ChainBuildUpRequest(ChainBuildUp chainBuildUp) {
            this.chainBuildUp = chainBuildUp;
        }

        @Override
        public ChainBuildUp getInfo() {
            return chainBuildUp;
        }
    }

    private class ChainBuildUp {
        private int stepNumber;
        private ChainNodeMetaData node;
        private boolean requestOrResponse;
        private boolean success;
    }
}
