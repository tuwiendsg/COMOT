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

package at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.helper;

import at.ac.tuwien.dsg.ElasticityInformationService.concepts.mela.monitoringConcepts.MetricValue;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @Author Daniel Moldovan
 * @E-mail: d.moldovan@dsg.tuwien.ac.at
 *
 */
public class CostIntervalMapAdapter extends XmlAdapter<CostIntervalEntries, Map<MetricValue, Double>> {

    @Override
    public Map<MetricValue, Double> unmarshal(CostIntervalEntries in) throws Exception {
        HashMap<MetricValue, Double > hashMap = new HashMap<MetricValue, Double>();
        for (CostIntervalEntry entry : in.entries()) {
            hashMap.put(entry.getInterval(), entry.getValue());
        }
        return hashMap;
    }

    @Override
    public CostIntervalEntries marshal(Map<MetricValue, Double> map) throws Exception {
        CostIntervalEntries props = new CostIntervalEntries();
        for (Map.Entry<MetricValue,Double> entry : map.entrySet()) {
            props.addEntry(new CostIntervalEntry(entry.getKey(), entry.getValue()));
        }
        return props;
    }

}
