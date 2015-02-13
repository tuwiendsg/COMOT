package at.ac.tuwien.dsg.comot.m.recorder.revisions;

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
import org.springframework.data.neo4j.annotation.StartNode;
import org.springframework.data.neo4j.fieldaccess.DynamicProperties;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.recorder.RecorderException;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalNode;
import at.ac.tuwien.dsg.comot.m.recorder.model.InternalRel;
import at.ac.tuwien.dsg.comot.m.recorder.model.ManagedRegion;
import at.ac.tuwien.dsg.comot.recorder.BusinessId;

/**
 * Convert a domain object annotated according to Spring Noe4j Data. Not thread-safe.
 * 
 * @author Juraj
 *
 */
@Component
@Scope("prototype")
public class ConverterFromInternal {

	protected static final Logger log = LoggerFactory.getLogger(ConverterFromInternal.class);

	protected Map<String, Object> objects;
	protected ManagedRegion region;

	protected Class<?> resolveClassToInstantiate(String name) throws ClassNotFoundException, RecorderException {

		String qualified = region.getClasses().get(name);

		if (qualified == null) {
			throw new RecorderException("Could not find class for node/relationship '" + name + "'");
		}

		return Class.forName(qualified);
	}

	public Object convertToObject(ManagedRegion region) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException, RecorderException {

		this.region = region;
		objects = new HashMap<>();

		Object object = createObject(region.getStartNode());

		return object;

	}

	public Object createObject(InternalNode node) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException, RecorderException {

		Class<?> clazz = resolveClassToInstantiate(node.getLabel());
		List<Field> fields = CustomReflectionUtils.getInheritedNonStaticNonTransientFields(clazz);

		// do properties
		Object object = createObjectWithProperties(clazz, fields, node.getProperties());
		objects.put(node.getBusinessId(), object);

		log.debug("created: {} {}", clazz, node.getBusinessId());

		for (Field field : fields) {
			Class<?> fc = field.getType();

			// businessId
			if (field.isAnnotationPresent(BusinessId.class)) {
				field.set(object, node.getBusinessId());

				// one relationship
			} else if (fc.isAnnotationPresent(RelationshipEntity.class)
					|| fc.isAnnotationPresent(NodeEntity.class)) {

				for (InternalRel rel : node.getRelationships()) {
					if (rel.getType().equals(field.getName())) {
						field.set(object, doOneRel(fc, rel));
					}
				}

				// Set relationship
			} else if (fc.equals(Set.class)) {

				if (CustomReflectionUtils.isPrimitiveOrWrapper(CustomReflectionUtils.classOfSet(field))) {
					continue;
				}

				Set<Object> relSet = new HashSet<>();
				Class<?> setClass = CustomReflectionUtils.classOfSet(field);

				for (InternalRel rel : node.getRelationships()) {
					if (rel.getType().equals(field.getName())) {
						relSet.add(doOneRel(setClass, rel));
					}
				}

				field.set(object, relSet);
			}
		}

		return object;
	}

	protected Object doOneRel(Class<?> fc, InternalRel rel)
			throws IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException, RecorderException {

		Object object = null;
		Object other;

		// rich REL
		if (fc.isAnnotationPresent(RelationshipEntity.class)) {

			Class<?> clazz = resolveClassToInstantiate(rel.getType());
			List<Field> fields = CustomReflectionUtils.getInheritedNonStaticNonTransientFields(clazz);

			object = createObjectWithProperties(clazz, fields, rel.getProperties());

			log.debug("created from rel: {} {}", clazz, rel.getType());

			for (Field field : fields) {
				if (field.isAnnotationPresent(EndNode.class)) {

					if (objects.containsKey(rel.getEndNode().getBusinessId())) {
						other = objects.get(rel.getEndNode().getBusinessId());
					} else {
						other = createObject(rel.getEndNode());
					}

					field.set(object, other);

				} else if (field.isAnnotationPresent(StartNode.class)) {
					field.set(object, objects.get(rel.getStartNode().getBusinessId()));
				}
			}

			// simple REL
		} else if (fc.isAnnotationPresent(NodeEntity.class)) {
			if (objects.containsKey(rel.getEndNode().getBusinessId())) {
				object = objects.get(rel.getEndNode().getBusinessId());
			} else {
				object = createObject(rel.getEndNode());
			}

		}

		return object;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object createObjectWithProperties(Class<?> objClazz, List<Field> fields,
			Map<String, Object> properties)
			throws IllegalArgumentException,
			IllegalAccessException, InstantiationException, ClassNotFoundException, RecorderException {

		Object obj = objClazz.newInstance();

		for (Field field : fields) {

			Class<?> clazz = field.getType();

			// @GraphId @BusinesId -> skip
			if (field.isAnnotationPresent(GraphId.class) || field.isAnnotationPresent(BusinessId.class)) {
				continue;

				// primitives and wrappers
			} else if (CustomReflectionUtils.isPrimitiveOrWrapper(clazz)) {
				field.set(obj, properties.get(field.getName()));

				// DynamicProperties
			} else if (clazz.equals(DynamicProperties.class)) {

				DynamicProperties props = (DynamicProperties) resolveClassToInstantiate(
						DynamicProperties.class.getSimpleName()).newInstance();
				String prefix = field.getName() + "-";

				for (String name : properties.keySet()) {
					if (name.startsWith(prefix)) {
						props.setProperty(name.substring(prefix.length()), properties.get(name));
					}
				}
				field.set(obj, props);

				// enum
			} else if (clazz.isEnum()) {

				Object enumValue = properties.get(field.getName());
				if (enumValue != null) {
					field.set(obj, Enum.valueOf((Class<Enum>) field.getType(), enumValue.toString()));
				}

				// collection of primitives
			} else if (field.get(obj) instanceof Set) {
				if (CustomReflectionUtils.isPrimitiveOrWrapper(CustomReflectionUtils.classOfSet(field))) {

					Set<Object> set = new HashSet<>();

					String prefix = field.getName() + "-";

					for (String name : properties.keySet()) {
						if (name.startsWith(prefix)) {
							set.add(properties.get(name));
						}
					}
					field.set(obj, set);
				}
			}
		}

		return obj;
	}

}
