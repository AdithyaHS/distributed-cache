package com.distributedsystems.distributedcache.Utilities;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class ControllerConfigurations {
    @org.springframework.beans.factory.annotation.Value("${tob.host}")
    public String tobHost;
    @org.springframework.beans.factory.annotation.Value("${tob.port}")
    public int tobPort;
    @org.springframework.beans.factory.annotation.Value("${grpc.port}")
    public int grpcPort;
    @org.springframework.beans.factory.annotation.Value("${controller.id}")
    public int controllerId;
    @org.springframework.beans.factory.annotation.Value("${redis.host}")
    public String redisHost;
    @org.springframework.beans.factory.annotation.Value("${redis.port}")
    public int redisPort;
    @org.springframework.beans.factory.annotation.Value("${num.of.servers}")
    public int numOfServers;
    @org.springframework.beans.factory.annotation.Value("${consistency}")
    public String consistency;
    @org.springframework.beans.factory.annotation.Value("${tob.servers}")
    public String tobServers;
}
