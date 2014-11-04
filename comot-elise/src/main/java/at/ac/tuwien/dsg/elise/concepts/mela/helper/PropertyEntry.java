/**
 * Copyright 2013 Technische Universitaet Wien (TUW), 
 * Distributed Systems Group E184
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

package at.ac.tuwien.dsg.elise.concepts.mela.helper;

import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.MetricValue;

/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
public class PropertyEntry {

    private Metric metric;

    private MetricValue value;

    public PropertyEntry() {
    }

    public PropertyEntry(Metric metric, MetricValue value) {
        this.metric = metric;
        this.value = value;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public MetricValue getValue() {
        return value;
    }

    public void setValue(MetricValue value) {
        this.value = value;
    }

    
}