/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.messaging.rabbitMq;

import at.ac.tuwien.dsg.comot.messaging.api.Message;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 *
 * @author vauvenal5
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
