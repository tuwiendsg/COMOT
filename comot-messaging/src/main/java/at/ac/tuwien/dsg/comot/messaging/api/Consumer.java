/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.messaging.api;

import java.io.IOException;

/**
 *
 * @author vauvenal5
 */
public interface Consumer {
	Message getMessage() throws IOException;
	void addMessageReceivedListener(MessageReceivedListener listener);
	void removeMessageReceivedListener(MessageReceivedListener listener);
	Consumer withType(String type);
}
