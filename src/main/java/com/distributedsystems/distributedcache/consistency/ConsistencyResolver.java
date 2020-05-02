package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConsistencyResolver {

    @Autowired
    SequentialConsistency sequentialConsistency;
    @Autowired
    EventualConsistency eventualConsistency;
    @Autowired
    LinearizabilityConsistency linearizabilityConsistency;
    @Autowired
    LinearizabilityConsistency causalConsistency;

    public Optional<ConsistencyImplInterface> resolveConsistency(Controller.ConsistencyLevel level){
        if(Controller.ConsistencyLevel.SEQUENTIAL.equals(level)){
            return Optional.of(sequentialConsistency);
        }
        else if(Controller.ConsistencyLevel.EVENTUAL.equals(level)){
            return Optional.of(eventualConsistency);
        }
        else if(Controller.ConsistencyLevel.LINEARIZABILITY.equals(level)){
            return Optional.of(linearizabilityConsistency);
        }
        else if(Controller.ConsistencyLevel.CAUSAL.equals(level)) {
            return Optional.of(causalConsistency);
        }
        else{
            return Optional.empty();
        }
    }
}
