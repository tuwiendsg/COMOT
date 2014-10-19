package at.ac.tuwien.dsg.comot.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.client.stub.RsyblStub;
import at.ac.tuwien.dsg.comot.common.coreservices.ControlClient;
import at.ac.tuwien.dsg.comot.common.coreservices.CoreServiceException;
import at.ac.tuwien.dsg.comot.common.model.Capability;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.Constraint;
import at.ac.tuwien.dsg.comot.common.model.ElasticityCapability;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.Requirement;
import at.ac.tuwien.dsg.comot.common.model.ServiceTopology;
import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import at.ac.tuwien.dsg.comot.common.model.Strategy;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.CloudServiceXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.RelationshipXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.SYBLAnnotationXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.ServiceTopologyXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.ServiceUnitXML;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentUnit;
import at.ac.tuwien.dsg.mela.common.configuration.metricComposition.CompositionRulesConfiguration;


public class ControlClientRsybl implements ControlClient{

	private final Logger log = LoggerFactory.getLogger(ControlClientRsybl.class);

	protected RsyblStub rsybl;

	public ControlClientRsybl() {
		rsybl = new RsyblStub();
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

		if (compositionRulesConfiguration != null) {//optional
			rsybl.metricsCompositionRules(serviceId, compositionRulesConfiguration);
		}
		
		if (effectsJSON != null) {//optional
			rsybl.elasticityCapabilitiesEffects(serviceId, effectsJSON);
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
		Map<String, ServiceUnit> capabilitiesPerUnit = new HashMap<>();
		Map<String, ServiceUnit> requirementsPerUnit = new HashMap<>();

		Map<String, String> toFromRelationships = new HashMap<>();

		// build map connecting capabilities to units
		for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {
			for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
				for (Capability c : serviceUnit.getCapabilities()) {
					// if (capabilitiesPerUnit.containsKey(c.getId())) {
					// capabilitiesPerUnit.get(c.getId()).add(serviceUnit);
					// } else {
					// ArrayList<ServiceUnit> sus = new ArrayList<>();
					// sus.add(serviceUnit);
					capabilitiesPerUnit.put(c.getId(), serviceUnit);
					// }
				}

				for (Requirement requirement : serviceUnit.getRequirements()) {
					requirementsPerUnit.put(requirement.getId(), serviceUnit);
				}
			}
		}

		Set<EntityRelationship> entityRelationships = serviceTemplate.getRelationships();

		for (EntityRelationship entityRelationship : entityRelationships) {
			if (entityRelationship.getFrom() instanceof Capability
					&& entityRelationship.getTo() instanceof Requirement) {
				Capability from = (Capability) entityRelationship.getFrom();
				Requirement to = (Requirement) entityRelationship.getTo();

				ServiceUnit fromUnit = capabilitiesPerUnit.get(from.getId());
				ServiceUnit toUnit = requirementsPerUnit.get(to.getId());
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

			for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
				if (!serviceUnit.getType().equals(ServiceUnit.NodeType.Software.toString())) {
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

				if (serviceUnit.hasConstraints()) {

					String costraints = "";

					for (Constraint constraint : serviceUnit.getConstraints()) {

						costraints += constraint.getId() + ":CONSTRAINT " + constraint.getMetric().getName()
								+ " " + constraint.getOperator().toString() + " "
								+ constraint.getValue() + " " + constraint.getMetric().getUnit() + ";";

					}
					SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

					costraints = costraints.replaceAll("  ", " ");
					annotationXML.setConstraints(costraints.trim());

					serviceUnitXML.setXMLAnnotation(annotationXML);
				}

				if (serviceUnit.hasStrategies()) {
					String strategies = "";

					for (Strategy strategy : serviceUnit.getStrategies()) {

						String costraints = "";
						for (Constraint constraint : strategy.getConstraints()) {

							costraints += constraint.getMetric().getName() + " "
									+ constraint.getOperator().toString()
									+ " " + constraint.getValue() + " "
									+ constraint.getMetric().getUnit() + " " + strategy.getOperator().toString() + " ";
						}

						if (costraints.lastIndexOf("AND") > 0) {
							costraints = costraints.substring(0, costraints.lastIndexOf("AND")).trim();
						}

						costraints = costraints.trim();
						strategies = strategy.getId() + ":STRATEGY CASE " + costraints + ":"
								+ strategy.getAction().toString() + ";";

					}
					SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();
					strategies = strategies.replaceAll("  ", " ");
					annotationXML.setStrategies(strategies);

					serviceUnitXML.setXMLAnnotation(annotationXML);
				}

			}

			if (serviceTopology.hasConstraints()) {

				String costraints = "";

				for (Constraint constraint : serviceTopology.getConstraints()) {
					costraints += constraint.getId() + ":CONSTRAINT " + constraint.getMetric().getName()
							+ " " + constraint.getOperator().toString() + " " + constraint.getValue() + " "
							+ constraint.getMetric().getUnit() + ";";
				}
				SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

				costraints = costraints.replaceAll("  ", " ");
				annotationXML.setConstraints(costraints.trim());

				serviceTopologyXML.setXMLAnnotation(annotationXML);
			}

			if (serviceTopology.hasStrategies()) {
				String strategies = "";

				for (Strategy strategy : serviceTopology.getStrategies()) {

					String costraints = "";
					for (Constraint constraint : strategy.getConstraints()) {
						costraints += constraint.getMetric().getName() + " "
								+ constraint.getOperator().toString()
								+ " " + constraint.getValue() + " " + constraint.getMetric().getUnit() + " "
								+ strategy.getOperator().toString() + " ";
					}

					// remove last operator in strategy
					if (costraints.lastIndexOf("AND") > 0) {
						costraints = costraints.substring(0, costraints.lastIndexOf("AND")).trim();
					}

					costraints = costraints.trim();
					strategies += strategy.getId() + ":STRATEGY CASE " + costraints + ":"
							+ strategy.getAction().toString() + ";";

				}
				SYBLAnnotationXML annotationXML = new SYBLAnnotationXML();

				strategies = strategies.replaceAll("  ", " ");

				annotationXML.setStrategies(strategies.trim());

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
		Map<String, ServiceUnit> softwareUnits = new HashMap<>();

		for (ServiceTopology serviceTopology : serviceTemplate.getServiceTopologies()) {
			for (ServiceUnit serviceUnit : serviceTopology.getServiceUnits()) {
				softwareUnits.put(serviceUnit.getId(), serviceUnit);
			}
		}

		for (DeploymentUnit deploymentUnit : deploymentDescription.getDeployments()) {
			if (softwareUnits.containsKey(deploymentUnit.getServiceUnitID())) {
				Set<ElasticityCapability> capabilities = softwareUnits.get(deploymentUnit.getServiceUnitID())
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
