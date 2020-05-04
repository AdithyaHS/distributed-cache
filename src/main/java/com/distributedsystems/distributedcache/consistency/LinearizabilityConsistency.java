package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/*
* Both reads and writes are broadcasted
*/

@Component
public class LinearizabilityConsistency extends ConsistencyImpl {

    private static final Logger logger = LoggerFactory.getLogger(LinearizabilityConsistency.class);
    /*
    * broadcast read request
     */
    @Override
    public Controller.ReadResponse read(ConsistencyRequest request) {

        TotalOrderedBroadcast.BroadcastMessage.Builder builder = TotalOrderedBroadcast.BroadcastMessage.newBuilder();
        builder.setKey(request.getKey());
        builder.setTypeOfRequest(TotalOrderedBroadcast.RequestType.GET);
        builder.setLamportClock(request.getLamportClock());

        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub client = super.getTOBClient();
        BroadcastStatus status = new BroadcastStatus();
        request.getPendingRequests().put(request.getLamportClock(), status);
        try{
            TotalOrderedBroadcast.Empty broadcastResponse = client.withWaitForReady().sendBroadcastMessage(builder.build());
        }catch (StatusRuntimeException e){
            logger.error(e.getMessage());
            return Controller.ReadResponse.newBuilder().setSuccess(false).build();
        }
        waitUntilBroadcastIsCompleted(status);
        return Controller.ReadResponse.newBuilder().setValue(status.getValue()).setSuccess(true).build();
    }

    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        return super.broadcastWrite(request);
    }
}
