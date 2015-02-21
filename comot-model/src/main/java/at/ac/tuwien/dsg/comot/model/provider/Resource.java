/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

/**
 *
 * @author hungld
 */
@NodeEntity
public class Resource extends Entity implements HasUniqueId {
	
    @Indexed(unique = true)
    @XmlAttribute
    String id;

    @Fetch
    protected ResourceOrQualityType type;

//    @RelatedTo(direction = Direction.INCOMING)
    protected String associatedEntityID;

    // e.g. Flavor has CPU, RAM   
    @RelatedTo(direction = Direction.OUTGOING)
    @Fetch
    protected Set<Resource> containsResources;

    @RelatedTo(direction = Direction.OUTGOING)
    @Fetch
    protected Set<Quality> containsQualities;

    // e.g. CPU has CPU number, CPU frequency, CPU type
//    @RelatedTo(direction = Direction.OUTGOING)
    @Fetch
    protected Set<MetricValue> hasMetric;

    {
        hasMetric = new HashSet<>();
    }

    public Resource() {
    }

    public Resource(String name, ResourceOrQualityType type) {
        super(name);
        this.name = name;
        this.type = type;
    }

    public boolean addMetric(MetricValue metricValue) {
        // check if metricvalue is consistant with type

        for (Metric m : type.getMetrics()) {
            System.out.print(m.getName() + ",");
        }

        if (type.getMetricByName(metricValue.getName()) != null) {

            this.hasMetric.add(metricValue);
            return true;
        }

        return false;
    }

    // fluent construction
    public Resource hasMetric(MetricValue metricValue) {
        this.addMetric(metricValue);
        return this;
    }

    public void hasResource(Resource resource) {
        if (containsResources == null) {
            containsResources = new HashSet<>();
        }
        this.containsResources.add(resource);
    }

    public void hasQuality(Quality quality) {
        if (containsQualities == null) {
            containsQualities = new HashSet<>();
        }
        this.containsQualities.add(quality);
    }

    public ResourceOrQualityType getType() {
        return type;
    }

    public Set<Resource> getContainsResources() {
        return containsResources;
    }

    public Set<Quality> getContainsQualities() {
        return containsQualities;
    }

    public Set<MetricValue> getHasMetric() {
        return hasMetric;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
