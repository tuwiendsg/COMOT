/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.messaging.api;

import java.util.EventListener;

/**
 *
 * @author vauvenal5
 */
public interface MessageReceivedListener extends EventListener {
	void messageRecived(Message message);
}
