package com.distributedsystems.distributedcache.totalorderedbroadcast;

import com.distributedsystems.distributedcache.configuration.Configuration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientStubs {

    private static ClientStubs ourInstance = new ClientStubs();

    private ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub> totalOrderBroadcastServiceStubs =
            new ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub>();

    private TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub currentClientTOBstub;

    private ClientStubs() {
        HashMap<String, String> config = Configuration.getInstance().readConfig();
        System.out.println(config);
        for (Map.Entry<String, String> entry : config.entrySet()) {
            String[] address = entry.getValue().split(":");
            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(address[0].trim(), Integer.valueOf(address[1]))
                    .usePlaintext()
                    .build();
            TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub = TotalOrderBroadcastServiceGrpc
                    .newStub(channel);

            //get the current ip address here compare if its the actual client
            if (address[0].equals(" 10.0.0.202") && address[1].equals("1993")) {
                currentClientTOBstub = stub;
            }
            totalOrderBroadcastServiceStubs.add(stub);
        }
    }

    public static ClientStubs getInstance() {
        return ourInstance;
    }

    public ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub> getStubs() {
        return totalOrderBroadcastServiceStubs;
    }

    public TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub getCurrentTOBStub() {
        return currentClientTOBstub;
    }
}
