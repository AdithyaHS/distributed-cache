package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;
/*
In this implementation of eventual consistency all reads are local and for write we do a total order broadcast and
wait for acknowledgement from half of the controller replicas.
 */
public class EventualConsistency extends AbstractConsistencyImpl {

    @Override
    public Controller.WriteResponse write(Controller.WriteRequest request) {
        //TODO: call the total order broadcast and once half of the services send an acknowledgment return
        return null;
    }
}
