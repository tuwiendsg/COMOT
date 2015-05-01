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
package at.ac.tuwien.dsg.comot.m.core.test.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.util.Random;

public class LoadGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(LoadGenerator.class);

	protected ExecutorService scheduler;

	public static void main(String[] args) {

		LoadGenerator generator = new LoadGenerator();
		generator.startLoadTunel();

	}

	public void startLoadTunel() {
		startLoad("localhost", 9090);
	}

	public void startLoad(String host, int port) {

		int rowsToCreate = 30;
		String keyspaceName = "m2m";
		String tablename = "sensor";
		final String uri = "http://" + host + ":" + port + "/DaaS/api/xml/table/row";

		scheduler = Executors.newFixedThreadPool(100);
		final String body = createBody(tablename, rowsToCreate, keyspaceName);

		System.out.println(body);

		for (int i = 0; i < 20; i++) {

			final int id = i;

			scheduler.execute(new Runnable() {

				Client client = ClientBuilder.newClient();

				@Override
				public void run() {

					try {
						while (true) {

							Response response = client.target(uri)
									.request(MediaType.WILDCARD_TYPE)
									.put(Entity.xml(body));

							String result = response.readEntity(String.class);
							int status = response.getStatus();

						}
					} catch (Exception e) {
						LOG.error("{}", e);
					}
				}

			});
		}

	}

	public void stop() {
		scheduler.shutdownNow();
	}

	public String createBody(String table, int rowsToCreate, String keyspaceName) {

		StringBuilder createRowStatement = new StringBuilder(
				"<CreateRowsStatement><Table name=\"" + table + "\"><Keyspace name=\"" + keyspaceName + "\"/></Table>");
		UUID key;
		double finalX;

		DecimalFormat df = new DecimalFormat("#.######");
		Random rand = new Random();
		double minX = 1.0;
		double maxX = 20000.0;

		for (int i = 0; i < rowsToCreate; i++) {
			key = UUID.randomUUID();
			finalX = rand.nextDouble() * (maxX - minX) + minX;

			createRowStatement.append("<Row><Column name=\"key\" value=\"" + key
					+ "\"/><Column name=\"sensorName\" value=\"SensorY\"/><Column name=\"sensorValue\" value=\""
					+ df.format(finalX) + "\"/> </Row>");

		}
		createRowStatement.append("</CreateRowsStatement>");

		return createRowStatement.toString();
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
