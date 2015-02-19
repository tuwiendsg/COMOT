/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import at.ac.tuwien.dsg.comot.model.HasUniqueId;
import java.util.HashSet;
import java.util.Set;
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
public class Quality extends Entity implements HasUniqueId {

    @Indexed(unique = true)
    String id;

//    @RelatedTo(direction = Direction.OUTGOING)
    @Fetch
    protected Set<MetricValue> hasMetricQuality;

    @Fetch
    protected ResourceOrQualityType type;

    public Quality() {
    }

    public Quality(String name, ResourceOrQualityType type) {
        super(name);
        this.type = type;
    }

    public void addMetric(MetricValue metricValue) {
        if (type.getMetricByName(metricValue.getName()) != null) {
            this.hasMetricQuality.add(metricValue);
        }
    }

    // fluent construction
    public Quality hasMetric(MetricValue metricValue) {
        if (this.hasMetricQuality == null) {
            this.hasMetricQuality = new HashSet<>();
        }
        this.addMetric(metricValue);
        return this;
    }

    public Set<MetricValue> getHasMetric() {
        return hasMetricQuality;
    }

    public ResourceOrQualityType getType() {
        return type;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
