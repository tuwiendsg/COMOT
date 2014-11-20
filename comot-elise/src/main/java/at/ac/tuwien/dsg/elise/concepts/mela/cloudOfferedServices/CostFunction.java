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
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;

import at.ac.tuwien.dsg.elise.concepts.Link;
import at.ac.tuwien.dsg.elise.concepts.ServiceEntity;

/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
@NodeEntity
@TypeAlias("CostFunction")
public class CostFunction extends ServiceEntity {

    //utility for which cost is applied
//    private serviceUnit utility;
    //if we apply the "utility" together with the entities in this LIST, then the cost
    //is the one specified by this function
    //Is Entity to support ServiceUnit, or Resource and Quality options (you pay separately per I/O performance, etc)

    
    private Set<ServiceEntity> appliedInConjunctionWith;
    
    private Set<CostElement> costElements;

    {
        costElements = new HashSet<CostElement>();
        appliedInConjunctionWith = new HashSet<ServiceEntity>();
    }

    public CostFunction() {
    }

    public CostFunction(String name) {
        super(name);
    }

    public Set<ServiceEntity> getAppliedInConjunctionWith() {
        return appliedInConjunctionWith;
    }

    public void setAppliedInConjunctionWith(Set<ServiceEntity> appliedInConjunctionWith) {
        this.appliedInConjunctionWith = appliedInConjunctionWith;
    }

    public void setCostElements(Set<CostElement> costElements) {
        this.costElements = costElements;
    }

    public List<QualityType> getAppliedInConjunctionWithQuality() {
        List<QualityType> list = new ArrayList<QualityType>();
        for (ServiceEntity e : appliedInConjunctionWith) {
            if (e instanceof QualityType) {
                list.add((QualityType) e);
            }
        }
        return list;
    }

    public List<ResourceType> getAppliedInConjunctionWithResource() {
        List<ResourceType> list = new ArrayList<ResourceType>();
        for (ServiceEntity e : appliedInConjunctionWith) {
            if (e instanceof ResourceType) {
                list.add((ResourceType) e);
            }
        }
        return list;
    }

    public List<CloudOfferedServiceUnit> getAppliedInConjunctionWithServiceUnit() {
        List<CloudOfferedServiceUnit> list = new ArrayList<CloudOfferedServiceUnit>();
        for (ServiceEntity e : appliedInConjunctionWith) {
            if (e instanceof CloudOfferedServiceUnit) {
                list.add((CloudOfferedServiceUnit) e);
            }
        }
        return list;
    }

//    public serviceUnit getUtility() {
//        return utility;
//    }
//
//    public void setUtility(serviceUnit utility) {
//        this.utility = utility;
//    }
//    public CostFunction(serviceUnit utility, List<serviceUnit> appliedInConjunctionWith, Type type) {
//        this.utility = utility;
//        this.appliedInConjunctionWith = appliedInConjunctionWith;
//        this.type = type;
//    }
//
//    public CostFunction(serviceUnit utility, Type type) {
//        this.utility = utility;
//        this.type = type;
//    }
    public void addCostElement(CostElement ce) {
        costElements.add(ce);
    }

    public void removeCostElement(CostElement ce) {
        costElements.remove(ce);
    }

    public void addUtilityAppliedInConjunctionWith(ServiceEntity u) {
        if (u instanceof CostFunction) {
            System.err.println("Adding " + u.name + " as in conjunction with for " + this.name);
            new Exception().printStackTrace();
        }
        this.appliedInConjunctionWith.add(u);
    }

    public void removeUtilityAppliedInConjunctionWith(ServiceEntity u) {
        this.appliedInConjunctionWith.remove(u);
    }

    public Set<CostElement> getCostElements() {
        return costElements;
    }

    @Override
    public String toString() {
        return "CostFunction{" + ", costElements=" + costElements + '}';
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
