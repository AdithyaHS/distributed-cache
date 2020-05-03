package com.distributedsystems.distributedcache.totalorderedbroadcast;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.configuration.Configuration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClientStubs {

    private static final Logger logger = LoggerFactory.getLogger(ClientStubs.class);

    private ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub> totalOrderBroadcastServiceStubs =
            new ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub>();

    private TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub currentClientTOBstub;

    /**
     * @description creates clients stubs
     */
    @Autowired
    public ClientStubs(ControllerConfigurations appConfig) {
        HashMap<String, String> config = Configuration.getInstance().readConfig();

        ControllerConfigurations controllerConfigurations = new ControllerConfigurations();


        logger.debug(config.toString());

        String ipAddress = getIpAddress().trim();

        for (Map.Entry<String, String> entry : config.entrySet()) {

            String[] address = entry.getValue().split(":");

            ManagedChannel channel = ManagedChannelBuilder
                    .forAddress(address[0].trim(), Integer.valueOf(address[1]))
                    .usePlaintext()
                    .build();
            logger.info("Created a stub for host:" + address[0].trim() + "port:" + address[1]);
            TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub = TotalOrderBroadcastServiceGrpc
                    .newStub(channel);

            if (address[0].trim().equals(ipAddress) && address[1].equals(appConfig.grpcPort)) {
                currentClientTOBstub = stub;
            }
            totalOrderBroadcastServiceStubs.add(stub);
        }
    }

    /**
     * @return ipaddress of the host system.
     */
    private String getIpAddress() {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return inetAddress.getHostAddress();
    }

    public ArrayList<TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub> getStubs() {
        return totalOrderBroadcastServiceStubs;
    }

    public TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub getCurrentTOBStub() {
        return currentClientTOBstub;
    }
}
