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


    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7004).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceStub stub = ControllerServiceGrpc.newStub(channel);
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
        stub.get(Controller.ReadRequest.newBuilder().setKey("x").setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setTimeStamp("2.2").build(),controllerResponse2);
        while (!flag){
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setFlag(boolean b) {
        flag = b;
    }

}
