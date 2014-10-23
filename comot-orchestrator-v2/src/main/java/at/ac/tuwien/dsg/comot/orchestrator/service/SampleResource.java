package at.ac.tuwien.dsg.comot.orchestrator.service;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.coreservices.DeploymentClient;

// http://localhost:8380/rest/service

@Service
@Path("/")
public class SampleResource {

	private static final Logger log = LoggerFactory.getLogger(SampleResource.class);

	@Autowired
	protected DeploymentClient deploy;

	@PostConstruct
	public void aaa() {
		log.info("REST resource created");
	}

	@POST
	@Path("/service")
	@Consumes(MediaType.APPLICATION_XML)
	public String deploy(String tosca) {

		log.info("input: " + tosca);

		try {
			deploy.deploy(tosca);
		} catch (CoreServiceException e) {
			e.printStackTrace();
		}

		return "ok";
	}

	@GET
	@Path("/service/{serviceId}")
	@Consumes(MediaType.WILDCARD)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public CloudService getStatusXml(@PathParam("serviceId") String serviceId) {

		log.info("input: " + serviceId);

		try {
			CloudService service = deploy.getStatus(serviceId);
			return service;

		} catch (CoreServiceException e) {
			e.printStackTrace();
		}
		return null;

	}


	// @GET
	// @Path("/cloudservice/json/compact/{serviceId}")
	// @Produces(MediaType.TEXT_PLAIN)
	// public String getServiceRuntimeJsonTreeCompact(@PathParam("serviceId") String serviceId) {
	//
	// CloudService service = deploy.getStatus(serviceId);
	//
	// ServiceJsonDataTree datatree = new ServiceJsonDataTree();
	// datatree.setId(service.getName());
	// datatree.setNodeType("CLOUD SERVICE");
	// datatree.setState(service.getState());
	//
	// List<ServiceTopology> topos = service.getComponentTopologyList();
	// for (ServiceTopology topo : topos) {
	// ServiceJsonDataTree topoNode = new ServiceJsonDataTree();
	// topoNode.setAbstract(true);
	// topoNode.setId(topo.getId());
	// topoNode.setNodeType("TOPOLOGY");
	// topoNode.addProperty("Number of service units", topo.getComponents().size() + "");
	// topoNode.setState(topo.getState());
	// datatree.addChild(topoNode);
	// List<ServiceUnit> components = topo.getComponentsByType(SalsaEntityType.OPERATING_SYSTEM);
	// for (ServiceUnit compo : components) {
	// ServiceJsonDataTree componode = new ServiceJsonDataTree();
	// componode.loadData(compo, -1, topo); // -1 will not check instance id
	// topoNode.addChild(componode);
	// }
	// }
	//
	// datatree.compactData(); // parent=null for root node
	// datatree.reduceLargeNumberOfInstances();
	// Gson json = new GsonBuilder().setPrettyPrinting().create();
	//
	// return json.toJson(datatree);
	//
	// return "";
	// }

}
