package com.distributedsystems.distributedcache.consistency;

import java.util.HashMap;

public class ConsistencyRequest {


    private String value;
    private String key;
    private String lamportClock;
    private HashMap<String, BroadcastStatus> pendingRequests;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLamportClock() {
        return lamportClock;
    }

    public void setLamportClock(String lamportClock) {
        this.lamportClock = lamportClock;
    }

    public HashMap<String, BroadcastStatus> getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(HashMap<String, BroadcastStatus> pendingRequests) {
        this.pendingRequests = pendingRequests;
    }
}

