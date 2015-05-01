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
package at.ac.tuwien.dsg.comot.m.recorder.test;

import java.util.Iterator;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestBean {

	private static final Logger LOG = LoggerFactory.getLogger(TestBean.class);

	@Autowired
	protected GraphDatabaseService db;
	@Autowired
	protected Neo4jOperations neo;
	@Autowired
	protected ExecutionEngine engine;

	@Transactional
	public void test() {

		Iterator<Relationship> iter = engine.execute("match (n)-[r:connectTo]->(m) return r").columnAs("r");
		for (Relationship rel : IteratorUtil.asIterable(iter)) {
			for (String prop : rel.getPropertyKeys()) {
				LOG.info("{}: {}={}", rel, prop, rel.getProperty(prop));
			}
		}
	}

	@Transactional
	public Long countLabel(String label) {

		Iterator<Long> iter = engine.execute("match (n:" + label + ") return count(n) as m").columnAs("m");
		for (Long ll : IteratorUtil.asIterable(iter)) {
			return ll;
		}
		return null;
	}

}
