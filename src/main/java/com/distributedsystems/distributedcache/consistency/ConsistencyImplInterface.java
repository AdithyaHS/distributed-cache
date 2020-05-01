package com.distributedsystems.distributedcache.consistency;

import com.distributedsystems.distributedcache.controller.Controller;

public interface ConsistencyImplInterface {

    Controller.ReadResponse read(ConsistencyRequest request);
    Controller.WriteResponse write(ConsistencyRequest request);
}
