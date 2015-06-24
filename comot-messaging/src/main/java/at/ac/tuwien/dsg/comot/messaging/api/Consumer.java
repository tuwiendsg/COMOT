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

import at.ac.tuwien.dsg.comot.messaging.ComotMessagingFactory;
import java.io.IOException;

/**
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public interface Consumer {
	/**
	 * Returns the next message in the queue.
	 * If no types are set it will always return null;
	 * 
	 * @return - The next {@link Message}
	 */
	Message getMessage();
	/**
	 * Add a listener to this consumer which will get notified when a message arrives.
	 * @param listener - The listener to add.
	 */
	void addMessageReceivedListener(MessageReceivedListener listener);
	/**
	 * Remove a listener from this consumer.
	 * @param listener - The listener to remove.
	 */
	void removeMessageReceivedListener(MessageReceivedListener listener);
	/**
	 * This sets the types of messages this consumer shall receive.
	 * 
	 * If no types are set the consumer will not return anything.
	 * 
	 * If you add further types after the consumer has been initialized it will 
	 * try to listen also to this further types.
	 * This behavior depends on the actual framework.
	 * 
	 * @param type - The message type.
	 * @return - This object for function aggregation.
	 * @throws IllegalStateException - If adding a new type is not possible.
	 */
	Consumer withType(String type) throws IllegalStateException;
}
