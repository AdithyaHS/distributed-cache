package com.distributedsystems.distributedcache.controller.client;

import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.controller.ControllerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class SampleControllerClient {

    public static ControllerServiceGrpc.ControllerServiceBlockingStub getControllerBlockingClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceBlockingStub blockingStub = ControllerServiceGrpc.newBlockingStub(channel);
        return blockingStub;
    }

    public static void main(String[] args) {
        ControllerServiceGrpc.ControllerServiceBlockingStub stub= getControllerBlockingClient("localhost", 7004);
        //stub.get(Controller.ReadRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.EVENTUAL).build());
        stub.put(Controller.WriteRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.SEQUENTIAL).build());
        //stub.broadcastRequestAcknowledgement(Controller.Ack.newBuilder().setLamportClock("1.1").build());
    }
}
