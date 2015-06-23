/* 
 * Copyright 2015 Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.ac.tuwien.dsg.comot.messaging.api;

/**
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
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
