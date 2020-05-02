package com.distributedsystems.distributedcache.controller;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.consistency.BroadcastStatus;
import com.distributedsystems.distributedcache.consistency.ConsistencyImplInterface;
import com.distributedsystems.distributedcache.consistency.ConsistencyRequest;
import com.distributedsystems.distributedcache.consistency.ConsistencyResolver;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Optional;

@GRpcService
public class ControllerHandler extends ControllerServiceGrpc.ControllerServiceImplBase{

    @Autowired
    ControllerConfigurations appConfig;

    @Autowired
    ConsistencyResolver consistencyResolver;

    private int requestId;
    private HashMap<String, BroadcastStatus> pendingRequests = new HashMap<>();

    @Override
    public void get(Controller.ReadRequest request, StreamObserver<Controller.ReadResponse> responseObserver) {

        Optional<ConsistencyImplInterface> consistencyImpl = consistencyResolver.resolveConsistency(request.getConsistencyLevel());
        if(consistencyImpl.isPresent()) {
            ConsistencyRequest consistencyRequest = new ConsistencyRequest();
            consistencyRequest.setKey(request.getKey());
            consistencyRequest.setLamportClock(getLamportClock());
            consistencyRequest.setPendingRequests(pendingRequests);
            Controller.ReadResponse response = consistencyImpl.get().read(consistencyRequest);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
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
            consistencyRequest.setLamportClock(getLamportClock());
            consistencyRequest.setPendingRequests(pendingRequests);
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
}
