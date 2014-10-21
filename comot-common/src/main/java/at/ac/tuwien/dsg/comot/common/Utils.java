package at.ac.tuwien.dsg.comot.common;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Utils {

	public static String xmlObjToString(Object obj) throws JAXBException {
		
		StringWriter w = new StringWriter();
		JAXBContext context = JAXBContext.newInstance(obj.getClass());

		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(obj, w);

		return w.toString();
	}
}
