/**
 * Copyright 2013 Technische Universitat Wien (TUW), Distributed Systems Group
 * E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
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
package at.ac.tuwien.dsg.comot.visualisation.service.api;

import at.ac.tuwien.dsg.comot.visualisation.service.config.ComotUIModule;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import org.json.simple.JSONArray;
import org.springframework.context.ApplicationContext;

/**
 * Author: Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 *
 * <p/>
 * Delegates the functionality of configuring MELA for instant monitoring and
 * analysis
 */
@Service
public class ComotVisControl {

    @Autowired
    private ApplicationContext context;

    static final Logger LOG = LoggerFactory.getLogger(ComotVisControl.class);

    private List<ComotUIModule> modules = new ArrayList<ComotUIModule>();

    @PostConstruct
    public void init() {
        Map<String, ComotUIModule> comotUIModulesDefined = context.getBeansOfType(ComotUIModule.class);

        for (String module : comotUIModulesDefined.keySet()) {
            ComotUIModule comotUIModule = comotUIModulesDefined.get(module);
            LOG.debug("Found COMOT UI Module '{}': {}", module, comotUIModule);
            modules.add(comotUIModule);
        }
    }

    public String getAllModules() {

        JSONArray array = new JSONArray();

        for (ComotUIModule comotUIModule : modules) {
            JSONObject o = new JSONObject();
            o.put("name", comotUIModule.getName());
            o.put("url", comotUIModule.getUrl());
            array.add(o);
        }
        return array.toJSONString();
    }
}
