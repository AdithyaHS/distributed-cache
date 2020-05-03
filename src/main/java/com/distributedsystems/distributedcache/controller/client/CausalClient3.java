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


public class CausalClient3 {
    private static final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    static boolean flag = false;

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7004).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceStub stub = ControllerServiceGrpc.newStub(channel);
        logger.info("Client 3 Request");
        StreamObserver<Controller.WriteResponse> writeResponse2 = new StreamObserver<Controller.WriteResponse>() {
            @Override
            public void onNext(Controller.WriteResponse writeResponse) {
                logger.info("This is async write2 response " + writeResponse.getSuccess());
                setFlag(true);
            }
            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error" + throwable.getMessage());
            }
            @Override
            public void onCompleted() {
                logger.info(" Write2 Done");
            }
        };
        stub.put(Controller.WriteRequest.newBuilder().setKey("x").setValue("6").setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setTimeStamp("1.3").build(), writeResponse2);
        while (!flag) {
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
