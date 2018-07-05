package cn.mrray.blockchain.core.util;


import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;

@Component
public class PropertiesPo {
    private static Properties proper;

    public PropertiesPo() {
        //try (InputStream is = PropertiesPo.class.getResourceAsStream("/ConfigInfo.properties")) {
        try {
            InputStream is;
            File file = new File("/opt/docker_container/peer/ConfigInfo.properties");
            if (file.exists()) {
                System.out.println("local file exists");
                is = new FileInputStream(file);
            } else {
                is = PropertiesPo.class.getResourceAsStream("/ConfigInfo.properties");
            }
            proper = new Properties();
            proper.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //根据Key读取Value
    public String GetValueByKey(String filePath, String key) {
        Properties pps = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            pps.load(in);
            String value = pps.getProperty(key);
            return value;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getValueByKey(String key) {
        String value = proper.getProperty(key);
        return value;
    }
}
