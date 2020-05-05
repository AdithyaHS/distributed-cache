package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import org.springframework.stereotype.Component;

/*
In this implementation of eventual consistency all reads and writes are local
 */
@Component
public class EventualConsistency extends ConsistencyImpl {
    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        ConsistencyRequest r = new ConsistencyRequest();
        r.setLamportClock(request.getLamportClock());
        r.setValue(request.getValue());
        r.setKey(request.getKey());
        r.setPendingRequests(request.getPendingRequests());
        new Thread(()->{super.broadcastWrite(r);}).start();
        return super.write(request);
    }
}
