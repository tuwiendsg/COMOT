package at.ac.tuwien.dsg.comot.common.model.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.ac.tuwien.dsg.comot.common.model.AbstractEntity;
import at.ac.tuwien.dsg.comot.common.model.elastic.Capability;
import at.ac.tuwien.dsg.comot.common.model.elastic.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.elastic.Requirement;
import at.ac.tuwien.dsg.comot.common.model.elastic.SyblDirective;

public abstract class ServicePart extends AbstractEntity {

	protected List<SyblDirective> directives = new ArrayList<>();
	protected List<Requirement> requirements = new ArrayList<>();
	protected List<Capability> capabilities = new ArrayList<>();
	protected List<ElasticityCapability> elasticityCapabilities = new ArrayList<>();
	protected Map<String, Object> properties = new HashMap<String, Object>();

	public ServicePart() {
	}

	public ServicePart(String id) {
		super(id);
	}

	public ServicePart(String id, String type) {
		super(id, type);
	}

	public ServicePart(String id, String type, String name) {
		super(id, type, name);
	}

	public ServicePart(String id, String type, List<SyblDirective> directives, List<Requirement> requirements,
			List<Capability> capabilities, List<ElasticityCapability> elasticityCapabilities,
			Map<String, Object> properties) {
		super(id, type);
		this.directives = directives;
		this.requirements = requirements;
		this.capabilities = capabilities;
		this.elasticityCapabilities = elasticityCapabilities;
		this.properties = properties;
	}

	public ServicePart(String id, String type, String name, List<SyblDirective> directives,
			List<Requirement> requirements, List<Capability> capabilities,
			List<ElasticityCapability> elasticityCapabilities, Map<String, Object> properties) {
		this(id, type, directives, requirements, capabilities, elasticityCapabilities, properties);
		this.name = name;
	}

	public void addSyblDirective(SyblDirective directive) {
		if (directives == null) {
			directives = new ArrayList<>();
		}
		directives.add(directive);
	}

	public void addRequirement(Requirement requirement) {
		if (requirements == null) {
			requirements = new ArrayList<>();
		}
		requirements.add(requirement);
	}

	public void addCapability(Capability capability) {
		if (capabilities == null) {
			capabilities = new ArrayList<>();
		}
		capabilities.add(capability);
	}

	public void addElasticityCapability(ElasticityCapability elasticityCapability) {
		if (elasticityCapabilities == null) {
			elasticityCapabilities = new ArrayList<>();
		}
		elasticityCapabilities.add(elasticityCapability);
	}

	public void addProperty(String key, Object value) {
		if (properties == null) {
			properties = new HashMap<>();
		}
		properties.put(key, value);
	}

	// GENERATED METHODS

	public List<SyblDirective> getDirectives() {
		return directives;
	}

	public void setDirectives(List<SyblDirective> directives) {
		this.directives = directives;
	}

	public List<Requirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<Requirement> requirements) {
		this.requirements = requirements;
	}

	public List<Capability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<Capability> capabilities) {
		this.capabilities = capabilities;
	}

	public List<ElasticityCapability> getElasticityCapabilities() {
		return elasticityCapabilities;
	}

	public void setElasticityCapabilities(List<ElasticityCapability> elasticityCapabilities) {
		this.elasticityCapabilities = elasticityCapabilities;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((capabilities == null) ? 0 : capabilities.hashCode());
		result = prime * result + ((directives == null) ? 0 : directives.hashCode());
		result = prime * result + ((elasticityCapabilities == null) ? 0 : elasticityCapabilities.hashCode());
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		result = prime * result + ((requirements == null) ? 0 : requirements.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServicePart other = (ServicePart) obj;
		if (capabilities == null) {
			if (other.capabilities != null)
				return false;
		} else if (!capabilities.equals(other.capabilities))
			return false;
		if (directives == null) {
			if (other.directives != null)
				return false;
		} else if (!directives.equals(other.directives))
			return false;
		if (elasticityCapabilities == null) {
			if (other.elasticityCapabilities != null)
				return false;
		} else if (!elasticityCapabilities.equals(other.elasticityCapabilities))
			return false;
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (requirements == null) {
			if (other.requirements != null)
				return false;
		} else if (!requirements.equals(other.requirements))
			return false;
		return true;
	}

}
