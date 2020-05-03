package com.distributedsystems.distributedcache;

import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderBroadcastHandler;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.distributedsystems.distributedcache")
public class DistributedcacheApplication {

    private static final Logger logger = LoggerFactory.getLogger(DistributedcacheApplication.class);
    private static final int port = 1993; // To be changed to read from the config file

    public static void main(String[] args) throws IOException, InterruptedException {

        /*
        * This is required to start controller grpc server
        */
        SpringApplication.run(DistributedcacheApplication.class, args);

        /*
         * Create a total order broadcast server from the main. Runs on port 1993.
         */
        final Server server = ServerBuilder.forPort(port).addService(new TotalOrderBroadcastHandler()).build();

        server.start();

        logger.info("Server started at port: " + server.getPort());

        server.awaitTermination();


    }
}