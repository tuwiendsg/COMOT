package at.ac.tuwien.dsg.comot.common.model;

public abstract class AbstractEntity {

	protected String id;
	protected String description;
	protected String type;
	protected String name;

	public AbstractEntity() {
	}

	public AbstractEntity(String id) {
		this.id = id;
	}

	public AbstractEntity(String id, String type) {
		this.id = id;
		this.type = type;
	}

	public AbstractEntity(String id, String type, String name) {
		this.id = id;
		this.type = type;
		this.name = name;
	}

	public AbstractEntity(String id, String type, String name, String description) {
		this.id = id;
		this.description = description;
		this.type = type;
		this.name = name;
	}

	// GENERATED METHODS

	public String getType() {
		return type;
	}

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

	public void setType(String type) {
		this.type = type;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
