package com.distributedsystems.distributedcache.Utilities;

import redis.clients.jedis.Jedis;

public class Utils {

    public static boolean writeToRedis(String key, String value){

        Jedis jedis = new Jedis();
        String str = jedis.set(key, value);
        if(str.equals("OK")){
            return true;
        } else {
            return false;
        }
    }


    public static String readFromRedis(String key){
        
        Jedis jedis = new Jedis();
        return jedis.get(key);
    }
}
