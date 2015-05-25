/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorInterface;

import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;

/**
 *
 * @author hungld
 */
public interface MessageSubscribeInterface {

    public void subscribe(String topic);

    public void handleMessage(EliseMessage paramEliseMessage);
}
