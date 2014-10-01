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
package at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.cloudOfferedServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.LinkType;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnit;



/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
@NodeEntity
public class CloudOfferedServiceUnit extends Entity{
	private static final long serialVersionUID = 1L;

	@RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_BELONGS_TO_PROVIDER, direction=Direction.OUTGOING)
	@Fetch
	CloudProvider provider;
	
    private String category;
    private String subcategory;
   
    private Set<CostFunction> costFunctions;
    
    @RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_RESOURCE, direction=Direction.OUTGOING)
    @Fetch private Set<ResourceValue> resourceProperties;
    
    @RelatedToVia(type = LinkType.CLOUD_OFFER_SERVICE_HAS_QUALITY, direction=Direction.OUTGOING)
    @Fetch private Set<QualityValue> qualityProperties;

    //holds dynamic properties , i.e. elasticity capabilities, something you can change
	
    private Set<ElasticityCapability> elasticityCapabilities;
    
    @RelatedTo(type = LinkType.CLOUD_OFFER_SERVICE_DERIVES_SERVICE_UNIT, direction=Direction.OUTGOING)
	@Fetch
    private ServiceUnit derivedServiceUnit;
    
    
    

//    
    //from here onwards associations )optional or mandatory) are seen as ElasticityCapabilities
//    private List<serviceUnit> mandatoryAssociations;
//    private List<serviceUnit> optionalAssociations;
    {    	
        costFunctions = new HashSet<CostFunction>();
        qualityProperties = new HashSet<QualityValue>();
        resourceProperties = new HashSet<ResourceValue>();
        elasticityCapabilities = new HashSet<ElasticityCapability>();
    }

    public CloudOfferedServiceUnit() { }

    public CloudOfferedServiceUnit(String category, String subcategory, String name) {
        this.category = category;
        this.subcategory = subcategory;
        this.name = name;
    }
   
	public ServiceUnit getDerivedServiceUnit() {
		return derivedServiceUnit;
	}

	public void setDerivedServiceUnit(ServiceUnit derivedServiceUnit) {
		this.derivedServiceUnit = derivedServiceUnit;
	}

	public CloudProvider getProvider() {
		return provider;
	}

	public void setProvider(CloudProvider provider) {
		this.provider = provider;
	}

	public void setCostFunctions(Set<CostFunction> costFunctions) {
        this.costFunctions = costFunctions;
    }

    public void setResourceProperties(Set<ResourceValue> resourceProperties) {
        this.resourceProperties = resourceProperties;
    }

    public void setQualityProperties(Set<QualityValue> qualityProperties) {
        this.qualityProperties = qualityProperties;
    }

    public void addCostFunction(CostFunction cf) {
        costFunctions.add(cf);
    }

    public void removeCostFunction(CostFunction cf) {
        costFunctions.remove(cf);
    }

    public void addResourceProperty(ResourceValue resource) {    	
        resourceProperties.add(resource);
        // link
        
    }

    public void removeResourceProperty(ResourceValue resource) {
        resourceProperties.remove(resource);
    }

    public void addQualityProperty(QualityValue quality) {
        qualityProperties.add(quality);
    }

    public void removeQualityProperty(QualityValue quality) {
        qualityProperties.remove(quality);
    }

    public Set<CostFunction> getCostFunctions() {
        return costFunctions;
    }

    public Set<ResourceValue> getResourceProperties() {
        return resourceProperties;
    }

    public Set<QualityValue> getQualityProperties() {
        return qualityProperties;
    }

    public Set<ElasticityCapability> getElasticityCapabilities() {
        return elasticityCapabilities;
    }

    public void setElasticityCapabilities(Set<ElasticityCapability> elasticityCapabilities) {
        this.elasticityCapabilities = elasticityCapabilities;
    }

    public void addElasticityCapability(ElasticityCapability characteristic) {
        this.elasticityCapabilities.add(characteristic);
    }

    public void removeElasticityCapability(ElasticityCapability characteristic) {
        this.elasticityCapabilities.remove(characteristic);
    }

    public List<ElasticityCapability> getServiceUnitAssociations() {

        List<ElasticityCapability> mandatoryAssociations = new ArrayList<ElasticityCapability>();

        for (ElasticityCapability capability : getElasticityCapabilities()) {

            //only optional associations towards ServiceUnit
            if (!capability.getTargetType().equals(CloudOfferedServiceUnit.class)) {
                continue;
            }

            mandatoryAssociations.add(capability);
        }

        return mandatoryAssociations;
    }

    public List<ElasticityCapability> getResourceAssociations() {

        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();

        for (ElasticityCapability capability : getElasticityCapabilities()) {

            //only optional associations towards ServiceUnit
            if (!capability.getTargetType().equals(ResourceValue.class)) {
                continue;
            }

            optionalAssociations.add(capability);
        }

        return optionalAssociations;
    }

    public List<ElasticityCapability> getQualityAssociations() {

        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();

        for (ElasticityCapability capability : getElasticityCapabilities()) {

            //only optional associations towards ServiceUnit
            if (!capability.getTargetType().equals(QualityValue.class)) {
                continue;
            }

            optionalAssociations.add(capability);
        }

        return optionalAssociations;
    }

    public List<ElasticityCapability> getCostAssociations() {

        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();

        for (ElasticityCapability capability : getElasticityCapabilities()) {

            //only optional associations towards ServiceUnit
            if (!capability.getTargetType().equals(CostFunction.class)) {
                continue;
            }

            optionalAssociations.add(capability);
        }

        return optionalAssociations;
    }

    public List<ElasticityCapability> getElasticityCapabilities(Class capabilitiesTargetClass) {

        List<ElasticityCapability> optionalAssociations = new ArrayList<ElasticityCapability>();

        for (ElasticityCapability capability : getElasticityCapabilities()) {
            if (!capability.getTargetType().equals(capabilitiesTargetClass)) {
                continue;
            }
            optionalAssociations.add(capability);
        }

        return optionalAssociations;
    }

    public interface Category {

        String IAAS = "IaaS";
        String PAASS = "OaaS";
        String MAAS = "MaaS";
    }

    @Override
    public String toString() {
        return "ServiceUnit{" + "name=" + name + ", costFunctions=" + costFunctions + ", resourceProperties=" + resourceProperties + ", qualityProperties=" + qualityProperties + ", elasticityCapabilities=" + elasticityCapabilities + '}';
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
//        if ((this.provider == null) ? (other.provider != null) : !this.provider.equals(other.provider)) {
//            return false;
//        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
