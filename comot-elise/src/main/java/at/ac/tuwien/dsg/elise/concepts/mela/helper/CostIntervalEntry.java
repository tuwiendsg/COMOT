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

import at.ac.tuwien.dsg.elise.concepts.mela.monitoringConcepts.MetricValue;

/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */

public class CostIntervalEntry {


    private MetricValue interval;
    
    private Double value;

    public CostIntervalEntry() {
    }

    public CostIntervalEntry(MetricValue interval, Double value) {
        this.interval = interval;
        this.value = value;
    }

    public MetricValue getInterval() {
        return interval;
    }

    public void setInterval(MetricValue interval) {
        this.interval = interval;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    
 
    
}
