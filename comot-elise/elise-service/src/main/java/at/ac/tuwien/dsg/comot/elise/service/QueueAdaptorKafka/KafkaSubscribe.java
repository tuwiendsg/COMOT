/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorKafka;

import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorInterface.MessageSubscribeInterface;
import static at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorKafka.KafkaPublish.logger;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.log4j.Logger;

/**
 *
 * @author hungld
 */
public abstract class KafkaSubscribe implements MessageSubscribeInterface {
    static Logger logger = EliseConfiguration.logger;
    String zooKeeper;

    public KafkaSubscribe(){
        this.zooKeeper = EliseConfiguration.ZOO_KEEPER;
    }
    
    public KafkaSubscribe(String zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @Override
    public void subscribe(String topic) {
        logger.debug("Kafka consumer is subscribing the topic: " + topic);
        Properties properties = new Properties();
        properties.put("zookeeper.connect", zooKeeper);
        properties.put("group.id", "at.ac.tuwien.dsg.elise");
        ConsumerConfig consumerConfig = new ConsumerConfig(properties);
        ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumerConnector.createMessageStreams(topicCountMap);
        KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
        ConsumerIterator<byte[], byte[]> it = stream.iterator();
        while (it.hasNext()) {
            System.out.println(new String(it.next().message()));
            logger.debug("Kafka producer got a message the topic: " + topic);
            handleMessage(EliseMessage.fromJson(it.next().message()));
        }
    }

    @Override
    public abstract void handleMessage(EliseMessage paramEliseMessage);
}
