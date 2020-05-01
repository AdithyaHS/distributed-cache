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
        //Testing eventual consistency local write and local read
        ControllerServiceGrpc.ControllerServiceBlockingStub stub= getControllerBlockingClient("localhost", 7004);
        Controller.WriteResponse response = stub.put(Controller.WriteRequest.newBuilder().setKey("a").setValue("1").setConsistencyLevel(Controller.ConsistencyLevel.EVENTUAL).build());
        System.out.println(response.getSuccess());
        Controller.ReadResponse readResponse = stub.get(Controller.ReadRequest.newBuilder().setKey("a").setConsistencyLevel(Controller.ConsistencyLevel.EVENTUAL).build());
        System.out.println(readResponse.getValue());
        //stub.put(Controller.WriteRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.SEQUENTIAL).build());
        //stub.broadcastRequestAcknowledgement(Controller.Ack.newBuilder().setLamportClock("1.1").build());
    }
}
