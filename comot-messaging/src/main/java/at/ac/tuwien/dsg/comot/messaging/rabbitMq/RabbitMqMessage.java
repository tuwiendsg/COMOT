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
package at.ac.tuwien.dsg.comot.messaging.rabbitMq;

import at.ac.tuwien.dsg.comot.messaging.api.Message;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * 
 * @author Svetoslav Videnov <s.videnov@dsg.tuwien.ac.at>
 */
public class RabbitMqMessage implements Message {
	
	private List<String> types;
	private byte[] content;

	@Override
	public void setMessage(byte[] content) {
		this.content = content;
	}

	@Override
	public byte[] getMessage() {
		return content;
	}

	@Override
	public Message withType(String type) {
		this.types.add(type);
		return this;
	}
	
	public String getTypeString() {
		if(this.types.size() == 0) {
			return "#";
		}
		
		return this.types.stream().collect(Collectors.joining("."));		
	}
	
}
