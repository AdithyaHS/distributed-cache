package com.distributedsystems.distributedcache.Utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;

@Component
public class Utils {

    @Autowired
    ControllerConfigurations controllerConfigurations;

    public boolean writeToRedis(String key, String value){

        Jedis jedis = new Jedis(new HostAndPort(controllerConfigurations.redisHost, controllerConfigurations.redisPort));
        String str = jedis.set(key, value);
        if(str.equals("OK")){
            return true;
        } else {
            return false;
        }
    }


    public String readFromRedis(String key){
        Jedis jedis = new Jedis(new HostAndPort(controllerConfigurations.redisHost, controllerConfigurations.redisPort));
        if (jedis.get(key) == null) {
            return "";
        }
        return jedis.get(key);
    }
}
