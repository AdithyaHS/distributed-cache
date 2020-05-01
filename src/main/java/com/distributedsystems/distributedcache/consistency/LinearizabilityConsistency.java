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

    @Override
    public Controller.ReadResponse read(ConsistencyRequest request) {

        TotalOrderedBroadcast.BroadcastMessage.Builder builder = TotalOrderedBroadcast.BroadcastMessage.newBuilder();
        builder.setKey(request.getKey());
        builder.setTypeOfRequest(TotalOrderedBroadcast.RequestType.GET);
        builder.setLamportClock(request.getLamportClock());

        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub client = super.getTOBClient();
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
                checkStatus(status);
                //return Controller.ReadResponse.newBuilder().setValue(status.getValue()).setSuccess(true).build();
            }
        };

        client.withWaitForReady().sendBroadcastMessage(builder.build(), response);
        return null;
    }

    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        return super.broadcastWrite(request);
    }
}
