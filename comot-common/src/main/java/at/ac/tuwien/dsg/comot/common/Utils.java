package at.ac.tuwien.dsg.comot.common;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

	public static String asXmlString(Object obj, Class... clazz) throws JAXBException {

		List<Class> list = new ArrayList(Arrays.asList(clazz));
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

	public static String asJsonString(Object obj) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(obj);

	}
}
