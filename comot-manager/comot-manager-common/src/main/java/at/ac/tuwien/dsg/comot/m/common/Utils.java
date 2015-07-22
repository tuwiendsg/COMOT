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
package at.ac.tuwien.dsg.comot.m.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

	protected static final Logger LOG = LoggerFactory.getLogger(Utils.class);

	@SuppressWarnings("unchecked")
	public static <T> T asObjectFromXml(String str, Class<T> clazz, Class<?>... otherClazz) throws JAXBException,
			IOException {

		List<Object> list = new ArrayList<Object>(Arrays.asList(otherClazz));
		list.add(clazz);

		JAXBContext context = JAXBContext.newInstance(list.toArray(new Class[list.size()]));
		Unmarshaller unmarshaller = context.createUnmarshaller();

		return (T) unmarshaller.unmarshal(new StringReader(str));
	}

	@SuppressWarnings("unchecked")
	public static <T> T asObjectFromJson(String str, Class<T> clazz, Class<?>... otherClazz) throws JAXBException {

		List<Object> list = new ArrayList<Object>(Arrays.asList(otherClazz));
		list.add(clazz);

		Map<String, Object> props = new HashMap<String, Object>();
		props.put(JAXBContextProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);

		JAXBContext context = JAXBContextFactory.createContext(list.toArray(new Class[list.size()]), props);
		Unmarshaller unm = context.createUnmarshaller();

		return (T) unm.unmarshal(new StringReader(str));
	}

	public static String asXmlStringLog(Object obj, Class<?>... clazz) {
		try {
			return asXmlString(obj, clazz);
		} catch (JAXBException e) {
			LOG.error("Fail to marshall to XML", e);
			return null;
		}
	}

	public static String asJsonString(Object obj, Class<?>... clazz) throws JAXBException {

		List<Object> list = new ArrayList<Object>(Arrays.asList(clazz));
		list.add(obj.getClass());

		StringWriter w = new StringWriter();

		Map<String, Object> props = new HashMap<String, Object>();
		// props.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
		props.put(JAXBContextProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);

		JAXBContext context = JAXBContextFactory.createContext(list.toArray(new Class[list.size()]), props);

		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(obj, w);

		return w.toString();
	}

	public static String asXmlString(Object obj, Class<?>... clazz) throws JAXBException {

		List<Object> list = new ArrayList<Object>(Arrays.asList(clazz));
		list.add(obj.getClass());

		StringWriter w = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(list.toArray(new Class[list.size()]));

		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(obj, w);

		return w.toString();
	}

	public static String asXmlString(Object obj, String contextPath) throws JAXBException {

		StringWriter w = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(contextPath);

		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(obj, w);

		return w.toString();
	}

	public static Object deepCopy(Object oldObj) throws IOException, ClassNotFoundException {

		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			oos = new ObjectOutputStream(bos);
			oos.writeObject(oldObj);
			oos.flush();

			ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));

			return ois.readObject();

		} finally {
			if (oos != null)
				oos.close();
			if (ois != null)
				ois.close();
		}
	}

	public static String loadFileFromSystemAsString(String path) throws IOException {
		return IOUtils.toString(loadFileFromSystem(path), "UTF-8");
	}

	public static InputStream loadFileFromSystem(String path) throws IOException {
		return FileUtils.openInputStream(new File(path));
	}

	public static InputStream loadFileFromClassPath(String path) throws IOException {
		return ClassLoader.getSystemResourceAsStream(path);
	}

}
