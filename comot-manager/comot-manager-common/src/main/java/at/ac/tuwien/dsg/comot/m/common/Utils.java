package at.ac.tuwien.dsg.comot.m.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;

public class Utils {

	protected static final Logger log = LoggerFactory.getLogger(Utils.class);

	public static String asXmlStringLog(Object obj, Class<?>... clazz) {
		try {
			return asXmlString(obj, clazz);
		} catch (JAXBException e) {
			log.error("Fail to marshall to XML", e);
			return null;
		}
	}
	
	public static String asJsonString(Object obj, Class<?>... clazz) throws JAXBException {

		List<Object> list = new ArrayList<Object>(Arrays.asList(clazz));
		list.add(obj.getClass());

		StringWriter w = new StringWriter();
		
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
		props.put(JAXBContextProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
		
		JAXBContext context = JAXBContext.newInstance(list.toArray(new Class[list.size()]), props);

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

	// public static String asJsonString(Object obj) {
	//
	// Gson gson = new GsonBuilder().setPrettyPrinting().create();
	// return gson.toJson(obj);
	//
	// }

	public static String asString(CloudService service) throws JAXBException {
		return Utils.asXmlString(service);
	}

	static public Object deepCopy(Object oldObj) throws IOException, ClassNotFoundException {

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

	public static String loadFile(String path) throws IOException {
		return IOUtils.toString(loadFileFromSystem(path), "UTF-8");
	}

	public static InputStream loadFileFromSystem(String path) throws IOException {
		return FileUtils.openInputStream(new File(path));
	}

	public static InputStream loadFileFromClassPath(String path) throws IOException {
		return ClassLoader.getSystemResourceAsStream(path);
	}
}
