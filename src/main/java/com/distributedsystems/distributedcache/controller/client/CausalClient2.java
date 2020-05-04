package com.distributedsystems.distributedcache.controller.client;

import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.controller.ControllerHandler;
import com.distributedsystems.distributedcache.controller.ControllerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.lang.Thread.sleep;


public class CausalClient2 {
    private static final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    static boolean flag = false;

    public static ControllerServiceGrpc.ControllerServiceBlockingStub getControllerBlockingClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceBlockingStub blockingStub = ControllerServiceGrpc.newBlockingStub(channel);
        return blockingStub;
    }
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7004).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceBlockingStub stub= getControllerBlockingClient("localhost", 7004);
        System.out.println("Causal Consistency Test");
        Controller.WriteResponse causalWrite = stub.put(Controller.WriteRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setKey("x").setValue("3").setTimeStamp("1.2").build());
        System.out.println("Write status: " + causalWrite.getSuccess()+ "  timestamp: "+causalWrite.getTimeStamp());
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Testing for Causal local read
        Controller.ReadResponse causalRead = stub.get(Controller.ReadRequest.newBuilder().setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setKey("x").setTimeStamp("3.2").build());
        System.out.println("Reading value of x:" + causalRead.getValue());



       /*
        // Async client
        ControllerServiceGrpc.ControllerServiceStub asyncStub = ControllerServiceGrpc.newStub(channel);
        logger.info("Client 2 read request");
        StreamObserver<Controller.ReadResponse> controllerResponse2 = new StreamObserver<Controller.ReadResponse>() {
            @Override
            public void onNext(Controller.ReadResponse readResponse) {
                System.out.println("This is async response2 " + readResponse.getValue());
                logger.info("Reading value of 'x': "+readResponse.getValue());
                setFlag(true);
            }
            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error" + throwable.getMessage());
            }
            @Override
            public void onCompleted() {
                System.out.println("Done");
            }
        };
        asyncStub.get(Controller.ReadRequest.newBuilder().setKey("x").setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setTimeStamp("3.2").build(),controllerResponse2);
        while (!flag){
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        */

    }

    private static void setFlag(boolean b) {
        flag = b;
    }

}
