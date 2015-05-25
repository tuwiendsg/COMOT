/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.Communication;

/**
 *
 * @author hungld
 */
public class EliseQueueTopic {

    private static final String prefix = "at.ac.tuwien.dsg.elise";
    public static final String QUERY_TOPIC = prefix + ".query";
    public static final String FEEDBACK_TOPIC = prefix + ".feedback.";
    public static final String NOTIFICATION_TOPIC = prefix + ".notification";

    public static String getFeedBackTopic(String feedBackID) {
        return "at.ac.tuwien.dsg.elise.feedback." + feedBackID;
    }
}
