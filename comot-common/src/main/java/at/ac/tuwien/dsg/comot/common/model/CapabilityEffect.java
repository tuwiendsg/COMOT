/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.common.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class CapabilityEffect {

    private List<MetricEffect> metricEffects;

    {
        metricEffects = new ArrayList<>();
    }

    public static CapabilityEffect CapabilityEffect() {
        return new CapabilityEffect();
    }

    public static CapabilityEffect CapabilityEffect(AbstractCloudEntity abstractCloudEntity) {
        return new CapabilityEffect().withTarget(abstractCloudEntity);
    }

    private AbstractCloudEntity target;

    public AbstractCloudEntity getTarget() {
        return target;
    }

    public void setTarget(AbstractCloudEntity target) {
        this.target = target;
    }

    public CapabilityEffect withTarget(final AbstractCloudEntity target) {
        this.target = target;
        return this;
    }

    public List<MetricEffect> getMetricEffects() {
        return metricEffects;
    }

    public void setMetricEffects(List<MetricEffect> metricEffects) {
        this.metricEffects = metricEffects;
    }

    public CapabilityEffect withMetricEffects(final List<MetricEffect> metricEffects) {
        this.metricEffects = metricEffects;
        return this;
    }

    public CapabilityEffect withMetricEffect(MetricEffect... metricEffect) {
        this.metricEffects.addAll(Arrays.asList(metricEffect));
        return this;
    }

    public CapabilityEffect withMetricEffects(MetricEffect metricEffect) {
        this.metricEffects.add(metricEffect);
        return this;
    }

}
