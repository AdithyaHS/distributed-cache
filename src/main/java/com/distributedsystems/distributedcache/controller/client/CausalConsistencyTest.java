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


public class CausalConsistencyTest {
    private static final Logger logger = LoggerFactory.getLogger(ControllerHandler.class);
    static boolean flag = false;

    public static ControllerServiceGrpc.ControllerServiceBlockingStub getControllerBlockingClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceBlockingStub blockingStub = ControllerServiceGrpc.newBlockingStub(channel);
        return blockingStub;
    }

    public static void main(String[] args) {
        //ControllerServiceGrpc.ControllerServiceBlockingStub stub= getControllerBlockingClient("localhost", 7004);
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 7004).usePlaintext().build();
        ControllerServiceGrpc.ControllerServiceStub stub = ControllerServiceGrpc.newStub(channel);
        StreamObserver<Controller.WriteResponse> writeResponse = new StreamObserver<Controller.WriteResponse>() {
            @Override
            public void onNext(Controller.WriteResponse writeResponse) {
                logger.info("This is async write response " + writeResponse.getSuccess());
                setFlag(true);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error" + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info(" Write Done");
            }
        };
        stub.put(Controller.WriteRequest.newBuilder().setKey("a").setValue("1").setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setTimeStamp("1.1").build(), writeResponse);
        logger.info("Waiting");
        while (!flag){
            try {

                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
        setFlag(false);

        StreamObserver<Controller.ReadResponse> controllerResponse = new StreamObserver<Controller.ReadResponse>() {
            @Override
            public void onNext(Controller.ReadResponse readResponse) {
                System.out.println("This is async resposne " + readResponse.getValue());
                logger.info("Reading value of 'a': "+readResponse.getValue());
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
        stub.get(Controller.ReadRequest.newBuilder().setKey("a").setConsistencyLevel(Controller.ConsistencyLevel.CAUSAL).setTimeStamp("1.1").build(),controllerResponse);

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
