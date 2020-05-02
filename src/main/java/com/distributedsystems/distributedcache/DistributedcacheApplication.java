package com.distributedsystems.distributedcache;

import com.distributedsystems.distributedcache.totalorderedbroadcast.ClientStubs;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastHandler;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.distributedsystems.distributedcache")
public class DistributedcacheApplication {

    private static final Logger logger = LoggerFactory.getLogger(DistributedcacheApplication.class);
    private static final int port = 1993; // To be changed to read from the config file

    public static void main(String[] args) {

        /*
        * This is required to start controller grpc server
        */
        SpringApplication.run(DistributedcacheApplication.class, args);

        /*
         * Create a total order broadcast server from the main. Runs on port 1993.
         */
        final Server server = ServerBuilder.forPort(port).addService(new TotalOrderBroadcastHandler()).build();

        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Server started at port: " + server.getPort());


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

        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}