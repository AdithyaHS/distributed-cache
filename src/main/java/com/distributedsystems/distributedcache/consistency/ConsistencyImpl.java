package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.Utilities.Utils;
import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
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

    /*
    * This method calls the broadcast server can blocks until the
     */
    protected Controller.WriteResponse broadcastWrite(ConsistencyRequest request) {
        TotalOrderedBroadcast.BroadcastMessage.Builder builder = TotalOrderedBroadcast.BroadcastMessage.newBuilder();
        builder.setKey(request.getKey());
        builder.setValue(request.getValue());
        builder.setTypeOfRequest(TotalOrderedBroadcast.RequestType.PUT);
        builder.setLamportClock(request.getLamportClock());
        if(request.getClientTimeStamp().length() > 1)
            builder.setClientTimeStamp(request.getClientTimeStamp());

        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub client = getTOBClient();
        try{
            logger.info("Sending a broadcast request to the tob server for request " + request.getLamportClock());
            BroadcastStatus status = new BroadcastStatus();
            request.getPendingRequests().put(request.getLamportClock(), status);
            TotalOrderedBroadcast.Empty broadcastResponse = client.sendBroadcastMessage(builder.build());
            logger.info("Waiting for the broadcast request to complete request: " + request.getLamportClock());
            waitUntilBroadcastIsCompleted(status);
            logger.info("Broadcast request completed replying to the client for request:  " + request.getLamportClock());
            return Controller.WriteResponse.newBuilder().setSuccess(true).setTimeStamp(builder.getClientTimeStamp()).build();
        } catch (StatusRuntimeException e) {
            logger.error(e.getMessage());
            return Controller.WriteResponse.newBuilder().setSuccess(false).build();
        }
    }

    protected TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub getTOBClient(){

        String tobHost = appConfig.tobHost;
        int tobPort = appConfig.grpcPort;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(tobHost, tobPort).usePlaintext().build();
        return TotalOrderBroadcastServiceGrpc.newBlockingStub(channel);
    }

    /*
    * This will block until tob server calls broadcastRequestAcknowledgement is called and status is set to completed
     */
    protected void waitUntilBroadcastIsCompleted(BroadcastStatus status){
        synchronized (status){
            while(!status.isCompleted()){
                try {
                    status.wait(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
