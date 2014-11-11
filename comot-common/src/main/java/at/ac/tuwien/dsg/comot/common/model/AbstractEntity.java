package at.ac.tuwien.dsg.comot.common.model;

import java.io.Serializable;

public abstract class AbstractEntity implements Serializable {

	protected String id;
	protected String name;
	protected String description;

	public AbstractEntity() {
	}

	public AbstractEntity(String id) {
		this.id = id;
	}

	public AbstractEntity(String id, String name) {
		this.id = id;
		this.name = name;
	}

	// GENERATED METHODS

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

}
