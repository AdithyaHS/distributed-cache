package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.Utilities.Utils;
import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.totalorderedbroadcast.ClientStubs;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConsistencyImpl implements ConsistencyImplInterface {

    @Autowired
    private ControllerConfigurations appConfig;

    @Autowired
    private Utils utils;

    private static final Logger logger = LoggerFactory.getLogger(ConsistencyImpl.class);

    /*
    * Implements local read
     */
    @Override
    public Controller.ReadResponse read(ConsistencyRequest request){
        String value = utils.readFromRedis(request.getKey());
        if(value==null){
            return Controller.ReadResponse.newBuilder().setSuccess(false).build();
        }
        return Controller.ReadResponse.newBuilder().setValue(value).setSuccess(true).build();
    }

    /*
     * Implements local write
     */

    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        boolean status = utils.writeToRedis(request.getKey(), request.getValue());
        if(status){
            return Controller.WriteResponse.newBuilder().setSuccess(true).build();
        }
        return Controller.WriteResponse.newBuilder().setSuccess(false).build();
    }

    protected Controller.WriteResponse broadcastWrite(ConsistencyRequest request) {
        TotalOrderedBroadcast.BroadcastMessage.Builder builder = TotalOrderedBroadcast.BroadcastMessage.newBuilder();
        builder.setKey(request.getKey());
        builder.setValue(request.getValue());
        builder.setTypeOfRequest(TotalOrderedBroadcast.RequestType.PUT);
        builder.setLamportClock(request.getLamportClock());

        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub client = getTOBClient();
        StreamObserver<TotalOrderedBroadcast.Empty> response = new StreamObserver<TotalOrderedBroadcast.Empty>() {

                @Override
                public void onNext(TotalOrderedBroadcast.Empty empty) {
                    //do nothing
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println(throwable.getCause() + " " + throwable.getMessage());
                    //TODO: fix this return error to the user
                }

                @Override
                public void onCompleted() {
                    //TODO: fix this return success to user
                    BroadcastStatus status = new BroadcastStatus();
                    request.getPendingRequests().put(request.getLamportClock(), status);
                    waitUntilBroadcastIsCompleted(status);
                    //return Controller.WriteResponse.newBuilder().setSuccess(true).build();
                }
            };

            client.withWaitForReady().sendBroadcastMessage(builder.build(), response);
            return null;
    }

    protected TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub getTOBClient(){

//        String tobHost = appConfig.tobHost;
//        int tobPort = appConfig.tobPort;
//        ManagedChannel channel = ManagedChannelBuilder.forAddress(tobHost, tobPort).usePlaintext().build();
//        return TotalOrderBroadcastServiceGrpc.newBlockingStub(channel);
        /*
         * This is to get the current Total order client stub that will be associated with this controller
         */
        ClientStubs clientStub = ClientStubs.getInstance();
        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub = clientStub.getCurrentTOBStub();
        return stub;
    }

    /*
    * This will block until tob server calls broadcastRequestAcknowledgement is called and status is set to completed
     */
    protected void waitUntilBroadcastIsCompleted(BroadcastStatus status){
        synchronized (status){
            while(!status.isCompleted()){
                try {
                    status.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
