package com.distributedsystems.distributedcache.totalorderedbroadcast;

import com.distributedsystems.distributedcache.Utilities.ControllerConfigurations;
import com.distributedsystems.distributedcache.controller.Controller;
import com.distributedsystems.distributedcache.controller.ControllerServiceGrpc;
import com.distributedsystems.distributedcache.dto.TotalOrderedBroadcastMessage;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;

@GRpcService
public class TotalOrderBroadcastHandler extends TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(TotalOrderBroadcastHandler.class);

    @Autowired
    ControllerConfigurations appConfig;

    @Autowired
    ClientStubs clientStubs;

    private HashMap<String, TotalOrderedBroadcastMessage> lamportClockToMessageMap =
            new HashMap<String, TotalOrderedBroadcastMessage>();

    private Map<String, Integer> acknowledgementCountMap = new ConcurrentHashMap<String, Integer>();

    private PriorityBlockingQueue<TotalOrderedBroadcastMessage> queue = new PriorityBlockingQueue<TotalOrderedBroadcastMessage>
            (1000, new Comparator<TotalOrderedBroadcastMessage>() {

                @Override
                public int compare(TotalOrderedBroadcastMessage o1, TotalOrderedBroadcastMessage o2) {
                    if(Double.parseDouble(o1.getBroadcastMessage().getLamportClock()) >
                            Double.parseDouble(o2.getBroadcastMessage().getLamportClock()))
                        return 1;
                    else
                        return -1;
                }
            });

    /**
     * @param request          is the actual message the user wants to broadcast.
     * @param responseObserver is an empty observer. This is just the syntax to do async calls.
     * @description This method sends the broadcast method to all the TOB servers including itself by
     * calling receiveBroadcastMessage function.
     */
    @Override
    public void sendBroadcastMessage(final TotalOrderedBroadcast.BroadcastMessage request,
                                     final StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {

        final CountDownLatch countDownLatch = new CountDownLatch(appConfig.numOfServers);

        logger.info("How many number of tob stubs" + Integer.toString(clientStubs.getStubs().size()));
        logger.info(clientStubs.getStubs().toString());

        for (final TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub : clientStubs.getStubs()) {

            StreamObserver<TotalOrderedBroadcast.Empty> totalOrderBroadcastMessageObserver = new StreamObserver<TotalOrderedBroadcast.Empty>() {
                @Override
                public void onNext(TotalOrderedBroadcast.Empty empty) {
                    logger.info("In server onNext" + empty.getLamportClock());
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println(throwable.getMessage());
                    countDownLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    logger.info("In server onCompleted");
                    countDownLatch.countDown();
                }
            };
            logger.info("Tob server connecting to the tob server on port: "+ stub.getChannel().toString());
            stub.withWaitForReady().receiveBroadcastMessage(request, totalOrderBroadcastMessageObserver);
            logger.info("Sending messages executed for " + request.getLamportClock());
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        responseObserver.onNext(TotalOrderedBroadcast.Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    /**
     * @param request          is the actual message the user wants to broadcast.
     * @param responseObserver is an empty observer. This is just the syntax to do async calls.
     * @description This method is responsible for ordering the data and publishing Ack for the message on top
     * of the queue.
     */
    @Override
    public void receiveBroadcastMessage(final TotalOrderedBroadcast.BroadcastMessage request,
                                        final StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {

        final TotalOrderedBroadcastMessage totalOrderedBroadcastMessage =
                new TotalOrderedBroadcastMessage(request, false);

        lamportClockToMessageMap.put(request.getLamportClock(), totalOrderedBroadcastMessage);

        logger.info("Adding to queue message: " + totalOrderedBroadcastMessage.getBroadcastMessage().getLamportClock());
        if (!queue.offer(totalOrderedBroadcastMessage)) {
            responseObserver.onError(new Error("Not able to add to the queue"));
        }

        if (!queue.peek().isAcknowledgementPublished()) {
            sendAck();
        }
        responseObserver.onNext(TotalOrderedBroadcast.Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    /**
     * @description This method is responsible for publishing acknowledgements to all the TOB servers
     * We create a new AckMessage to send the Acknowledgement.
     */
    private void sendAck() {

        logger.info("acknowledgement counts before sending ack " + acknowledgementCountMap.toString());
        if (!queue.isEmpty() && !queue.peek().isAcknowledgementPublished()) {

            final CountDownLatch countDownLatch = new CountDownLatch(appConfig.numOfServers);

            TotalOrderedBroadcast.AckMessage ackMessage = TotalOrderedBroadcast.AckMessage.newBuilder()
                    .setBroadcastMessage(queue.peek().getBroadcastMessage())
                    .setIsAcknowledgementPublished(true)
                    .build();
            queue.peek().setAcknowledgementPublished(true);
            for (TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub : clientStubs.getStubs()) {

                StreamObserver<TotalOrderedBroadcast.Empty> emptyStreamObserver = new StreamObserver<TotalOrderedBroadcast.Empty>() {
                    @Override
                    public void onNext(TotalOrderedBroadcast.Empty empty) {
                        logger.info("In server send Ack onNext" + empty.getLamportClock());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.info(throwable.getMessage());
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("In server send Ack onCompleted");
                        countDownLatch.countDown();
                    }
                };
                stub.receiveAck(ackMessage, emptyStreamObserver);
                logger.info("Sending ack executed");
            }
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param request is an Ack message which contains the actual message inside it along with other attributes
     * @param responseObserver is an empty observer. This is just the syntax to do async calls.
     * @description Parses the AckMessage and if the Ack is received from all the messages it forwards it to the
     *              application
     */
    @Override
    public void receiveAck(final TotalOrderedBroadcast.AckMessage request,
                           final StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {

        logger.info("acknowledgement received for message" + request.getBroadcastMessage().getLamportClock());
        String key = request.getBroadcastMessage().getLamportClock();
        acknowledgementCountMap.putIfAbsent(key, 0);
        int oldVal;
        do {
            oldVal = acknowledgementCountMap.get(key);
        } while(!acknowledgementCountMap.replace(key, oldVal, oldVal+1));

        int numOfServers = appConfig.numOfServers;

        if (Controller.ConsistencyLevel.EVENTUAL == getConsistencyLevel(appConfig.consistency)) {
            numOfServers = numOfServers / 2;
        }

        if (acknowledgementCountMap.getOrDefault(key,0) >= numOfServers) {
            if (!queue.isEmpty()) {

                queue.remove(lamportClockToMessageMap.get(key));
                lamportClockToMessageMap.remove(key);
                acknowledgementCountMap.remove(key);

                logger.info("All acknowledgements received for message " + key);
                logger.info("Delivering message to Application");

                ManagedChannel channel = ManagedChannelBuilder
                        .forAddress(appConfig.tobHost, appConfig.grpcPort)
                        .usePlaintext()
                        .build();

//                ControllerServiceGrpc.ControllerServiceBlockingStub controllerServiceBlockingStub =
//                        ControllerServiceGrpc.newBlockingStub(channel);

                ControllerServiceGrpc.ControllerServiceStub controllerServiceStub =
                        ControllerServiceGrpc.newStub(channel);

                StreamObserver<TotalOrderedBroadcast.Empty> broadcastMessageStreamResponse =
                        new StreamObserver<TotalOrderedBroadcast.Empty>() {
                            @Override
                            public void onNext(TotalOrderedBroadcast.Empty broadcastMessage) {
                                logger.info("on next after delivering the message to the controller application");
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                logger.info("Error delivering the message to the controller application!!" + throwable.getMessage());
                            }

                            @Override
                            public void onCompleted() {
                                logger.info("Completed delivering the message to the controller application!!");
                                channel.shutdown();
                            }
                        };
                /* For most requests request to controller was being cancelled.
                  Fixed the issue of context cancelling by forking the context before sending the request to controller.
                 */
//                controllerServiceBlockingStub.withWaitForReady().handleMessageRequest(request.getBroadcastMessage());
                Context.current().fork().run(()-> {
                            controllerServiceStub.withWaitForReady().handleMessageRequest(request.getBroadcastMessage()
                                    , broadcastMessageStreamResponse);
                        });
                /* ************************************/
                /* send the message to controller part */
                sendAck();
            }
        }
        responseObserver.onNext(TotalOrderedBroadcast.Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    private com.distributedsystems.distributedcache.controller.Controller.ConsistencyLevel getConsistencyLevel(String consistency) {

        switch (consistency) {
            case "sequential":
                return com.distributedsystems.distributedcache.controller.Controller.ConsistencyLevel.SEQUENTIAL;
            case "eventual":
                return com.distributedsystems.distributedcache.controller.Controller.ConsistencyLevel.EVENTUAL;
            case "linearizability":
                return com.distributedsystems.distributedcache.controller.Controller.ConsistencyLevel.LINEARIZABILITY;
            case "causal":
                return com.distributedsystems.distributedcache.controller.Controller.ConsistencyLevel.CAUSAL;
            default:
                return Controller.ConsistencyLevel.DEFAULT;
        }
    }
}
