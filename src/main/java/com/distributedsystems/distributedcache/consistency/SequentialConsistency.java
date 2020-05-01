package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import org.springframework.stereotype.Component;

@Component
public class SequentialConsistency extends ConsistencyImpl {

    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        return super.broadcastWrite(request);
    }
}
