package com.distributedsystems.distributedcache.controller;

import io.grpc.stub.StreamObserver;

public class ControllerHandler extends ControllerServiceGrpc.ControllerServiceImplBase{
    @Override
    public void get(Controller.ReadRequest request, StreamObserver<Controller.ReadRequest> responseObserver) {
        //TODO: implement
        super.get(request, responseObserver);
    }

    @Override
    public void put(Controller.WriteRequest request, StreamObserver<Controller.WriteResponse> responseObserver) {
        //TODO: implement
        super.put(request, responseObserver);
    }
}
