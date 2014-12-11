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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;

/**
 * One elasticity capability has more dependencies, between which we need to choose 1
 * 
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
@NodeEntity
@TypeAlias("ElasticityCapability")
public class ElasticityCapability extends ServiceEntity {


    private Set<Dependency> dependencies;

    private String phase = Phase.INSTANTIATION_TIME;

    {
        dependencies = new HashSet<Dependency>();
    }

    public ElasticityCapability() {
    }

    public ElasticityCapability(String name) {
        super(name);
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public void setCapabilityDependencies(Set<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public Set<Dependency> getCapabilityDependencies() {
        return dependencies;
    }

    public void addCapabilityDependency(Dependency e) {
        dependencies.add(e);
    }

    public void removeCapabilityDependencies(Dependency e) {
        dependencies.remove(e);
    }

    public void addCapabilityDependencies(List<Dependency> e) {
        dependencies.addAll(e);
    }

    public void removeCapabilityDependencies(List<Dependency> e) {
        dependencies.removeAll(e);
    }

    public List<ServiceEntity> getOptionalDependencies() {
        List<ServiceEntity> optionalDepenencies = new ArrayList<ServiceEntity>();
        for (Dependency d : dependencies) {
            if (d.getDependencyType().equals(Type.OPTIONAL_ASSOCIATION)) {
                optionalDepenencies.add(d.target);
            }
        }

        return optionalDepenencies;
    }

    public List<ServiceEntity> getMandatoryDependencies() {
        List<ServiceEntity> optionalDepenencies = new ArrayList<ServiceEntity>();
        for (Dependency d : dependencies) {
            if (d.getDependencyType().equals(Type.MANDATORY_ASSOCIATION)) {
                optionalDepenencies.add(d.target);
            }
        }

        return optionalDepenencies;
    }

    /**
     * assumes that the ElasticityCapability hlds entities of the same type
     *
     * @return
     */
    public Class getTargetType() {
        if (dependencies.isEmpty()) {
            return null;
        } else {
        	Iterator<Dependency> iter = dependencies.iterator();
        	ServiceEntity target= iter.next().getTarget();
//            Entity target = dependencies.get(0).getTarget();
            return target.getClass();
        }
    }

    @Override
    public String toString() {
        return "ElasticityCapability{" + "name=" + name + ", phase=" + phase + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
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
        final ElasticityCapability other = (ElasticityCapability) obj;

        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public interface Type {

        String MANDATORY_ASSOCIATION = "MandatoryAssociation";
        String OPTIONAL_ASSOCIATION = "OptionalAssociation";
    }

    public interface Phase {

        String RUN_TIME = "ExecutionTime";
        String INSTANTIATION_TIME = "InstantiationTime";
        String BOTH = "Both";
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "Dependency")
    public static class Dependency {

        //entity for which the characteristic is defined (Resource or Quality)
        //this is abstract: Example: Computing
        @XmlElement(name = "DependencyTarget", required = false)
        private ServiceEntity target;

        @XmlElement(name = "CapabilityTarget", required = false)
        private String dependencyType;

        @XmlElement(name = "Volatility", required = true)
        private Volatility volatility;
        
        {
            volatility = new Volatility();
        }

        public ServiceEntity getTarget() {
            return target;
        }

        public void setTarget(ServiceEntity target) {
            this.target = target;
        }

        public Dependency withTarget(ServiceEntity target) {
            this.target = target;
            return this;
        }

        public String getDependencyType() {
            return dependencyType;
        }

        public void setDependencyType(String dependencyType) {
            this.dependencyType = dependencyType;
        }

        public Dependency withType(String dependencyType) {
            this.dependencyType = dependencyType;
            return this;

        }

        public Volatility getVolatility() {
            return volatility;
        }

        public void setVolatility(Volatility volatility) {
            this.volatility = volatility;
        }
        
        
        public Dependency withVolatility(Volatility volatility) {
            this.volatility = volatility;
            return this;
        }
        
        

        public Dependency() {
        }

        public Dependency(ServiceEntity target, String dependencyType) {
            this.target = target;
            this.dependencyType = dependencyType;
        }

    }
}
