package at.ac.tuwien.dsg.comot.cs.mapper;

import javax.xml.bind.JAXBException;

import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.ObjectFactory;

public class UtilsMapper {

	protected static ObjectFactory factoryRsybl = new ObjectFactory();

	public static String asString(Definitions definition) throws JAXBException {
		return Utils.asXmlString(definition, SalsaMappingProperties.class);
	}

	public static String asString(CloudServiceXML xml) throws JAXBException {
		;
		return Utils.asXmlString(factoryRsybl.createCloudService(xml), "at.ac.tuwien.dsg.comot.rsybl");
	}

}
