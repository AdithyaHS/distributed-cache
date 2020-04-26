package com.distributedsystems.distributedcache;

import com.distributedsystems.distributedcache.totalorderedbroadcast.ClientStubs;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastHandler;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastServiceGrpc;
import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;

@SpringBootApplication
public class DistributedcacheApplication {

    public static void main(String[] args) {

        final Server server = ServerBuilder.forPort(1993).addService(new TotalOrderBroadcastHandler()).build();
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Server started at port: " + server.getPort());

        ClientStubs clientStub = ClientStubs.getInstance();
        ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub> totalOrderBroadcastServiceStubs
                = clientStub.getStubs();
        int count = 0;

        TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub = clientStub.getCurrentTOBStub();
        TotalOrderedBroadcast.BroadcastMessage message = TotalOrderedBroadcast.BroadcastMessage
                .newBuilder()
                .setKey("x")
                .setValue("0")
                .setLamportClock(String.valueOf(count))
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