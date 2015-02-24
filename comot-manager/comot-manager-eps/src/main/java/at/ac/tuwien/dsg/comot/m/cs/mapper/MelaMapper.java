package at.ac.tuwien.dsg.comot.m.cs.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Navigator;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.comot.model.SyblDirective;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceEntity;
import at.ac.tuwien.dsg.comot.model.devel.structure.ServiceUnit;
import at.ac.tuwien.dsg.comot.model.runtime.UnitInstance;
import at.ac.tuwien.dsg.comot.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.model.type.RelationshipType;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.BinaryRestriction;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.BinaryRestrictionsConjunction;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Constraint;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.SYBLDirectiveMappingFromXML;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MetricValue;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement.MonitoredElementLevel;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Relationship;
import at.ac.tuwien.dsg.mela.common.requirements.Condition;
import at.ac.tuwien.dsg.mela.common.requirements.Condition.Type;
import at.ac.tuwien.dsg.mela.common.requirements.Requirement;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;

@Component
public class MelaMapper {

	protected final Logger log = LoggerFactory.getLogger(MelaMapper.class);

	@Autowired
	protected MelaOrika mapper;

	public MonitoredElement extractMela(CloudService cloudService) throws ClassNotFoundException, IOException {

		MonitoredElement vmElement;
		ServiceUnit node;
		Navigator navigator = new Navigator(cloudService);

		MonitoredElement root = mapper.get().map(cloudService, MonitoredElement.class);
		Map<MonitoredElement, MonitoredElement> map = extractAllElements(root, new MonitoredElement());

		log.trace("Orika mapping: {}", Utils.asXmlStringLog(root));

		// add VMs
		for (MonitoredElement element : map.keySet()) {
			if (element.getLevel().equals(MonitoredElementLevel.SERVICE_UNIT)) {

				if (!navigator.isTrueServiceUnit(element.getId())) {
					map.get(element).removeElement(element);
					continue;
				}

				node = navigator.getOsForServiceUnit(element.getId());

				for (UnitInstance instance : node.getInstances()) {
					vmElement = new MonitoredElement();
					vmElement.setLevel(MonitoredElementLevel.VM);
					vmElement.setId(instance.getEnvId());

					element.addElement(vmElement);
				}
			}
		}

		// add relationships TODO
		// for (EntityRelationship rel : cloudService.getRelationships()) {
		//
		// log.trace("original from={} to={}", rel.getFrom(), rel.getTo());
		//
		// if (resolver.isServicePartRelationship(rel)) {
		//
		// String fromPartId = resolver.resolveToServicePart(rel.getFrom()).getId();
		// String toPartId = resolver.resolveToServicePart(rel.getTo()).getId();
		//
		// log.trace("part from={} to={}", fromPartId, toPartId);
		//
		// if (map.containsKey(fromPartId) && map.containsKey(toPartId)
		// && !rel.getType().equals(RelationshipType.LOCAL)) {
		//
		// tempRel = new Relationship()
		// .withFrom(new MonitoredElement(fromPartId))
		// .withTo(new MonitoredElement(toPartId))
		// .withType(resolveType(rel.getType()));
		//
		// log.trace("inserted relationship from={} to={}", fromPartId, toPartId);
		//
		// map.get(fromPartId).getRelationships().add(tempRel);
		// }
		// }
		// }

		log.debug("Final mapping: {}", Utils.asXmlStringLog(root));

		return root;
	}

	/**
	 * 
	 * @param parent
	 * @param element
	 * @return returns map where key is an element and the value is its parent
	 */
	protected Map<MonitoredElement, MonitoredElement> extractAllElements(MonitoredElement element,
			MonitoredElement parent) {

		Map<MonitoredElement, MonitoredElement> map = new HashMap<>();
		map.put(element, parent);

		for (MonitoredElement child : element.getContainedElements()) {
			map.putAll(extractAllElements(child, element));
		}
		return map;
	}

	// TODO InConjunctionWith seems not to be eqivalent with anything in tosca
	protected Relationship.RelationshipType resolveType(RelationshipType type) {
		if (type.equals(RelationshipType.CONNECT_TO)) {
			return Relationship.RelationshipType.ConnectedTo;

		} else if (type.equals(RelationshipType.HOST_ON)) {
			return Relationship.RelationshipType.HostedOn;

		} else if (type.equals(RelationshipType.LOCAL)) {
			throw new UnsupportedOperationException();

		} else {
			throw new UnsupportedOperationException();
		}
	}

	public Requirements extractRequirements(CloudService cloudService) {

		Navigator navigator = new Navigator(cloudService);

		List<Requirement> requirementList = new ArrayList<>();

		Requirements requirements = new Requirements();
		requirements.setTargetServiceID(cloudService.getId());
		requirements.setRequirements(requirementList);

		for (ServiceEntity part : navigator.getAllServiceParts()) {
			for (SyblDirective directive : part.getDirectives()) {
				if (directive.getType().equals(DirectiveType.CONSTRAINT)) {
					requirementList.addAll(parseToRequirement(part, directive.getDirective()));
				}
			}
		}

		return requirements;
	}

	// see
	// at.ac.tuwien.dsg.rSybl.dataProcessingUnit.monitoringPlugins.melaPlugin.MELA_API3.submitElasticityRequirements()
	// !!! number must be on the right side, there is a bug in rsybl
	protected List<Requirement> parseToRequirement(ServiceEntity servicePart, String constraint) {

		log.trace("parsing constraint: {}", constraint);

		Requirement req;
		List<Requirement> requirements = new ArrayList<>();
		Constraint rConstraint = SYBLDirectiveMappingFromXML.mapSYBLAnnotationToXMLConstraint(constraint);

		// log.trace("Constraint parsed from the string '{}' {}", constraint, Utils.asJsonString(rConstraint));

		for (BinaryRestrictionsConjunction binaryRestrictions : rConstraint.getToEnforce().getBinaryRestriction()) {
			for (BinaryRestriction binaryRestriction : binaryRestrictions.getBinaryRestrictions()) {

				ArrayList<String> targetedEls = new ArrayList<String>();
				targetedEls.add(servicePart.getId());

				List<Condition> conditions = new ArrayList<Condition>();
				Condition cond = new Condition();
				conditions.add(cond);

				req = new Requirement();
				req.setId(servicePart.getId());
				req.setTargetMonitoredElementIDs(targetedEls);
				req.setTargetMonitoredElementLevel(MelaOrika.decideLevel(servicePart));
				req.setConditions(conditions);

				String metricName;
				MetricValue metricValue = new MetricValue();

				if (binaryRestriction.getLeftHandSide().getMetric() != null) {

					metricName = binaryRestriction.getLeftHandSide().getMetric();
					metricValue.setValue(Double.parseDouble(binaryRestriction.getRightHandSide().getNumber()));

					switch (binaryRestriction.getType()) {
					case "lessThan":
						cond.setType(Type.LESS_THAN);
						break;
					case "greaterThan":
						cond.setType(Type.GREATER_THAN);
						break;
					case "lessThanOrEqual":
						cond.setType(Type.LESS_EQUAL);
						break;
					case "greaterThanOrEqual":
						cond.setType(Type.GREATER_EQUAL);
						break;
					case "differentThan":
						break;
					case "equals":
						cond.setType(Type.EQUAL);
						break;
					default:
						cond.setType(Type.LESS_THAN);
						break;
					}

				} else if (binaryRestriction.getRightHandSide().getMetric() != null) {

					metricName = binaryRestriction.getRightHandSide().getMetric();
					metricValue.setValue(Double.parseDouble(binaryRestriction.getLeftHandSide().getNumber()));

					switch (binaryRestriction.getType()) {
					case "lessThan":
						cond.setType(Type.GREATER_THAN);
						break;
					case "greaterThan":
						cond.setType(Type.LESS_THAN);
						break;
					case "lessThanOrEqual":
						cond.setType(Type.GREATER_EQUAL);
						break;
					case "greaterThanOrEqual":
						cond.setType(Type.LESS_EQUAL);
						break;
					case "differentThan":
						break;
					case "equals":
						cond.setType(Type.EQUAL);
						break;
					default:
						cond.setType(Type.LESS_THAN);
						break;
					}
				} else {
					throw new IllegalArgumentException("One side of binary restriction MUST contain a Metric");
				}

				Metric metric = new Metric();
				metric.setName(metricName);
				metric.setMeasurementUnit(null);

				req.setMetric(metric);
				cond.addValue(metricValue);

				requirements.add(req);
			}
		}

		return requirements;
	}

}
