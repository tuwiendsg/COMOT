/**
 * Copyright 2013 Technische Universitaet Wien (TUW), Distributed Systems Group
 * E184
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.elise.concepts.LinkType;
import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.Links.HasQuality;
import at.ac.tuwien.dsg.elise.concepts.mela.cloudOfferedServices.Links.HasResource;
import at.ac.tuwien.dsg.elise.concepts.salsa.cloudservicestructure.ServiceUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;



/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
@NodeEntity
@TypeAlias("CloudOfferedServiceUnit")
public class CloudOfferedServiceUnit extends ServiceEntity{
	private static final long serialVersionUID = 430086387005314892L;

	@RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_BELONGS_TO_PROVIDER, direction=Direction.OUTGOING)
	@Fetch
	CloudProvider provider;
	
	String serviceName;

//	@RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_BELONGS_TO_PROVIDER, direction=Direction.OUTGOING)
//	@Fetch
//	@JsonIgnore
//	BelongToProvider belongToProvider;
	
    private String category;
    private String subcategory;
    
    @RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_HAS_RESOURCE, direction=Direction.OUTGOING)
    @Fetch
    @JsonIgnore
    private Set<ResourceType> resourceType;
    
    @RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_RESOURCE, direction=Direction.OUTGOING)
    @Fetch
    private Set<HasResource> resourceProperties;
    
//    @RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_HAS_COSTFUNCTION, direction=Direction.OUTGOING)
//    @Fetch
//    @JsonIgnore
//    private Set<CostFunction> costFunctions;
//
//    @RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_HAS_QUALITY, direction=Direction.OUTGOING)
//    @Fetch
//    @JsonIgnore
//    private Set<QualityType> qualityType;
//    
//    @RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_QUALITY, direction=Direction.OUTGOING)
//    @Fetch
//    @JsonIgnore
//    private Set<HasQuality> qualityProperties;
//
    // holds dynamic properties , i.e. elasticity capabilities, something you can change
    @RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_HAS_ELASTICICY_CAPA, direction=Direction.OUTGOING)
    @Fetch
	@JsonIgnore
    private Set<ElasticityCapability> elasticityCapabilities;
    
    @RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_DERIVES_SERVICE_UNIT, direction=Direction.OUTGOING)
	@Fetch
	@JsonIgnore
    private Set<ServiceUnit> derivedServiceUnit;
    
    {    	
        resourceProperties = new HashSet<HasResource>();
    	this.type="CloudOfferedService";
    	
    }

    public CloudOfferedServiceUnit() {
    	super();
    }

    public CloudOfferedServiceUnit(String category, String subcategory, String name) {
    	super("unknownProvider"+"/"+name);
        this.category = category;
        this.subcategory = subcategory;
        this.serviceName = name;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

//	@JsonIgnore
//	public ServiceUnit getDerivedServiceUnit() {
//		return derivedServiceUnit;
//	}
//
	public void addDerivedServiceUnit(ServiceUnit derivedServiceUnit) {
		if (this.derivedServiceUnit == null){
			this.derivedServiceUnit = new HashSet<ServiceUnit>();
		}		
		// add node
		this.derivedServiceUnit.add(derivedServiceUnit);
		// add link
//		if (this.hasDerivedServiceUnit == null){
//			this.hasDerivedServiceUnit = new HashSet<DerivedServiceUnit>();
//		}
//		System.out.println("ADDDDDDDDD ");
//		this.hasDerivedServiceUnit.add(new DerivedServiceUnit(this, derivedServiceUnit));
		
	}

//	@JsonIgnore
//	public CloudProvider getProvider_() {
//		return provider;
//	}

	public void setProvider(CloudProvider provider) {
		this.setName(provider.name+"/"+serviceName);
		this.provider = provider;
	}


    public void setResourceProperties(Set<HasResource> resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

//    public void setQualityProperties(Set<HasQuality> qualityProperties) {
//        this.qualityProperties = qualityProperties;
//    }


    
    
    public void addResourceProperty(HasResource resource) {    	
        resourceProperties.add(resource);
    }

    public String getCategory() {
		return category;
	}

	public String getSubcategory() {
		return subcategory;
	}

	public void removeResourceProperty(HasResource resource) {
        resourceProperties.remove(resource);
    }

//    public void addQualityProperty(HasQuality quality) {
//        qualityProperties.add(quality);
//    }
//
//    public void removeQualityProperty(HasQuality quality) {
//        qualityProperties.remove(quality);
//    }
    
    @JsonIgnore
    public Set<HasResource> getResourceProperties() {
        return resourceProperties;
    }

//    @JsonIgnore
//    public Set<HasQuality> getQualityProperties() {
//        return qualityProperties;
//    }
//
//    @JsonIgnore
//    public Set<ElasticityCapability> getElasticityCapabilities() {
//        return elasticityCapabilities;
//    }
//
//    public void setElasticityCapabilities(Set<ElasticityCapability> elasticityCapabilities) {
//        this.elasticityCapabilities = elasticityCapabilities;
//    }
//
//    public void addElasticityCapability(ElasticityCapability characteristic) {
//        this.elasticityCapabilities.add(characteristic);
//    }
//
//    public void removeElasticityCapability(ElasticityCapability characteristic) {
//        this.elasticityCapabilities.remove(characteristic);
//    }

//    @JsonIgnore
//    public List<ElasticityCapability> getServiceUnitAssociations() {
//
//        List<ElasticityCapability> mandatoryAssociations = new ArrayList<ElasticityCapability>();
//
//        for (ElasticityCapability capability : getElasticityCapabilities()) {
//          
//            if (!capability.getTargetType().equals(CloudOfferedServiceUnit.class)) {
//                continue;
//            }
//
//            mandatoryAssociations.add(capability);
//        }
//
//        return mandatoryAssociations;
//    }
//
//    @JsonIgnore
//    public List<ElasticityCapability> getResourceAssociations() {
//
//        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();
//
//        for (ElasticityCapability capability : getElasticityCapabilities()) {
//
//          
//            if (!capability.getTargetType().equals(HasResource.class)) {
//                continue;
//            }
//
//            optionalAssociations.add(capability);
//        }
//
//        return optionalAssociations;
//    }
//
//    @JsonIgnore
//    public List<ElasticityCapability> getQualityAssociations() {
//
//        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();
//
//        for (ElasticityCapability capability : getElasticityCapabilities()) {
//
//            if (!capability.getTargetType().equals(HasQuality.class)) {
//                continue;
//            }
//
//            optionalAssociations.add(capability);
//        }
//
//        return optionalAssociations;
//    }
//
//    @JsonIgnore
//    public List<ElasticityCapability> getCostAssociations() {
//
//        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();
//
//        for (ElasticityCapability capability : getElasticityCapabilities()) {
//
//            
//            if (!capability.getTargetType().equals(CostFunction.class)) {
//                continue;
//            }
//
//            optionalAssociations.add(capability);
//        }
//
//        return optionalAssociations;
//    }
//
//    @JsonIgnore
//    public List<ElasticityCapability> getElasticityCapabilities(Class capabilitiesTargetClass) {
//
//        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();
//
//        for (ElasticityCapability capability : getElasticityCapabilities()) {
//            if (!capability.getTargetType().equals(capabilitiesTargetClass)) {
//                continue;
//            }
//            optionalAssociations.add(capability);
//        }
//
//        return optionalAssociations;
//    }

    public interface Category {

        String IAAS = "IaaS";
        String PAASS = "OaaS";
        String MAAS = "MaaS";
    }


    @Override
    public int hashCode() {
        int hash = 7;
        
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CloudOfferedServiceUnit other = (CloudOfferedServiceUnit) obj;

        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
