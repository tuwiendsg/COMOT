/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.messaging.api;

/**
 *
 * @author vauvenal5
 */
public interface Message {
	/**
	 * Sets the content of the message.
	 * @param content - The message content.
	 */
	void setMessage(byte[] content);
	/**
	 * Returns the content of the message.
	 * @return - The message content.
	 */
	byte[] getMessage();
	/**
	 * Adds a type to the message.
	 * This will allow you to send the message to one or more different queues depending on the set types.
	 * If no type is set this message will be send to all queues.
	 * @param type - The message type.
	 * @return 
	 */
	Message withType(String type);
}
