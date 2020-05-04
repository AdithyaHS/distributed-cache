package com.distributedsystems.distributedcache.controller;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.Utilities.Utils;
import com.distributedsystems.distributedcache.consistency.BroadcastStatus;
import com.distributedsystems.distributedcache.consistency.ConsistencyImplInterface;
import com.distributedsystems.distributedcache.consistency.ConsistencyRequest;
import com.distributedsystems.distributedcache.consistency.ConsistencyResolver;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.PriorityBlockingQueue;


@GRpcService
public class ControllerHandler extends ControllerServiceGrpc.ControllerServiceImplBase{

    private static final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);

    @Autowired
    ControllerConfigurations appConfig;

    @Autowired
    ConsistencyResolver consistencyResolver;

    @Autowired
    Utils utils;

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
            if(!request.getConsistencyLevel().equals(Controller.ConsistencyLevel.CAUSAL)) {
                consistencyRequest.setLamportClock(getLamportClock());
                consistencyRequest.setClientTimeStamp(request.getTimeStamp());
                Controller.ReadResponse response = consistencyImpl.get().read(consistencyRequest);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
            else {
                consistencyRequest.setLamportClock(String.valueOf(this.requestId));
                logger.info("time stamp: "+request.getTimeStamp());
                if (getClientTimeStamp(request.getTimeStamp()) > this.requestId) {
                    logger.info("In if "+this.requestId);
                    pendingGetRequestsQueue.offer(request);
                    pendingGetRequestsMap.put(request, responseObserver);
                }
                else{
                    logger.info("In else "+this.requestId);
                    Controller.ReadResponse response = consistencyImpl.get().read(consistencyRequest);
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            }
        }else{
            responseObserver.onError(new Exception("Unimplemented consistency option"));
        }
        logger.info("Controller timestamp: "+this.requestId);
    }



        @Override
    public void put(Controller.WriteRequest request, StreamObserver<Controller.WriteResponse> responseObserver) {
        Optional<ConsistencyImplInterface> consistencyImpl = consistencyResolver.resolveConsistency(request.getConsistencyLevel());
        if(consistencyImpl.isPresent()) {
            ConsistencyRequest consistencyRequest = new ConsistencyRequest();
            consistencyRequest.setKey(request.getKey());
            consistencyRequest.setValue(request.getValue());
            consistencyRequest.setPendingRequests(pendingRequests);
            if(!request.getConsistencyLevel().equals(Controller.ConsistencyLevel.CAUSAL)){
                consistencyRequest.setClientTimeStamp("");
                consistencyRequest.setLamportClock(getLamportClock());
            }
            else{
                consistencyRequest.setLamportClock(getLamportClock(request));
                String clientsUpdatedTimeStamp = this.requestId+"."+getClientId(request.getTimeStamp());
                consistencyRequest.setClientTimeStamp(clientsUpdatedTimeStamp);


            }
            Controller.WriteResponse response = consistencyImpl.get().write(consistencyRequest);
            System.out.println(response.getTimeStamp());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            processPendingGetRequests();

        }else{
            responseObserver.onError(new Exception("Unimplemented consistency option"));
        }
    }

    public void unblockController(ConsistencyRequest request) {
        logger.info("Got a broadcast ack for request: " + request.getLamportClock());
        if(pendingRequests.containsKey(request.getLamportClock())) {
            logger.info("Unblocking client for request: " + request.getLamportClock());
            BroadcastStatus status = pendingRequests.get(request.getLamportClock());
            status.setValue(request.getValue());
            status.setCompletedAndNotify(true);
        }
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
        }
        return appConfig.controllerId+"."+requestId;
    }

    private void processPendingGetRequests() {
        while(!pendingGetRequestsQueue.isEmpty() && getClientTimeStamp(pendingGetRequestsQueue.peek().getTimeStamp()) <= this.requestId){
                synchronized (this){
                    Controller.ReadRequest request= pendingGetRequestsQueue.poll();
                    StreamObserver<Controller.ReadResponse> responseObserver = pendingGetRequestsMap.get(request);
                    pendingGetRequestsMap.remove(request);
                    this.get(request,responseObserver);
                }
        }
    }

    private int getClientTimeStamp(String lamportTimeStamp){
        String[] clientTimeStamp = lamportTimeStamp.split("\\.");
        return Integer.parseInt(clientTimeStamp[0]);
    }

    @Override
    public void handleMessageRequest(TotalOrderedBroadcast.BroadcastMessage request, StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {
        logger.info("Received ack for broadcast request: " + request.getLamportClock());
        ConsistencyRequest response = new ConsistencyRequest();
        response.setLamportClock(request.getLamportClock());
        if(request.getTypeOfRequest().equals(TotalOrderedBroadcast.RequestType.GET)){
           String value = utils.readFromRedis(request.getKey());
            response.setValue(value);
        }else if(request.getTypeOfRequest().equals(TotalOrderedBroadcast.RequestType.PUT)){
            utils.writeToRedis(request.getKey(), request.getValue());
            logger.info("In handle request. The client timestamp is: "+request.getClientTimeStamp());
//            if(request.getClientTimeStamp().length() > 1){
//               String clientsUpdatedTimeStamp = this.requestId+"."+getClientId(request.getClientTimeStamp());
//               response.setClientTimeStamp(clientsUpdatedTimeStamp);
//            }

        }
        unblockController(response);
        responseObserver.onNext(TotalOrderedBroadcast.Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    private String getClientId(String clientTimeStamp) {
        return clientTimeStamp.split("\\.")[1];
    }


}
