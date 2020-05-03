package com.distributedsystems.distributedcache.controller.client;

import com.distributedsystems.distributedcache.totalorderedbroadcast.ClientStubs;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TotalOrderedBroadcastClient {

    private static final Logger logger = LoggerFactory.getLogger(TotalOrderedBroadcastClient.class);

    public static void main(String[] args) throws InterruptedException {
        /*
         * This is to get the current Total order client stub that will be associated with this controller
         */
        ClientStubs clientStub = ClientStubs.getInstance();
        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub = clientStub.getCurrentTOBStub();

        /*
         * Example for sending one total order broadcast message.
         * Lamport clock should be of the form incremented_clock.process_id if process_id is 1,
         * then it goes 1.1, 2.1, 3.1, 4.1 etc
         */
        TotalOrderedBroadcast.BroadcastMessage message = TotalOrderedBroadcast.BroadcastMessage
                .newBuilder()
                .setKey("x")
                .setValue("0")
                .setLamportClock(String.valueOf("1.1"))
                .build();

        StreamObserver<TotalOrderedBroadcast.Empty> response = new StreamObserver<TotalOrderedBroadcast.Empty>() {

            @Override
            public void onNext(TotalOrderedBroadcast.Empty empty) {
                System.out.println("OnNext");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getCause() + " " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("OnCompleted");
            }
        };

        stub.withWaitForReady().sendBroadcastMessage(message, response);

        logger.info("Calling blocking stub");
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 1993)
                .usePlaintext()
                .build();

        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceBlockingStub totalOrderBroadcastServiceStub =
                TotalOrderBroadcastServiceGrpc.newBlockingStub(channel);
        totalOrderBroadcastServiceStub.withWaitForReady().sendBroadcastMessage(message);
        logger.info("after blocking");
    }
}
