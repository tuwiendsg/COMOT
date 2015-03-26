package at.ac.tuwien.dsg.comot.m.common;

import java.net.URI;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;

public class InformationClientRest extends ServiceClient {

	public InformationClientRest(URI baseUri) {
		super("INFO_SERVICE", baseUri);
	}

	public InformationClientRest() {
		super();
	}

	// SERVICE

	public String createService(CloudService service) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.SERVICES)
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.xml(service));
		processResponseStatus(response);
		String result = response.readEntity(String.class);

		return result;
	}

	public List<CloudService> getServices() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.SERVICES)
				.request(MediaType.APPLICATION_XML)
				.get();
		processResponseStatus(response);
		final GenericType<List<CloudService>> list = new GenericType<List<CloudService>>() {
		};

		List<CloudService> result = response.readEntity(list);

		return result;
	}

	// public CloudService getService(String serviceId) {
	//
	// Response response = client.target(baseUri)
	// .path(SERVICE_ONE)
	// .resolveTemplate("serviceId", serviceId)
	// .request(MediaType.APPLICATION_XML)
	// .get();
	//
	// CloudService result = response.readEntity(CloudService.class);
	//
	// return result;
	// }

	// SERVICE INSTANCE

	public String createServiceInstance(String serviceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.INSTANCES)
				.resolveTemplate("serviceId", serviceId)
				.request(MediaType.TEXT_PLAIN)
				.post(Entity.text(""));

		processResponseStatus(response);
		String result = response.readEntity(String.class);

		return result;
	}

	public void removeServiceInstance(String serviceId, String instanceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.INSTANCE_ONE)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("instanceId", instanceId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	public CloudService getServiceInstance(String instanceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.INSTANCE_ONE)
				.resolveTemplate("serviceId", "ANY")
				.resolveTemplate("instanceId", instanceId)
				.request(MediaType.WILDCARD)
				.get();

		processResponseStatus(response);
		CloudService result = response.readEntity(CloudService.class);

		return result;
	}

	// UNIT INSTANCES

	public void putUnitInstance(String serviceId, String instanceId, String unitId, UnitInstance uInst)
			throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.UNIT_INSTANCE_ONE)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("instanceId", instanceId)
				.resolveTemplate("unitId", unitId)
				.resolveTemplate("unitInstanceId", uInst.getId())
				.request(MediaType.WILDCARD)
				.put(Entity.xml(uInst));

		processResponseStatus(response);
		// CloudService result = response.readEntity(CloudService.class);

	}

	public void removeUnitInstance(String serviceId, String instanceId, String uInstId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.UNIT_INSTANCE_ONE)
				.resolveTemplate("serviceId", serviceId)
				.resolveTemplate("instanceId", instanceId)
				.resolveTemplate("unitId", "ANY")
				.resolveTemplate("unitInstanceId", uInstId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	// OSU

	public List<OfferedServiceUnit> getOsus() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPSES)
				.request(MediaType.APPLICATION_XML)
				.get();

		processResponseStatus(response);
		final GenericType<List<OfferedServiceUnit>> list = new GenericType<List<OfferedServiceUnit>>() {
		};

		List<OfferedServiceUnit> result = response.readEntity(list);

		return result;
	}

	public void addOsu(OfferedServiceUnit osu) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPSES)
				.request(MediaType.WILDCARD)
				.post(Entity.xml(osu));

		processResponseStatus(response);
	}

	public void assignEps(String instanceId, String osuInstanceId) throws EpsException {

		log.info("assignEps({} {})", instanceId, osuInstanceId);

		Response response = client.target(baseUri)
				.path(Constants.EPS_ASSIGNMENT)
				.resolveTemplate("serviceId", "ANY")
				.resolveTemplate("instanceId", instanceId)
				.resolveTemplate("epsId", osuInstanceId)
				.request(MediaType.WILDCARD)
				.put(Entity.text(""));

		processResponseStatus(response);
	}

	public void removeEpsAssignment(String instanceId, String osuInstanceId) throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.EPS_ASSIGNMENT)
				.resolveTemplate("serviceId", "ANY")
				.resolveTemplate("instanceId", instanceId)
				.resolveTemplate("epsId", osuInstanceId)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

	public void deeteAll() throws EpsException {

		Response response = client.target(baseUri)
				.path(Constants.DELETE_ALL)
				.request(MediaType.WILDCARD)
				.delete();

		processResponseStatus(response);
	}

}
