package com.distributedsystems.distributedcache.dto;

import com.distributedsystems.distributedcache.totalorderedbroadcast.TotalOrderedBroadcast;

public class TotalOrderedBroadcastMessage {
    private TotalOrderedBroadcast.BroadcastMessage broadcastMessage;
    private boolean isAcknowledgementPublished;

    public TotalOrderedBroadcastMessage(TotalOrderedBroadcast.BroadcastMessage broadcastMessage, boolean isAcknowledgementPublished) {
        this.broadcastMessage = broadcastMessage;
        this.isAcknowledgementPublished = isAcknowledgementPublished;
    }

    public TotalOrderedBroadcast.BroadcastMessage getBroadcastMessage() {
        return broadcastMessage;
    }

    public void setBroadcastMessage(TotalOrderedBroadcast.BroadcastMessage broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }

    public boolean isAcknowledgementPublished() {
        return isAcknowledgementPublished;
    }

    public void setAcknowledgementPublished(boolean acknowledgementPublished) {
        isAcknowledgementPublished = acknowledgementPublished;
    }
}
