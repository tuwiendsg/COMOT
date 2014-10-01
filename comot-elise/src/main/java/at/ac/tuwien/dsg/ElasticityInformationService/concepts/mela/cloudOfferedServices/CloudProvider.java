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

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.Entity;
import at.ac.tuwien.dsg.ElasticityInformationService.concepts.salsa.cloudservicestructure.ServiceUnit;

/**
 *
 * @Author Daniel Moldovan, Duc-Hung Le
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
@NodeEntity
public class CloudProvider extends Entity {

    private Set<CloudOfferedServiceUnit> serviceUnits;	// e.g m1.small VM, float IP
	private Set<ServiceUnit> serviceProviding;	// e.g. VM
    
    private String type = Type.IAAS;

    {
        serviceUnits = new HashSet<CloudOfferedServiceUnit>();
    }

    public CloudProvider() {
    }

    public CloudProvider(String name) {
        super(name);
    }

    public CloudProvider(String name, String type) {
        super(name);
        this.type = type;
    }

    public Set<CloudOfferedServiceUnit> getServiceUnits() {
        return serviceUnits;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addServiceUnit(CloudOfferedServiceUnit u) {
        this.serviceUnits.add(u);
    }

    public void removeServiceUnit(CloudOfferedServiceUnit u) {
        this.serviceUnits.remove(u);
    }
    
    public void addServiceProviding(ServiceUnit u){
    	this.serviceProviding.add(u);
    }
    
    public void removeServiceProviding(ServiceUnit u){
    	this.serviceProviding.remove(u);
    }

    public void setServiceUnits(Set<CloudOfferedServiceUnit> serviceUnits) {
        this.serviceUnits = serviceUnits;
    }
    
    

    @Override
    public String toString() {
        return "CloudProvider{" + "name=" + name + ", type=" + type + '}';
    }

    public interface Type {

        public static final String IAAS = "IAAS";
        public static final String PAAS = "PAAS";
    }
}
