package at.ac.tuwien.dsg.comot.recorder.revisions;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyReflectionUtils {

	protected static final Logger log = LoggerFactory.getLogger(MyReflectionUtils.class);

	public static List<Field> getInheritedNonStaticNonTransientNonNullFields(Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		List<Field> list = new ArrayList<>();

		for (Field field : getInheritedNonStaticNonTransientFields(obj.getClass())) {
			if (field.get(obj) != null) {
				list.add(field);
			}
		}
		return list;
	}

	public static List<Field> getInheritedNonStaticNonTransientFields(Class<?> clazz)
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

			log.debug("name: {}, type: {}", field.getName(), field.getType());
			list.add(field);
		}

		if (clazz.getSuperclass() == null) {
			return list;
		} else {
			List<Field> fromParent = getInheritedNonStaticNonTransientFields(clazz.getSuperclass());
			list.addAll(fromParent);
			return list;
		}
	}

}
