package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CausalConsistency extends ConsistencyImpl {
    private static final Logger logger = LoggerFactory.getLogger(LinearizabilityConsistency.class);

    @Override
    public Controller.ReadResponse read(ConsistencyRequest request) {
        return super.read(request);
    }

    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        return super.broadcastWrite(request);
    }

}
