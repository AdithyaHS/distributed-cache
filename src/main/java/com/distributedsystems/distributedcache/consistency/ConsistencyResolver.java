package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import com.google.common.base.Optional;

public class ConsistencyResolver {

    public static Optional<ConsistencyImpl> resolveConsistency(Controller.ConsistencyLevel level){
        if(Controller.ConsistencyLevel.SEQUENTIAL.equals(level)){
            return Optional.of((ConsistencyImpl) new SequentialConsistency());
        }
        else if(Controller.ConsistencyLevel.EVENTUAL.equals(level)){
            return Optional.of((ConsistencyImpl) new EventualConsistency());
        }
        else if(Controller.ConsistencyLevel.LINEARIZABILITY.equals(level)){
            return Optional.of((ConsistencyImpl) new LinearizabilityConsistency());
        }
        else{
            return Optional.absent();
        }
    }
}
