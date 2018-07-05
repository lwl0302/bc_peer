package cn.mrray.blockchain.core.kafka;

import cn.mrray.blockchain.core.ApplicationContextProvider;
import cn.mrray.blockchain.core.util.PropertiesPo;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

public class ConsumerConnection {
    public KafkaConsumer<String, String> CONSUMER;

    public ConsumerConnection() {
        Properties props = new Properties();

        //@Resource
        PropertiesPo configProper = ApplicationContextProvider.getBean(PropertiesPo.class);
        props.put("bootstrap.servers", configProper.getValueByKey("bootstrap.servers"));
        props.put("group.id", configProper.getValueByKey("group.id"));
        props.put("enable.auto.commit", configProper.getValueByKey("enable.auto.commit"));
        props.put("auto.commit.interval.ms", configProper.getValueByKey("auto.commit.interval.ms"));
        props.put("session.timeout.ms", configProper.getValueByKey("session.timeout.ms"));
        props.put("auto.offset.reset", configProper.getValueByKey("auto.offset.reset"));
        props.put("key.deserializer", configProper.getValueByKey("key.deserializer"));
        props.put("value.deserializer", configProper.getValueByKey("value.deserializer"));

        CONSUMER = new KafkaConsumer<String, String>(props);

        CONSUMER.subscribe(Collections.singletonList(configProper.getValueByKey("topic")));
        CONSUMER.seekToBeginning(new ArrayList<TopicPartition>());

        Map<String, List<PartitionInfo>> listTopics = CONSUMER.listTopics();
        Set<Map.Entry<String, List<PartitionInfo>>> entries = listTopics.entrySet();

        for (Map.Entry<String, List<PartitionInfo>> entry :
                entries) {
            System.out.println("topic:" + entry.getKey());
            /*List<PartitionInfo> partitionInfos = listTopics.get(entry.getKey());
            for (PartitionInfo partitionInfo : partitionInfos) {
                System.out.println("PartitionInfo:" + partitionInfo);
            }*/
        }
    }
}

