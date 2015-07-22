/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
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
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.recorder;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.m.common.ConfigConstants;

@Configuration
@Profile(AppContextServrec.EMBEDDED_NEO4J_DB)
public class AppContextServrecEmbeddedDb {

	private static final Logger LOG = LoggerFactory.getLogger(AppContextServrecEmbeddedDb.class);

	@Autowired
	protected Environment env;

	@Bean(destroyMethod = "shutdown")
	public GraphDatabaseService graphDatabaseService() {
		GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(
				env.getProperty(ConfigConstants.RECORDER));
		LOG.info("Neo4jDB: {}", db);
		return db;
	}

}
