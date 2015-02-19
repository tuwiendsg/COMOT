package at.ac.tuwien.dsg.comot.m.common;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.neo4j.annotation.NodeEntity;

@XmlAccessorType(XmlAccessType.FIELD)
@NodeEntity
@XmlRootElement
public class StateMessage {

	protected String targetedId;
	protected Map<String, State> allIds = new HashMap<>();

	public StateMessage() {

	}

	public StateMessage(String targetedId, State state) {
		super();
		this.targetedId = targetedId;
		addOne(targetedId, state);
	}

	public void addOne(String id, State state) {
		if (allIds == null) {
			allIds = new HashMap<>();
		}
		allIds.put(id, state);
	}

	public String getTargetedId() {
		return targetedId;
	}

	public void setTargetedId(String targetedId) {
		this.targetedId = targetedId;
	}

	public Map<String, State> getAllIds() {
		return allIds;
	}

	public void setAllIds(Map<String, State> allIds) {
		this.allIds = allIds;
	}

}
