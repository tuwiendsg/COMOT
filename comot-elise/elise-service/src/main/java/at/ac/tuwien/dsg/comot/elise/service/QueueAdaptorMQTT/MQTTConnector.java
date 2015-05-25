/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT;

import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author hungld
 */
public class MQTTConnector {

    Logger logger = EliseConfiguration.logger;
    String broker = "tcp://iot.eclipse.org:1883";
    String clientId = UUID.randomUUID().toString();
    MemoryPersistence persistence = new MemoryPersistence();
    int qos = 2;

    public MQTTConnector() {

    }

    public MQTTConnector(String broker) {
        this.broker = broker;
    }

    MqttClient queueClient = null;

    public boolean connect() {
        try {
            this.queueClient = new MqttClient(this.broker, this.clientId, this.persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + this.broker);
            this.queueClient.connect(connOpts);
            if (this.queueClient.isConnected()) {
                this.logger.debug("Successfully connected to the broker: " + this.broker);
            } else {
                this.logger.error("Failed to connect to the broker: " + this.broker);
            }
            return true;
        } catch (MqttException ex) {
            this.logger.debug(ex);
            ex.printStackTrace();
        }
        return false;
    }

    public void disconnect() {
        if ((this.queueClient != null) && (this.queueClient.isConnected())) {
            try {
                this.queueClient.disconnect();
            } catch (MqttException ex) {
                this.logger.debug(ex);
                ex.printStackTrace();
            }
        }
    }

    public String genClientID() {
        return UUID.randomUUID().toString().substring(0, 10);
    }

    private static String byteArrayToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
