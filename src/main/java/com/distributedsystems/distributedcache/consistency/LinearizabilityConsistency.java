package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LinearizabilityConsistency extends ConsistencyImpl {

    private static final Logger logger = LoggerFactory.getLogger(LinearizabilityConsistency.class);

    @Override
    public Controller.ReadResponse read(ConsistencyRequest request) {

        TotalOrderedBroadcast.BroadcastMessage.Builder builder = TotalOrderedBroadcast.BroadcastMessage.newBuilder();
        builder.setKey(request.getKey());
        builder.setTypeOfRequest(TotalOrderedBroadcast.RequestType.GET);
        builder.setLamportClock(request.getLamportClock());
        try {
            TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub client = super.getTOBClient();
            //TotalOrderedBroadcast.Empty response = client.sendBroadcastMessage(builder.build());
        }catch (StatusRuntimeException e) {
            logger.info(e.getMessage());
            return Controller.ReadResponse.newBuilder().setSuccess(false).build();
        }
        BroadcastStatus status = new BroadcastStatus();
        request.getPendingRequests().put(request.getLamportClock(), status);
        super.checkStatus(status);
        return Controller.ReadResponse.newBuilder().setValue(status.getValue()).build();
    }
}
