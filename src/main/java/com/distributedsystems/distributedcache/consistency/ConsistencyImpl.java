package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.AppConfig;
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

    private static final Logger logger = LoggerFactory.getLogger(ConsistencyImpl.class);

    /*
    * Implements local read
     */
    @Override
    public Controller.ReadResponse read(ConsistencyRequest request){
        String value = Utils.readFromRedis(request.getKey());
        return Controller.ReadResponse.newBuilder().setValue(value).setSuccess(true).build();
    }

    /*
     * Implements broadcast write
     */
    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        TotalOrderedBroadcast.BroadcastMessage.Builder builder = TotalOrderedBroadcast.BroadcastMessage.newBuilder();
        builder.setKey(request.getKey());
        builder.setValue(request.getValue());
        builder.setTypeOfRequest(TotalOrderedBroadcast.RequestType.PUT);
        builder.setLamportClock(request.getLamportClock());
        try {
            TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub client = getTOBClient();
            //TotalOrderedBroadcast.Empty response = client.sendBroadcastMessage(builder.build());
        }catch (StatusRuntimeException e) {
            logger.info(e.getMessage());
            return Controller.WriteResponse.newBuilder().setSuccess(false).build();
        }

        BroadcastStatus status = new BroadcastStatus();
        request.getPendingRequests().put(request.getLamportClock(), status);
        checkStatus(status);
        return Controller.WriteResponse.newBuilder().setSuccess(true).build();
    }

    protected TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub getTOBClient(){

        String tobHost = appConfig.tobHost;
        int tobPort = appConfig.tobPort;
        ManagedChannel channel = ManagedChannelBuilder.forAddress(tobHost, tobPort).usePlaintext().build();
        return TotalOrderBroadcastServiceGrpc.newBlockingStub(channel);
    }

    protected void checkStatus(BroadcastStatus status){
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
