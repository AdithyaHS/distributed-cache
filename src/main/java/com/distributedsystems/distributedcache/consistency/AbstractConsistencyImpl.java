package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;

public abstract class AbstractConsistencyImpl implements ConsistencyImpl{

    @Override
    public Controller.ReadResponse read(Controller.ReadRequest request){
        // TODO: local read, call to readFromRedis goes here
        return null;
    }

    @Override
    public Controller.WriteResponse write(Controller.WriteRequest request) {
        // TODO: broadcast code goes here
        return null;
    }
}
