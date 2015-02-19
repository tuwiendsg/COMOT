/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.common.model;

import at.ac.tuwien.dsg.comot.common.model.Constraint.Metric;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class MetricEffect {

    public enum Type {

        DIV("/"),
        ADD("+"),
        SUB("-"),
        MUL("*");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    private Metric metric;
    private Type effectType;
    private Double effectValue;

    public static MetricEffect MetricEffect() {
        return new MetricEffect();
    }

    public MetricEffect withMetric(final Metric metric) {
        this.metric = metric;
        return this;
    }

    public MetricEffect withType(final Type effectType) {
        this.effectType = effectType;
        return this;
    }

    public MetricEffect withValue(final Double effectValue) {
        this.effectValue = effectValue;
        return this;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Type getEffectType() {
        return effectType;
    }

    public void setEffectType(Type effectType) {
        this.effectType = effectType;
    }

    public Double getEffectValue() {
        return effectValue;
    }

    public void setEffectValue(Double effectValue) {
        this.effectValue = effectValue;
    }

}
