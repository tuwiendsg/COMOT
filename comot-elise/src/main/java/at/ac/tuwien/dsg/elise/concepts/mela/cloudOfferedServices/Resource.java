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

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.fieldaccess.DynamicPropertiesContainer;

import at.ac.tuwien.dsg.elise.concepts.ResourceQualityOrCostEntity;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.MetricValue;

/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */

public class Resource extends ResourceQualityOrCostEntity implements Cloneable {
//    private Map<Metric, MetricValue> properties;	
	private static final long serialVersionUID = -8960026477565015538L;
	private DynamicProperties metrics = new DynamicPropertiesContainer();
    private DynamicProperties metricValue = new DynamicPropertiesContainer();
   
    {
        //properties = new LinkedHashMap<Metric, MetricValue>();        
    }

    public void addProperty(Metric metric, MetricValue metricValue) {
    	this.metrics.setProperty(metric.getName(), metric.toString());
    	this.metricValue.setProperty(metric.getName(), metricValue.toString());
//    	properties.put(metric, metricValue);
    }

    public void removeProperty(Metric metric) {
    	this.metrics.removeProperty(metric.getName());
    	this.metricValue.removeProperty(metric.getName());
//        properties.remove(metric);
    }

//    public void setProperties(Map<Metric, MetricValue> properties) {
//        this.properties = properties;
//    }
    
    public Resource() {
    }

    public Resource(String name) {
        super(name);
    }

    public Map<Metric, MetricValue> getProperties() {
    	Map<Metric, MetricValue> theMap = new HashMap<Metric, MetricValue>();
    	for (Map.Entry<String, Object> entry : this.metrics.asMap().entrySet()) {
    		String key = entry.getKey();
    	    theMap.put(Metric.fromString((String)this.metrics.getProperty(key)), MetricValue.fromString((String)this.metricValue.getProperty(key)));  
		}
    	return theMap;
//        return properties;
    }

//    public ResourceRelationship clone() {
//        Link clone = super.clone();
//        ResourceRelationship newResource = new ResourceRelationship(name);
//        newResource.setId(clone.id);
//        this.metrics.createFrom(this.metrics.asMap());
//        this.metricValue.createFrom(this.metricValue.asMap());
////        for (Map.Entry<Metric, MetricValue> e : properties.entrySet()) {
////            //currently this is NOT cloned. if needed, can be easily also clone (both are cloneable)
////            newResource.addProperty(e.getKey(), e.getValue());
////        }
//
//        return newResource;
//    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Resource) {
            Resource their = (Resource) obj;
            Map<Metric, MetricValue> theirProperties = their.getProperties();
            Map<Metric, MetricValue> properties = this.getProperties();

//            if they have different number of properties then they are not equal
            if (properties.keySet().size() != theirProperties.keySet().size()) {
                return false;
            } else {
                //else check that all values are equal
                for (Metric metric : properties.keySet()) {
                    if (theirProperties.containsKey(metric)) {                    	
                    	MetricValue theirMetricValue = theirProperties.get(metric);
                        return theirMetricValue.equals(properties.get(metric.getName()));
                    } else {
                        //if they have different properties
                        return false;
                    }
                }
            }
        } else {
            return false;
        }
        return super.equals(obj);
    }

	@Override
	public String toString() {
		String str="";
		Map<Metric, MetricValue> map =  getProperties();
		for (Map.Entry<Metric, MetricValue> entry : map.entrySet()) {
		    //str += "Key = " + entry.getKey().getName() + "(" +entry.getKey().getMeasurementUnit() +")" + ", Value = " + entry.getValue().getValue() + " \n ";
		    str += entry.getKey().toString()+ ":" + entry.getValue().toString() +",";
		}
		return "--"+this.name+"--"+"{" + str + "}";
	}

	
    
}