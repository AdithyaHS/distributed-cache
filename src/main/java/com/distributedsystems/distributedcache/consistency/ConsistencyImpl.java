package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;

public interface ConsistencyImpl {

    public Controller.ReadResponse read(Controller.ReadRequest request);
    public Controller.WriteResponse write(Controller.WriteRequest request);
}
