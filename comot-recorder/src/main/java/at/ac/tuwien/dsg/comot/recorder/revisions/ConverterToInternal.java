package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.recorder.model.ManagedRegion;

/**
 * Convert a domain object annotated according to Spring Noe4j Data. Not thread-safe.
 * 
 * @author Juraj
 *
 */
@Component
@Scope("prototype")
public class ConverterToInternal {

	protected static final Logger log = LoggerFactory.getLogger(ConverterToInternal.class);

	protected Map<String, InternalNode> nodes;
	protected ManagedRegion region;

	public ManagedRegion convertToGraph(Object obj) throws IllegalArgumentException, IllegalAccessException {

		region = new ManagedRegion();
		nodes = new HashMap<>();

		createNode(obj);

		return region;
	}

	protected InternalNode createNode(Object obj) throws IllegalArgumentException, IllegalAccessException {

		Class<?> clazz = obj.getClass();

		if (!clazz.isAnnotationPresent(NodeEntity.class)) {
			return null;
		}

		List<Field> fields = MyReflectionUtils.getInheritedNonStaticNonTransientNonNullFields(obj);

		InternalNode node = new InternalNode();
		node.setBusinessId(extractBusinessId(obj, fields));
		node.setLabel(clazz.getSimpleName());
		node.setProperties(extractProperties(obj, fields));

		nodes.put(node.getBusinessId(), node);
		region.addNode(node);
		region.addClass(clazz.getSimpleName(), clazz.getCanonicalName());

		node.setRelationships(createAllRelationships(node, obj, fields));

		return node;
	}

	protected Set<InternalRel> createAllRelationships(InternalNode node, Object obj, List<Field> fields)
			throws IllegalArgumentException, IllegalAccessException {

		Set<InternalRel> relSet = new HashSet<>();

		for (Field field : fields) {
			Class<?> fc = field.get(obj).getClass();

			// one
			if (fc.isAnnotationPresent(RelationshipEntity.class)
					|| fc.isAnnotationPresent(NodeEntity.class)) {
				relSet.add(createRelationship(node, field.getName(), field.get(obj)));

				// Set
			} else if (field.get(obj) instanceof Set) {
				Set<?> set = (Set<?>) field.get(obj);

				for (Object one : set) {
					relSet.add(createRelationship(node, field.getName(), one));
				}
			}
		}

		return relSet;
	}

	protected InternalRel createRelationship(InternalNode from, String relName, Object obj)
			throws IllegalArgumentException,
			IllegalAccessException {

		Map<String, Object> properties = new HashMap<>();
		Object toObject = null;
		InternalNode toNode;

		// rich REL
		if (obj.getClass().isAnnotationPresent(RelationshipEntity.class)) {

			List<Field> fields = MyReflectionUtils.getInheritedNonStaticNonTransientNonNullFields(obj);

			for (Field to : fields) {
				if (to.isAnnotationPresent(EndNode.class)) {
					toObject = to.get(obj);
					break;
				}
			}

			properties = extractProperties(obj, fields);
			region.addClass(relName, obj.getClass().getCanonicalName());
		}

		// simple REL
		if (obj.getClass().isAnnotationPresent(NodeEntity.class)) {
			toObject = obj;
		}

		String toId = extractBusinessId(toObject,
				MyReflectionUtils.getInheritedNonStaticNonTransientNonNullFields(toObject));

		if (nodes.containsKey(toId)) {
			toNode = nodes.get(toId);
		} else {
			toNode = createNode(toObject);
		}

		InternalRel rel = new InternalRel(relName, from, toNode, properties);

		region.addRelationship(rel);
		return rel;
	}

	protected String extractBusinessId(Object obj, List<Field> fields) throws IllegalArgumentException,
			IllegalAccessException {
		for (Field field : fields) {
			if (field.isAnnotationPresent(BusinessId.class)) {
				return field.get(obj).toString();
			}
		}
		throw new RuntimeException("There is no fiel annotated with @BusinessId in " + obj);
	}

	protected Map<String, Object> extractProperties(Object obj, List<Field> fields)
			throws IllegalArgumentException,
			IllegalAccessException {

		Map<String, Object> properties = new HashMap<>();

		for (Field field : fields) {
			Object fieldObj = field.get(obj);
			Class<?> clazz = field.getType();

			// @GraphId @BusinesId -> skip
			if (field.isAnnotationPresent(GraphId.class) || field.isAnnotationPresent(BusinessId.class)) {
				continue;

				// primitives and wrappers -> properties
			} else if (clazz.equals(Byte.class) || clazz.equals(Short.class) || clazz.equals(Integer.class)
					|| clazz.equals(Long.class) || clazz.equals(Float.class) || clazz.equals(Double.class)
					|| clazz.equals(Character.class) || clazz.equals(String.class) || clazz.isPrimitive()) {

				properties.put(field.getName(), fieldObj);

				// DynamicProperties
			} else if (clazz.equals(DynamicProperties.class)) {
				DynamicProperties props = (DynamicProperties) fieldObj;
				region.addClass(DynamicProperties.class.getSimpleName(), fieldObj.getClass().getCanonicalName());

				for (String name : props.getPropertyKeys()) {
					properties.put(field.getName() + "-" + name, props.getProperty(name));
				}

				// enum
			} else if (clazz instanceof Class && ((Class<?>) clazz).isEnum()) {
				properties.put(field.getName(), ((Enum<?>) fieldObj).name());
			}

		}

		return properties;
	}

}
