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

    public static void main(String[] args) throws InterruptedException {
        /*
        * It is upto client of controller to decide whether use a blocking stub or a non blocking stub. Samples for both are provided.
         */
        ControllerServiceGrpc.ControllerServiceBlockingStub stub= getControllerBlockingClient("localhost", 7004);



        //Testing eventual consistency local write and local read
        System.out.println("Testing for eventual consistency");
        Controller.WriteResponse response = stub.put(Controller.WriteRequest.newBuilder().setKey("a").setValue("1").setConsistencyLevel(Controller.ConsistencyLevel.EVENTUAL).build());
        System.out.println(response.getSuccess());
        Controller.ReadResponse readResponse = stub.get(Controller.ReadRequest.newBuilder().setKey("a").setConsistencyLevel(Controller.ConsistencyLevel.EVENTUAL).build());
        System.out.println(readResponse.getValue());

        //Testing for sequential consistency for broadcast write
        System.out.println("Testing for sequential consistency");
        Controller.WriteResponse sequentialWrite = stub.put(Controller.WriteRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.SEQUENTIAL).setKey("a").setValue("2").build());
        System.out.println(sequentialWrite.getSuccess());
        //Testing for sequential consistency local read request
        Controller.ReadResponse sequentialRead = stub.get(Controller.ReadRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.SEQUENTIAL).setKey("a").build());
        System.out.println(sequentialRead.getValue());


        //Testing for linearizability broadcast write
        System.out.println("Testing for linearizability consistency");
        Controller.WriteResponse linearizabilityWrite = stub.put(Controller.WriteRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.LINEARIZABILITY).setKey("a").setValue("3").build());
        System.out.println(linearizabilityWrite.getSuccess());

        Controller.ReadResponse linearizabilityRead = stub.get(Controller.ReadRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.LINEARIZABILITY).setKey("a").build());
        System.out.println(linearizabilityRead.getValue());

        //Testing for Causal broadcast write

        System.out.println("Causal Consistency Test");
        Controller.WriteResponse causalWrite = stub.put(Controller.WriteRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setKey("x").setValue("3").setTimeStamp("1.1").build());
        System.out.println("Write status: " + causalWrite.getSuccess());
        //Testing for Causal local read
        Controller.ReadResponse causalRead = stub.get(Controller.ReadRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setKey("x").setTimeStamp("1.1").build());
        System.out.println("Reading value of x:" + causalRead.getValue());


//

        // example async stub. Client should use this only when it wants to do some work in the background
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7004).usePlaintext().build();
//        ControllerServiceGrpc.ControllerServiceStub asynStub = ControllerServiceGrpc.newStub(channel);
//        StreamObserver<Controller.ReadResponse> controllerResponse = new StreamObserver<Controller.ReadResponse>() {
//            @Override
//            public void onNext(Controller.ReadResponse readResponse) {
//                System.out.println("This is asyn resposne " + readResponse.getValue());
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                System.out.println("Error" + throwable.getMessage());
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("Done");
//            }
//        };
//        asynStub.get(Controller.ReadRequest.newBuilder().setKey("a").setConsistencyLevel(Controller.ConsistencyLevel.EVENTUAL).build(), controllerResponse);
//        //need to sleep to see the results before the program exists
//        sleep(1000);
    }
}
