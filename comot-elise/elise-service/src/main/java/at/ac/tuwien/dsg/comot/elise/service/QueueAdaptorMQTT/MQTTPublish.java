/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT;

import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorInterface.MessagePublishInterface;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author hungld
 */
public class MQTTPublish extends MQTTConnector implements MessagePublishInterface {

    public MQTTPublish() {
    }

    public MQTTPublish(String broker) {
        super(broker);
    }

    @Override
    public void pushMessage(EliseMessage content) {
        try {
            if (this.queueClient == null) {
                connect();
            }
            System.out.println("Publishing message: " + content.getPayload());
            MqttMessage message = new MqttMessage(content.toJson().getBytes());
            message.setQos(this.qos);
            this.queueClient.publish(content.getTopic(), message);
            System.out.println("Message published");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

   
    public void pushMessageAndDisconnect(EliseMessage content) {
        pushMessage(content);
        disconnect();
    }
    
    public void pushCustomData(String content, String topic){
        try {
            if (this.queueClient == null) {
                connect();
            }
            System.out.println("Publishing custom data: " + content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(this.qos);
            this.queueClient.publish(topic, message);
            System.out.println("Message published");
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}
