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
package at.ac.tuwien.dsg.comot.m.cs.connector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.m.common.ServiceClient;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshots;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class MelaClient extends ServiceClient {

	private static final Logger LOG = LoggerFactory.getLogger(MelaClient.class);

	protected static final String DEF_BASE_PATH = "http://127.0.0.1:8180/MELA/REST_WS";

	protected static final String SERVICE_CREATE_PATH = "service";
	protected static final String SERVICE_PATH = "{serviceId}/structure";
	protected static final String SERVICE_DELETE_PATH = "{serviceId}";
	protected static final String LIST_ALL_SERVICES_PATH = "elasticservices";
	protected static final String CREAT_MCR_PATH = "{serviceId}/metricscompositionrules";
	protected static final String GET_MCR_PATH = "{serviceId}/metriccompositionrules/xml";
	protected static final String REQUIREMENTS_PATH = "{serviceId}/requirements";
	protected static final String DATA_PATH = "{serviceId}/monitoringdata/xml";
	protected static final String GET_DATA_ALL_PATH = "{serviceId}/historicalmonitoringdata/all/xml";
	protected static final String GET_DATA_INTERVAL_PATH = "{serviceId}/historicalmonitoringdata/ininterval/xml";
	protected static final String GET_DATA_LASTX_PATH = "{serviceId}/historicalmonitoringdata/lastX/xml";

	public MelaClient() throws URISyntaxException {
		this(new URI(DEF_BASE_PATH));
	}

	public MelaClient(URI baseUri) {
		super("MELA", baseUri);
	}

	// SERVICE STRUCTURE DESCRIPTION --------------------------------------------------

	// create service
	public void sendServiceDescription(MonitoredElement element) throws EpsException {

		try {
			LOG.debug("MonitoredElement: {}", UtilsCs.asString(element));
		} catch (JAXBException e) {
			LOG.error("{}", e);
		}

		Response response = client.target(getBaseUri())
				.path(SERVICE_CREATE_PATH)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(element));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "createServiceDescription. Response: '{}'", result);

	}

	// get
	public MonitoredElement getServiceDescription(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElement result = response.readEntity(MonitoredElement.class);

		LOG.info(ln + "getServiceDescription '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	// update structure
	public void updateServiceDescription(String serviceId, MonitoredElement element) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.xml(element));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "updateServiceDescription. Response: '{}'", result);

	}

	// remove
	public void removeServiceDescription(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_DELETE_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.delete();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "removeServiceDescription '{}'. Response: '{}'", serviceId, result);

	}

	// list all
	public List<String> listAllServices() throws EpsException {

		Response response = client.target(getBaseUri())
				.path(LIST_ALL_SERVICES_PATH)
				.request(MediaType.APPLICATION_JSON)
				.get();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "listAllServices . Response: '{}'", result);

		// extract IDs from JSON to list
		List<String> list = new ArrayList<String>();
		JSONArray array = new JSONArray(result);

		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			list.add(obj.getString("id"));
		}

		return list;
	}

	// METRIC COMPOSITION RULES --------------------------------------------------

	// create MCR
	public void sendMetricsCompositionRules(String serviceId, CompositionRulesConfiguration rules)
			throws EpsException {

		Response response = client.target(getBaseUri())
				.path(CREAT_MCR_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(rules));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "sendMetricsCompositionRules '{}'. Response: '{}'", serviceId, result);

	}

	// get
	public CompositionRulesConfiguration getMetricsCompositionRules(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(GET_MCR_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		CompositionRulesConfiguration result = response.readEntity(CompositionRulesConfiguration.class);

		LOG.info(ln + "getMetricsCompositionRules '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	// REQUIREMENTS --------------------------------------------------

	// create requirements
	public void sendRequirements(String serviceId, Requirements requirements) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(REQUIREMENTS_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(requirements));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "sendRequirements '{}'. Response: '{}'", serviceId, result);

	}

	// MONITORING DATA --------------------------------------------------

	// get monitoring data
	public MonitoredElementMonitoringSnapshot getMonitoringData(String serviceId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(DATA_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshot result = response.readEntity(MonitoredElementMonitoringSnapshot.class);

		LOG.info(ln + "getMonitoringData '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	public MonitoredElementMonitoringSnapshot getMonitoringData(String serviceId, MonitoredElement element)
			throws EpsException {

		Response response = client.target(getBaseUri())
				.path(DATA_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.post(Entity.xml(element));

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshot result = response.readEntity(MonitoredElementMonitoringSnapshot.class);

		LOG.info(ln + "getMonitoringData '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	public MonitoredElementMonitoringSnapshots getAllAggregatedMonitoringData(String serviceId)
			throws EpsException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_ALL_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshots result = response.readEntity(MonitoredElementMonitoringSnapshots.class);

		LOG.info(ln + "getAllAggregatedMonitoringData '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	/**
	 * Ad-hoc functionality
	 */
	@Deprecated
	public MonitoredElementMonitoringSnapshots getAllAggregatedMonitoringDataInTimeInterval(
			String serviceId,
			int startTimestampId,
			int endTimestampId) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_INTERVAL_PATH)
				.resolveTemplate("serviceId", serviceId)
				.queryParam("startTimestamp", startTimestampId)
				.queryParam("endTimestamp", endTimestampId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshots result = response.readEntity(MonitoredElementMonitoringSnapshots.class);

		LOG.info(ln + "getAllAggregatedMonitoringDataInTimeInterval '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	public MonitoredElementMonitoringSnapshots getLastXAggregatedMonitoringData(
			String serviceId,
			int count) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_LASTX_PATH)
				.resolveTemplate("serviceId", serviceId)
				.queryParam("count", count)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshots result = response.readEntity(MonitoredElementMonitoringSnapshots.class);

		LOG.info(ln + "getLastXAggregatedMonitoringData '{}'. Response: '{}'", serviceId, result);

		return result;
	}

	// METRICS --------------------------------------------------

	// getAvailableMetrics
	// returns Collection<Metric>
	public String getAvailableMetrics(
			String serviceId,
			String monitoredElementID) throws EpsException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_LASTX_PATH)
				.resolveTemplate("serviceId", serviceId)
				.queryParam("monitoredElementID", monitoredElementID)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		LOG.info(ln + "getAvailableMetrics '{}'. Response: '{}'", serviceId, result);

		return result;
	}

}
