package at.ac.tuwien.dsg.comot.cs.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.common.model.EntityRelationship;
import at.ac.tuwien.dsg.comot.common.model.Navigator;
import at.ac.tuwien.dsg.comot.common.model.SyblDirective;
import at.ac.tuwien.dsg.comot.common.model.structure.CloudService;
import at.ac.tuwien.dsg.comot.common.model.structure.ServicePart;
import at.ac.tuwien.dsg.comot.common.model.type.DirectiveType;
import at.ac.tuwien.dsg.comot.common.model.type.RelationshipType;
import at.ac.tuwien.dsg.comot.cs.mapper.orika.MelaOrika;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.BinaryRestriction;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.BinaryRestrictionsConjunction;
import at.ac.tuwien.dsg.csdg.elasticityInformation.elasticityRequirements.Constraint;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.abstractModelXML.SYBLDirectiveMappingFromXML;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Metric;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MetricValue;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.Relationship;
import at.ac.tuwien.dsg.mela.common.requirements.Condition;
import at.ac.tuwien.dsg.mela.common.requirements.Requirements;
import at.ac.tuwien.dsg.mela.common.requirements.Condition.Type;
import at.ac.tuwien.dsg.mela.common.requirements.Requirement;

@Component
public class MelaMapper {

	protected final Logger log = LoggerFactory.getLogger(MelaMapper.class);

	@Autowired
	protected MelaOrika mapper;

	public MonitoredElement extractMela(CloudService cloudService) {

		MonitoredElement element;
		Relationship tempRel;

		// Navigator navigator = new Navigator(cloudService);
		MonitoredElement root = mapper.get().map(cloudService, MonitoredElement.class);

		Map<String, MonitoredElement> map = extractAllElements(root);

		for (EntityRelationship rel : cloudService.getRelationships()) {

			log.info("original from={} to={}", rel.getFrom().getId(), rel.getTo().getId());

			if (rel.isServicePartRelationship()) {

				String fromPartId = rel.getFromPart().getId();
				String toPartId = rel.getToPart().getId();

				log.info("part from={} to={}", fromPartId, toPartId);

				if (map.containsKey(fromPartId) && map.containsKey(toPartId)
						&& !rel.getType().equals(RelationshipType.LOCAL)) {

					tempRel = new Relationship()
							.withFrom(map.get(fromPartId))
							.withTo(map.get(toPartId))
							.withType(resolveType(rel.getType()));

					map.get(fromPartId).getRelationships().add(tempRel);
				}
			}
		}

		log.trace("Final mapping: {}", Utils.asXmlStringLog(root));

		return root;
	}

	protected Map<String, MonitoredElement> extractAllElements(MonitoredElement element) {

		Map<String, MonitoredElement> map = new HashMap<>();
		map.put(element.getId(), element);

		for (MonitoredElement child : element.getContainedElements()) {
			map.putAll(extractAllElements(child));
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

		for (ServicePart part : navigator.getAllServiceParts()) {
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
	protected List<Requirement> parseToRequirement(ServicePart servicePart, String constraint) {

		Requirement req;
		List<Requirement> requirements = new ArrayList<>();
		Constraint rConstraint = SYBLDirectiveMappingFromXML.mapSYBLAnnotationToXMLConstraint(constraint);

		log.trace("Constraint parsed from the string '{}' {}", constraint, Utils.asJsonString(rConstraint));

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
