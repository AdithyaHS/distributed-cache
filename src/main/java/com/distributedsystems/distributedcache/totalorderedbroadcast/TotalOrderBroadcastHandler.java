package com.distributedsystems.distributedcache.totalorderedbroadcast;

import com.distributedsystems.distributedcache.dto.TotalOrderedBroadcastMessage;
import io.grpc.stub.StreamObserver;

import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class TotalOrderBroadcastHandler extends TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceImplBase {

    private static int NUMBER_OF_PROCESSES = 2;

    private HashMap<String, TotalOrderedBroadcastMessage> lamportClockToMessageMap =
            new HashMap<String, TotalOrderedBroadcastMessage>();

    private HashMap<String, Integer> acknowledgementCountMap = new HashMap<String, Integer>();

    private PriorityBlockingQueue<TotalOrderedBroadcastMessage> queue = new PriorityBlockingQueue<TotalOrderedBroadcastMessage>
            (1000, new Comparator<TotalOrderedBroadcastMessage>() {

                @Override
                public int compare(TotalOrderedBroadcastMessage o1, TotalOrderedBroadcastMessage o2) {
                    return Integer.parseInt(o1.getBroadcastMessage().getLamportClock()) -
                            Integer.parseInt(o2.getBroadcastMessage().getLamportClock());
                }
            });

    private StreamObserver<TotalOrderedBroadcast.Empty> getEmptyStreamObserver() {
        return new StreamObserver<TotalOrderedBroadcast.Empty>() {
            @Override
            public void onNext(TotalOrderedBroadcast.Empty empty) {
                System.out.println("In server onNext" + empty.getLamportClock());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("In server onCompleted");
            }
        };
    }

    @Override
    public void sendBroadcastMessage(final TotalOrderedBroadcast.BroadcastMessage request,
                                     final StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {

        StreamObserver<TotalOrderedBroadcast.Empty> totalOrderBroadcastMessageObserver = getEmptyStreamObserver();

        for (TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub : ClientStubs.getInstance().getStubs()) {
            stub.withWaitForReady().receiveBroadcastMessage(request, totalOrderBroadcastMessageObserver);
        }

    }

    @Override
    public void receiveBroadcastMessage(final TotalOrderedBroadcast.BroadcastMessage request,
                                        final StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {

        final TotalOrderedBroadcastMessage totalOrderedBroadcastMessage =
                new TotalOrderedBroadcastMessage(request, false);

        lamportClockToMessageMap.put(request.getLamportClock(), totalOrderedBroadcastMessage);

        if (!queue.offer(totalOrderedBroadcastMessage)) {
            responseObserver.onError(new Error("Not able to add to the queue"));
        }

        if (!queue.peek().isAcknowledgementPublished()) {
            sendAck();
        }
    }

    private void sendAck() {
        System.out.println(acknowledgementCountMap);
        if (!queue.peek().isAcknowledgementPublished()) {
            TotalOrderedBroadcast.AckMessage ackMessage = TotalOrderedBroadcast.AckMessage.newBuilder()
                    .setBroadcastMessage(queue.peek().getBroadcastMessage())
                    .setIsAcknowledgementPublished(true)
                    .build();
            StreamObserver<TotalOrderedBroadcast.Empty> emptyStreamObserver = getEmptyStreamObserver();

            for (TotalOrderBroadcastServiceGrpc.TotalOrderBroadcastServiceStub stub : ClientStubs.getInstance().getStubs()) {
                stub.receiveAck(ackMessage, emptyStreamObserver);
            }
            queue.peek().setAcknowledgementPublished(true);
        }
    }

    @Override
    public void receiveAck(final TotalOrderedBroadcast.AckMessage request,
                           final StreamObserver<TotalOrderedBroadcast.Empty> responseObserver) {

        System.out.println(acknowledgementCountMap);
        System.out.println("acknowledgement received for message" + request.getBroadcastMessage().getLamportClock());
        String key = request.getBroadcastMessage().getLamportClock();
        int count = acknowledgementCountMap.getOrDefault(key, 0) + 1;
        acknowledgementCountMap.put(key, count);

        if (acknowledgementCountMap.get(key) > NUMBER_OF_PROCESSES) {
            queue.remove(lamportClockToMessageMap.get(key));
            System.out.println("acknowlegement received");
            /* ************************************/
            /* send the message to controller part */
            sendAck();
        }
    }
}
