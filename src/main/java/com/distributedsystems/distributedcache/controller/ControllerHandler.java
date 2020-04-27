package com.distributedsystems.distributedcache.controller;

import com.distributedsystems.distributedcache.consistency.ConsistencyImpl;
import com.distributedsystems.distributedcache.consistency.ConsistencyResolver;
import com.google.common.base.Optional;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class ControllerHandler extends ControllerServiceGrpc.ControllerServiceImplBase{

    @Override
    public void get(Controller.ReadRequest request, StreamObserver<Controller.ReadResponse> responseObserver) {
        Optional<ConsistencyImpl> consistencyImpl = ConsistencyResolver.resolveConsistency(request.getConsistencyLevel());
        if(consistencyImpl.isPresent()) {
            Controller.ReadResponse response = consistencyImpl.get().read(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }else{
            responseObserver.onError(new Exception("Unimplemented consistency option"));
        }
    }

    @Override
    public void put(Controller.WriteRequest request, StreamObserver<Controller.WriteResponse> responseObserver) {
        //TODO: implement
        super.put(request, responseObserver);
    }

    @Override
    public void broadcastRequestHandler(Controller.BroadcastRequest request, StreamObserver<Controller.BroadcastResponse> responseObserver) {
        //TODO: implement
        super.broadcastRequestHandler(request, responseObserver);
    }
}
