package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;

public class LinearizabilityConsistency extends AbstractConsistencyImpl{

    @Override
    public Controller.ReadResponse read(Controller.ReadRequest request) {
        //TODO: call broadcast read
        return null;
    }
}
