package at.ac.tuwien.dsg.comot.cs.connector;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshot;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElementMonitoringSnapshots;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

public class MelaClient extends CoreServiceClient {

	private static final Logger log = LoggerFactory.getLogger(MelaClient.class);

	protected static final String DEF_BASE_PATH = "/MELA/REST_WS"; // TODO

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

	public MelaClient() {
		this(DEF_HOST, DEF_PORT);
	}

	public MelaClient(String host) {
		this(host, DEF_PORT, DEF_BASE_PATH);
	}

	public MelaClient(String host, int port) {
		this(host, port, DEF_BASE_PATH);
	}

	public MelaClient(String host, int port, String basePath) {
		super(host, port, basePath);
	}

	// SERVICE STRUCTURE DESCRIPTION --------------------------------------------------

	// create service
	public void sendServiceDescription(MonitoredElement element) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_CREATE_PATH)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(element));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "createServiceDescription. Response: '{}'",
					result);
		}
	}

	// get
	public MonitoredElement getServiceDescription(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElement result = response.readEntity(MonitoredElement.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getServiceDescription '{}'. Response: '{}'", serviceId,
					result);
		}

		return result;
	}

	// update structure
	public void updateServiceDescription(String serviceId, MonitoredElement element) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.post(Entity.xml(element));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "updateServiceDescription. Response: '{}'",
					result);
		}

	}

	// remove
	public void removeServiceDescription(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(SERVICE_DELETE_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.delete();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "removeServiceDescription '{}'. Response: '{}'", serviceId,
					result);
		}

	}

	// list all
	public List<String> listAllServices() throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(LIST_ALL_SERVICES_PATH)
				.request(MediaType.APPLICATION_JSON)
				// .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "listAllServices . Response: '{}'", result);
		}

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
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(CREAT_MCR_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(rules));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "sendMetricsCompositionRules '{}'. Response: '{}'", serviceId,
					result);
		}

	}

	// get
	public CompositionRulesConfiguration getMetricsCompositionRules(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(GET_MCR_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		CompositionRulesConfiguration result = response.readEntity(CompositionRulesConfiguration.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getMetricsCompositionRules '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

	// REQUIREMENTS --------------------------------------------------

	// create requirements
	public void sendRequirements(String serviceId, Requirements requirements) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(REQUIREMENTS_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.WILDCARD_TYPE)
				.put(Entity.xml(requirements));

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "sendRequirements '{}'. Response: '{}'", serviceId,
					result);
		}

	}

	// MONITORING DATA --------------------------------------------------

	// get monitoring data
	public MonitoredElementMonitoringSnapshot getMonitoringData(String serviceId) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(DATA_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshot result = response.readEntity(MonitoredElementMonitoringSnapshot.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getMonitoringData '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

	public MonitoredElementMonitoringSnapshot getMonitoringData(String serviceId, MonitoredElement element)
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(DATA_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.post(Entity.xml(element));

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshot result = response.readEntity(MonitoredElementMonitoringSnapshot.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getMonitoringData '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

	public MonitoredElementMonitoringSnapshots getAllAggregatedMonitoringData(String serviceId)
			throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_ALL_PATH)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshots result = response.readEntity(MonitoredElementMonitoringSnapshots.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getAllAggregatedMonitoringData '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

	public MonitoredElementMonitoringSnapshots getAllAggregatedMonitoringDataInTimeInterval(
			String serviceId,
			int startTimestamp,
			int endTimestamp) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_INTERVAL_PATH)
				.resolveTemplate("serviceId", serviceId)
				.queryParam("startTimestamp", startTimestamp)
				.queryParam("endTimestamp", endTimestamp)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshots result = response.readEntity(MonitoredElementMonitoringSnapshots.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getAllAggregatedMonitoringDataInTimeInterval '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

	public MonitoredElementMonitoringSnapshots getLastXAggregatedMonitoringData(
			String serviceId,
			int count) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_LASTX_PATH)
				.resolveTemplate("serviceId", serviceId)
				.queryParam("count", count)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		MonitoredElementMonitoringSnapshots result = response.readEntity(MonitoredElementMonitoringSnapshots.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getLastXAggregatedMonitoringData '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

	// METRICS --------------------------------------------------

	// getAvailableMetrics
	// returns Collection<Metric>
	public String getAvailableMetrics(
			String serviceId,
			String monitoredElementID) throws CoreServiceException {

		Response response = client.target(getBaseUri())
				.path(GET_DATA_LASTX_PATH)
				.resolveTemplate("serviceId", serviceId)
				.queryParam("monitoredElementID", monitoredElementID)
				.request(MediaType.APPLICATION_XML)
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);

		String result = response.readEntity(String.class);

		if (log.isInfoEnabled()) {
			log.info(Markers.CLIENT, "getAvailableMetrics '{}'. Response: '{}'", serviceId,
					result);
		}
		return result;
	}

}
