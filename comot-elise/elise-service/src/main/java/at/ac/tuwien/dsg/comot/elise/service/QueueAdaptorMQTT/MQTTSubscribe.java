/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT;

import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorInterface.MessageSubscribeInterface;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author hungld
 */
public abstract class MQTTSubscribe extends MQTTConnector implements MessageSubscribeInterface {

    static Logger logger = EliseConfiguration.logger;

    public MQTTSubscribe(String broker) {
        super(broker);
    }

    public MQTTSubscribe() {
    }

    @Override
    public void subscribe(String topic) {
        MqttCallback callBack = new MqttCallback() {

            @Override
            public void connectionLost(Throwable thrwbl) {
                logger.debug("Queue disconnect " + thrwbl.getMessage());
                thrwbl.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mm) throws Exception {
                logger.debug("A message arrived. Topic: " + topic + "Message size: " + mm.getPayload().length);
                ObjectMapper mapper = new ObjectMapper();
                EliseMessage em = (EliseMessage) mapper.readValue(mm.getPayload(), EliseMessage.class);
                handleMessage(em);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {
                logger.debug("Deliver complete. ");

            }
        };
        if (this.queueClient == null) {
            connect();
        }
        this.queueClient.setCallback(callBack);
        try {
            this.queueClient.subscribe(topic);
        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public abstract void handleMessage(EliseMessage paramEliseMessage);
}
