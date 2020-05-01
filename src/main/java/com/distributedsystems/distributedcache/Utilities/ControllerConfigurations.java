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
    @org.springframework.beans.factory.annotation.Value("${controller.id}")
    public int controllerId;
}
