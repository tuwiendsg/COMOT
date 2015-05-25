/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.common.message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author hungld
 */
public class EliseMessage {

    COMMAND command;
    
    String fromElise;

    String topic;

    String feedbackTopic;

    String payload;
    
    boolean update;
    
    boolean notify;
    
    long timeStamp;

    public EliseMessage() {
    }

    public EliseMessage(COMMAND command, String fromElise, String topic, String feedbackTopic, String payload) {
        this.fromElise = fromElise;
        this.command = command;
        this.topic = topic;
        this.feedbackTopic = feedbackTopic;
        this.payload = payload;
        this.timeStamp = System.currentTimeMillis();
    }

    public enum COMMAND {

        discover,
        queryInstance,
        queryProvider,
        answer  // dedicate for answer
    }

    public static String generateTopic() {
        return "at.ac.tuwien.dsg.comot.elise." + UUID.randomUUID().toString();
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static EliseMessage fromJson(byte[] bytes) {
        return fromJson(new String(bytes, StandardCharsets.UTF_8));
    }

    public static EliseMessage fromJson(String s) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(s, EliseMessage.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public COMMAND getCommand() {
        return command;
    }

    public String getTopic() {
        return topic;
    }

    public String getFeedbackTopic() {
        return feedbackTopic;
    }

    public String getPayload() {
        return payload;
    }

    public String getFromElise() {
        return fromElise;
    }

    public boolean isUpdate() {
        return update;
    }

    public boolean isNotify() {
        return notify;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    
    

    @Override
    public String toString() {
        return "EliseMessage{" + "command=" + command + ", payload=" + payload + '}';
    }
    
    
    
}
