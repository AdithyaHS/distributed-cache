package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SequentialConsistency extends ConsistencyImpl {

    private static final Logger logger = LoggerFactory.getLogger(SequentialConsistency.class);
    @Override
    public Controller.WriteResponse write(ConsistencyRequest request) {
        logger.info("Sequential request received for request "+ request.getLamportClock());
        return super.broadcastWrite(request);
    }
}
