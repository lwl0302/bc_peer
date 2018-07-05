package cn.mrray.blockchain.core.kafka;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.util.PropertiesPo;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;

import java.util.Properties;

public class ProductConnection {
    public Producer<String, String> producer = null;
    private static ProductConnection instance = null;
    private static final PropertiesPo configProper = ApplicationContextProvider.getBean(PropertiesPo.class);

    public static synchronized ProductConnection getInstance() {
        if (instance == null) {
            instance = new ProductConnection();
        }
        return instance;
    }

    private ProductConnection() {
        try {

            Properties props = new Properties();
            props.put("bootstrap.servers", configProper.getValueByKey("bootstrap.servers"));
            props.put("acks", configProper.getValueByKey("acks"));
            props.put("retries", configProper.getValueByKey("retries"));
            props.put("batch.size", configProper.getValueByKey("batch.size"));
            props.put("key.serializer", configProper.getValueByKey("key.serializer"));
            props.put("value.serializer", configProper.getValueByKey("value.serializer"));

            producer = new KafkaProducer<>(props);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
