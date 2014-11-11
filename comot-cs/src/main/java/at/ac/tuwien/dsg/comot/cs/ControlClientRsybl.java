package at.ac.tuwien.dsg.comot.cs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.exception.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.node.Capability;
import at.ac.tuwien.dsg.comot.common.model.node.Requirement;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.structure.StackNode;
import at.ac.tuwien.dsg.comot.common.model.type.NodeType;
import at.ac.tuwien.dsg.comot.cs.connector.RsyblClient;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.RelationshipXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.SYBLAnnotationXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.ServiceTopologyXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.ServiceUnitXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;

public class ControlClientRsybl implements ControlClient {

	private final Logger log = LoggerFactory.getLogger(ControlClientRsybl.class);

	protected RsyblClient rsybl;

	public ControlClientRsybl() {
		rsybl = new RsyblClient();
	}

	@Override
	public void sendInitialConfig(
			CloudService service,
			DeploymentDescription deploymentDescription,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		String serviceId = service.getId();
		CloudServiceXML cloudServiceXML = toRSYBLRepresentation(service);
		deploymentDescription = enrichWithElasticityCapabilities(deploymentDescription, service);

		rsybl.prepareControl(serviceId);

		rsybl.serviceDescription(serviceId, cloudServiceXML);

		rsybl.serviceDeployment(serviceId, deploymentDescription);

		if (compositionRulesConfiguration != null) {// optional
			rsybl.sendMetricsCompositionRules(serviceId, compositionRulesConfiguration);
		}

		if (effectsJSON != null) {// optional
			rsybl.sendElasticityCapabilitiesEffects(serviceId, effectsJSON);
		}

	}

	@Override
	public void sendUpdatedConfig(
			CloudService service,
			CompositionRulesConfiguration compositionRulesConfiguration,
			String effectsJSON) throws CoreServiceException {

		String serviceId = service.getId();
		CloudServiceXML cloudServiceXML = toRSYBLRepresentation(service);

		rsybl.updateMetricsCompositionRules(serviceId, compositionRulesConfiguration);

		rsybl.updateElasticityCapabilitiesEffects(serviceId, effectsJSON);

		rsybl.updateElasticityRequirements(serviceId, cloudServiceXML);

	}

	@Override
	public void startControl(String serviceId) throws CoreServiceException {
		rsybl.startControl(serviceId);
	}

	@Override
	public void stopControl(String serviceId) throws CoreServiceException {
		rsybl.stopControl(serviceId);
	}

	// ////////////

	protected CloudServiceXML toRSYBLRepresentation(CloudService serviceTemplate) {
		CloudServiceXML cloudServiceXML = new CloudServiceXML();
		cloudServiceXML.setId(serviceTemplate.getId());

		// Capability ID
		Map<String, StackNode> capabilitiesPerUnit = new HashMap<>();
		Map<String, StackNode> requirementsPerUnit = new HashMap<>();

		Map<String, String> toFromRelationships = new HashMap<>();

		// build map connecting capabilities to units
		for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {
			for (StackNode serviceUnit : serviceTopology.getServiceUnits()) {
				for (Capability c : serviceUnit.getCapabilities()) {
					capabilitiesPerUnit.put(c.getId(), serviceUnit);
				}

				for (Requirement requirement : serviceUnit.getRequirements()) {
					requirementsPerUnit.put(requirement.getId(), serviceUnit);
				}
			}
		}

		List<EntityRelationship> entityRelationships = serviceTemplate.getRelationships();

		for (EntityRelationship entityRelationship : entityRelationships) {
			if (entityRelationship.getFrom() instanceof Capability
					&& entityRelationship.getTo() instanceof Requirement) {
				Capability from = (Capability) entityRelationship.getFrom();
				Requirement to = (Requirement) entityRelationship.getTo();

				StackNode fromUnit = capabilitiesPerUnit.get(from.getId());
				StackNode toUnit = requirementsPerUnit.get(to.getId());
				if (fromUnit != null && toUnit != null) {
					toFromRelationships.put(fromUnit.getId(), toUnit.getId());
				} else {
					log.warn("Relationship " + entityRelationship + " has no capabilities/requirements");
				}
			}
		}

		List<ServiceTopologyXML> serviceTopologyXMLs = new ArrayList<>();
		cloudServiceXML.setServiceTopologies(serviceTopologyXMLs);

		for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {

			ServiceTopologyXML serviceTopologyXML = new ServiceTopologyXML();
			serviceTopologyXML.setId(serviceTopology.getId());

			serviceTopologyXMLs.add(serviceTopologyXML);

			List<ServiceUnitXML> serviceUnitXMLs = new ArrayList<>();
			serviceTopologyXML.setServiceUnits(serviceUnitXMLs);

			for (StackNode serviceUnit : serviceTopology.getServiceUnits()) {
				if (!serviceUnit.getType().equals(NodeType.SOFTWARE)) {
					// only gather Software type nodes
					continue;
				}
				ServiceUnitXML serviceUnitXML = new ServiceUnitXML();
				String serviceUnitID = serviceUnit.getId();
				serviceUnitXML.setId(serviceUnitID);

				serviceUnitXMLs.add(serviceUnitXML);

				if (toFromRelationships.containsKey(serviceUnitID)) {
					RelationshipXML relationshipXML = new RelationshipXML();
					relationshipXML.setSource(serviceUnitID);
					relationshipXML.setTarget(toFromRelationships.get(serviceUnitID));
					relationshipXML.setType(serviceUnitID);

					serviceTopologyXML.addRelationship(relationshipXML);
				}

				if (serviceUnit.get) {
					SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();
					annotationXML.setConstraints(); //
					serviceUnitXML.setXMLAnnotation(annotationXML);
				}
				if (serviceUnit.hasStrategies()) {
					SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();
					strategies = // put all strategies into one annotation
					annotationXML.setStrategies(strategies);
					serviceUnitXML.setXMLAnnotation(annotationXML);
				}
			}

			if (serviceTopology.hasConstraints()) {
				//
				serviceTopologyXML.setXMLAnnotation(annotationXML);
			}
			if (serviceTopology.hasStrategies()) {
				//
				serviceTopologyXML.setXMLAnnotation(annotationXML);
			}
		}

		return cloudServiceXML;

	}

	/**
	 * With side effects, Directly enriches supplied deploymentDescription
	 *
	 * @param deploymentDescription
	 * @param serviceTemplate
	 * @return
	 */
	protected DeploymentDescription enrichWithElasticityCapabilities(DeploymentDescription deploymentDescription,
			CloudService serviceTemplate) {
		// get a Map of Deployment Units and a map of SoftwareUnits, and match capabilities
		Map<String, StackNode> units = new HashMap<>();

		for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {
			for (StackNode serviceUnit : serviceTopology.getServiceUnits()) {
				units.put(serviceUnit.getId(), serviceUnit);
			}
		}

		for (DeploymentUnit deploymentUnit : deploymentDescription.getDeployments()) {
			if (units.containsKey(deploymentUnit.getServiceUnitID())) {
				Set<ElasticityCapability> capabilities = units.get(deploymentUnit.getServiceUnitID())
						.getElasticityCapabilities();
				for (ElasticityCapability capability : capabilities) {
					at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.ElasticityCapability ec = new at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.ElasticityCapability();
					ec.setType(capability.getType());
					ec.setScript(capability.getEndpoint());
					deploymentUnit.getElasticityCapabilities().add(ec);
				}
			}
		}

		return deploymentDescription;

	}

	@PreDestroy
	public void cleanup() {
		log.info("closing rsybl client");
		rsybl.close();
	}

	@Override
	public void setHost(String host) {
		rsybl.setHost(host);
	}

	@Override
	public void setPort(int port) {
		rsybl.setPort(port);
	}

}
