package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
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
import org.springframework.transaction.annotation.Transactional;

@Component
@Scope("prototype")
public class SingleConversion {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	protected GraphDatabaseService db;

	protected Neo4jOperations neo;
	Map<Object, Node> nodes = new HashMap<>();

	@PostConstruct
	public void setUp() {
		neo = new Neo4jTemplate(db);
	}

	@Transactional
	public void convertGraph(Object obj) throws IllegalArgumentException, IllegalAccessException {

		convertToNode(obj);
		nodes = new HashMap<>();
	}

	private Node convertToNode(Object obj) throws IllegalArgumentException, IllegalAccessException {

		Class<?> clazz = obj.getClass();

		if (!clazz.isAnnotationPresent(NodeEntity.class)) {
			return null;
		}

		List<Field> fields = getInheritedNonStaticNonTransientNonNullFields(clazz, obj);

		// set labels
		List<String> labels = new ArrayList<>();
		labels.add(clazz.getSimpleName());
		Node node = neo.createNode(extractProperties(obj, fields), labels);
		nodes.put(obj, node);

		// process fields
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
					createRelationship(node, field.getName(), one);
				}
			}
		}

		return node;
	}

	private void createRelationship(Node from, String relName, Object obj) throws IllegalArgumentException,
			IllegalAccessException {

		Map<String, Object> properties = null;
		Object toObject = null;

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
			neo.createRelationshipBetween(from, nodes.get(toObject), relName, properties);
		} else {
			neo.createRelationshipBetween(from, convertToNode(toObject), relName, properties);
		}

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

				for(String name: props.getPropertyKeys()){
					properties.put(field.getName()+"-"+name, props.getProperty(name));
				}
				
				// enum
			} else if(field.getType() instanceof Class && ((Class<?>)field.getType()).isEnum()){
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

			log.info("name: {}, type: {}", field.getName(), field.getType());
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
