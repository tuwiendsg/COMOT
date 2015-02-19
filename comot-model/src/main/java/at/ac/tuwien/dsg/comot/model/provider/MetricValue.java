/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.model.provider;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

/**
 *
 * @author hungld
 */
@NodeEntity
public class MetricValue extends Entity{

    protected Object value;
    protected ValueType valueType = ValueType.UNKNOWN;

    public enum ValueType {

        TEXT, NUMERIC, ENUM, UNKNOWN
    }

    public MetricValue() {
    }

    public MetricValue(String name, Object withValue, ValueType ofType) {
        if (withValue != null) {
            this.value = withValue.toString();
        } else {
            this.value = "undefined";
        }
        this.name = name;
        this.valueType = ofType;
    }

    public Object getValue() {
        return value;
    }

    public ValueType getValueType() {
        return valueType;
    }
    
}
