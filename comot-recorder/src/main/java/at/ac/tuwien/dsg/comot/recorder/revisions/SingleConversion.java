package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.graph.BusinessId;
import at.ac.tuwien.dsg.comot.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.recorder.model.ManagedRegion;

@Component
@Scope("prototype")
public class SingleConversion {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected GraphDatabaseService db;

	protected Neo4jOperations neo;
	protected Map<Object, InternalNode> nodes;
	protected ManagedRegion graph;

	@PostConstruct
	public void setUp() {
		neo = new Neo4jTemplate(db);
	}

	public ManagedRegion convertGraph(Object obj) throws IllegalArgumentException, IllegalAccessException {

		graph = new ManagedRegion();
		nodes = new HashMap<>();

		if (obj instanceof Collection) {
			for (Object one : (Collection<?>) obj) {
				createNode(one);
			}
		} else {
			createNode(obj);
		}

		return graph;
	}

	private InternalNode createNode(Object obj) throws IllegalArgumentException, IllegalAccessException {

		Class<?> clazz = obj.getClass();

		if (!clazz.isAnnotationPresent(NodeEntity.class)) {
			return null;
		}

		List<Field> fields = getInheritedNonStaticNonTransientNonNullFields(clazz, obj);

		InternalNode node = new InternalNode();
		nodes.put(obj, node);
		graph.addNode(node);

		// set businessId
		for (Field field : fields) {
			if (field.isAnnotationPresent(BusinessId.class)) {
				node.setBusinessId(field.get(obj).toString());
				break;
			}
		}

		node.setLabel(clazz.getSimpleName());
		node.setProperties(extractProperties(obj, fields));
		node.setRelationships(createAllRelationships(node, obj, fields));

		return node;
	}

	private Set<InternalRel> createAllRelationships(InternalNode node, Object obj, List<Field> fields)
			throws IllegalArgumentException, IllegalAccessException {

		Set<InternalRel> relSet = new HashSet<>();

		for (Field field : fields) {

			Class<?> fc = field.get(obj).getClass();

			// one
			if (fc.isAnnotationPresent(RelationshipEntity.class)
					|| fc.isAnnotationPresent(NodeEntity.class)) {
				createRelationship(node, field.getName(), field.get(obj));

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

	private InternalRel createRelationship(InternalNode from, String relName, Object obj)
			throws IllegalArgumentException,
			IllegalAccessException {

		Map<String, Object> properties = new HashMap<>();
		Object toObject = null;
		InternalNode toNode;

		// rich REL
		if (obj.getClass().isAnnotationPresent(RelationshipEntity.class)) {

			List<Field> fields = getInheritedNonStaticNonTransientNonNullFields(obj.getClass(), obj);

			for (Field to : fields) {
				if (to.isAnnotationPresent(EndNode.class)) {
					toObject = to.get(obj);
					break;
				}
			}
			properties = extractProperties(obj, fields);
		}

		// simple REL
		if (obj.getClass().isAnnotationPresent(NodeEntity.class)) {
			toObject = obj;
		}

		if (nodes.containsKey(toObject)) {
			toNode = nodes.get(toObject);
		} else {
			toNode = createNode(toObject);
		}

		InternalRel rel = new InternalRel(relName, from, toNode, properties);
		graph.addRelationship(rel);
		return rel;
	}

	private Map<String, Object> extractProperties(Object obj, List<Field> fields) throws IllegalArgumentException,
			IllegalAccessException {

		Map<String, Object> properties = new HashMap<>();

		for (Field field : fields) {

			Object fieldObj = field.get(obj);
			Class<?> fc = fieldObj.getClass();

			// @GraphId -> skip
			if (field.isAnnotationPresent(GraphId.class)) {
				continue;

				// primitives and wrappers -> properties
			} else if (fc.equals(Byte.class) || fc.equals(Short.class) || fc.equals(Integer.class)
					|| fc.equals(Long.class) || fc.equals(Float.class) || fc.equals(Double.class)
					|| fc.equals(Character.class) || fc.equals(String.class)) {

				properties.put(field.getName(), fieldObj);

				// DynamicProperties
			} else if (fieldObj instanceof DynamicProperties) {
				DynamicProperties props = (DynamicProperties) fieldObj;

				for (String name : props.getPropertyKeys()) {
					properties.put(field.getName() + "-" + name, props.getProperty(name));
				}

				// enum
			} else if (field.getType() instanceof Class && ((Class<?>) field.getType()).isEnum()) {
				properties.put(field.getName(), fieldObj.toString());
			}

		}

		return properties;
	}

	private List<Field> getInheritedNonStaticNonTransientNonNullFields(Class<?> clazz, Object obj)
			throws IllegalArgumentException, IllegalAccessException {

		List<Field> list = new ArrayList<>();

		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Transient.class)) {
				continue;
			}
			if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			field.setAccessible(true);

			Object value = field.get(obj);
			if (value == null) {
				continue;
			}

			log.debug("name: {}, type: {}", field.getName(), field.getType());
			list.add(field);
		}

		if (clazz.getSuperclass() == null) {
			return list;
		} else {
			List<Field> fromParent = getInheritedNonStaticNonTransientNonNullFields(clazz.getSuperclass(), obj);
			list.addAll(fromParent);
			return list;
		}
	}
}
