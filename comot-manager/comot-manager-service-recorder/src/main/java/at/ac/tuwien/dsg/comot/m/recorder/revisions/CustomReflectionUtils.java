/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.recorder.revisions;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.recorder.BusinessId;

public class CustomReflectionUtils {

	private static final Logger LOG = LoggerFactory.getLogger(CustomReflectionUtils.class);

	public static List<Field> getInheritedNonStaticNonTransientNonNullFields(Object obj)
			throws IllegalArgumentException, IllegalAccessException {
		List<Field> list = new ArrayList<>();

		for (Field field : getInheritedNonStaticNonTransientFields(obj.getClass())) {
			if (field.get(obj) != null) {
				list.add(field);
			} else {
				if (field.isAnnotationPresent(BusinessId.class)) {
					throw new RuntimeException("Field '" + field + "' annotated with @BusinessId must not be null !");
				}
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

	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {

		if (clazz.equals(Byte.class) || clazz.equals(Short.class) || clazz.equals(Integer.class)
				|| clazz.equals(Long.class) || clazz.equals(Float.class) || clazz.equals(Double.class)
				|| clazz.equals(Character.class) || clazz.equals(String.class) || clazz.isPrimitive()) {
			return true;
		} else {
			return false;
		}
	}

	public static Class<?> classOfCollection(Field field) {
		return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
	}

}
