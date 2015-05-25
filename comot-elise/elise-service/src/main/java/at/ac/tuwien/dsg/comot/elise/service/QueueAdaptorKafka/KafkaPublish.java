/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorKafka;

import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorInterface.MessagePublishInterface;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import java.util.Properties;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.apache.log4j.Logger;

/**
 *
 * @author hungld
 */
public class KafkaPublish implements MessagePublishInterface {
    static Logger logger = EliseConfiguration.logger;
    String broker;

    public KafkaPublish(){
        this.broker = EliseConfiguration.BROKER;
    }
    
    public KafkaPublish(String broker) {
        this.broker = broker;
    }

    @Override
    public void pushMessage(EliseMessage content) {
        logger.debug("Kafka producer is sending message to topic: " + content.getTopic());
        Properties properties = new Properties();
        properties.put("metadata.broker.list", this.broker);
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        ProducerConfig producerConfig = new ProducerConfig(properties);
        kafka.javaapi.producer.Producer<String, EliseMessage> producer = new kafka.javaapi.producer.Producer<>(producerConfig);

        KeyedMessage<String, EliseMessage> message = new KeyedMessage<>(content.getTopic(), content);
        producer.send(message);
        logger.debug("Kafka producer is sending done: " + content.getTopic());
        producer.close();
    }

}
