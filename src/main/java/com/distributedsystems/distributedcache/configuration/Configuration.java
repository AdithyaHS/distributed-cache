package com.distributedsystems.distributedcache.configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class Configuration {

    private static Configuration ourInstance = new Configuration();
    private HashMap<String, String> config = new HashMap<String, String>();
    private String TOTAL_BROADCAST_CONFIG_FILE_PATH = "src/main/resources/configuration/serverconf";

    private Configuration() {
        File file = new File(TOTAL_BROADCAST_CONFIG_FILE_PATH);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] temp = line.split(("="));
                if (temp.length >= 2) {
                    config.put(temp[0], temp[1]);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Configuration getInstance() {
        return ourInstance;
    }

    public HashMap<String, String> readConfig() {
        return config;
    }
}
