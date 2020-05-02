package com.distributedsystems.distributedcache.controller;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.consistency.BroadcastStatus;
import com.distributedsystems.distributedcache.consistency.ConsistencyImplInterface;
import com.distributedsystems.distributedcache.consistency.ConsistencyRequest;
import com.distributedsystems.distributedcache.consistency.ConsistencyResolver;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;


import java.util.HashMap;
import java.util.Optional;
import java.util.PriorityQueue;


@GRpcService
public class ControllerHandler extends ControllerServiceGrpc.ControllerServiceImplBase{

    @Autowired
    ControllerConfigurations appConfig;

    @Autowired
    ConsistencyResolver consistencyResolver;

    private int requestId;
    private HashMap<String, BroadcastStatus> pendingRequests = new HashMap<>();
    private HashMap<Controller.ReadRequest, StreamObserver<Controller.ReadResponse>> pendingGetRequestsMap = new HashMap<>();
    private PriorityBlockingQueue<Controller.ReadRequest> pendingGetRequestsQueue = new PriorityBlockingQueue<Controller.ReadRequest>(
            1000,
            new Comparator<Controller.ReadRequest>() {
                @Override
                public int compare(Controller.ReadRequest o1, Controller.ReadRequest o2) {
                    String[] r1 = o1.getTimeStamp().split(".");
                    String[] r2 = o2.getTimeStamp().split(".");
                    return Integer.parseInt(r1[0]) - Integer.parseInt(r2[0]);
                }
            }
    );

    @Override
    public void get(Controller.ReadRequest request, StreamObserver<Controller.ReadResponse> responseObserver) {

        Optional<ConsistencyImplInterface> consistencyImpl = consistencyResolver.resolveConsistency(request.getConsistencyLevel());
        if(consistencyImpl.isPresent()) {
            ConsistencyRequest consistencyRequest = new ConsistencyRequest();
            consistencyRequest.setKey(request.getKey());
            consistencyRequest.setPendingRequests(pendingRequests);
            if(!request.getConsistencyLevel().equals("4")) {
                consistencyRequest.setLamportClock(getLamportClock());
                Controller.ReadResponse response = consistencyImpl.get().read(consistencyRequest);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            else {
                consistencyRequest.setLamportClock(String.valueOf(this.requestId));
                if (getClientTimeStamp(request.getTimeStamp()) > this.requestId) {
                    pendingGetRequestsQueue.offer(request);
                    pendingGetRequestsMap.put(request, responseObserver);
                }
                else{
                    Controller.ReadResponse response = consistencyImpl.get().read(consistencyRequest);
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            }
        }else{
            responseObserver.onError(new Exception("Unimplemented consistency option"));
        }
    }

    @Override
    public void put(Controller.WriteRequest request, StreamObserver<Controller.WriteResponse> responseObserver) {

        Optional<ConsistencyImplInterface> consistencyImpl = consistencyResolver.resolveConsistency(request.getConsistencyLevel());
        if(consistencyImpl.isPresent()) {
            ConsistencyRequest consistencyRequest = new ConsistencyRequest();
            consistencyRequest.setKey(request.getKey());
            consistencyRequest.setValue(request.getValue());
            consistencyRequest.setPendingRequests(pendingRequests);
            if(!request.getConsistencyLevel().equals("4"))
                consistencyRequest.setLamportClock(getLamportClock(request));
            else
                consistencyRequest.setLamportClock(String.valueOf(this.requestId));
            Controller.WriteResponse response = consistencyImpl.get().write(consistencyRequest);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }else{
            responseObserver.onError(new Exception("Unimplemented consistency option"));
        }
    }



    @Override
    public void broadcastRequestAcknowledgement(Controller.Ack request, StreamObserver<Controller.broadcastRequestAcknowledgementResponse> responseObserver) {
        if(pendingRequests.containsKey(request.getLamportClock())) {
            BroadcastStatus status = pendingRequests.get(request.getLamportClock());
            status.setValue(request.getValueRead());
            status.setCompletedAndNotify(true);
        }
        responseObserver.onNext(Controller.broadcastRequestAcknowledgementResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private String getLamportClock(){
        synchronized (this){
            this.requestId += 1;
        }
        return appConfig.controllerId+"."+requestId;
    }

    private String getLamportClock(Controller.WriteRequest request) {
        synchronized (this){
            this.requestId = Math.max(this.requestId,getClientTimeStamp(request.getTimeStamp()));
            this.requestId += 1;
            processPendingGetRequests();
        }
        return appConfig.controllerId+"."+requestId;
    }

    private void processPendingGetRequests() {
        while(!pendingGetRequestsQueue.isEmpty() && getClientTimeStamp(pendingGetRequestsQueue.peek().getTimeStamp()) <= this.requestId){
                Optional<ConsistencyImplInterface> consistencyImpl = consistencyResolver.resolveConsistency(Controller.ConsistencyLevel.CAUSAL);
                Controller.ReadRequest request= pendingGetRequestsQueue.poll();
                StreamObserver<Controller.ReadResponse> responseObserver = pendingGetRequestsMap.get(request);
                pendingGetRequestsMap.remove(request);
                this.get(request,responseObserver);
        }
    }

    private int getClientTimeStamp(String lamportTimeStamp){
        String[] clientTimeStamp = lamportTimeStamp.split(".");
        return Integer.parseInt(clientTimeStamp[0]);
    }


}
