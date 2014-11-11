package at.ac.tuwien.dsg.comot.cs.mapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.xml.bind.JAXBException;

import org.oasis.tosca.Definitions;

import at.ac.tuwien.dsg.cloud.salsa.tosca.extension.SalsaMappingProperties;
import at.ac.tuwien.dsg.comot.common.Utils;
import at.ac.tuwien.dsg.comot.rsybl.CloudServiceXML;
import at.ac.tuwien.dsg.comot.rsybl.ObjectFactory;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import at.ac.tuwien.dsg.mela.common.monitoringConcepts.MonitoredElement;

public class UtilsMapper {

	protected static ObjectFactory factoryRsybl = new ObjectFactory();

	public static String asString(Definitions definition) throws JAXBException {
		return Utils.asXmlString(definition, SalsaMappingProperties.class);
	}

	public static String asString(MonitoredElement element) throws JAXBException {
		return Utils.asXmlString(element);
	}

	public static String asString(DeploymentDescription deployment) throws JAXBException {
		return Utils.asXmlString(deployment);
	}

	public static String asString(CloudServiceXML xml) throws JAXBException {
		return Utils.asXmlString(factoryRsybl.createCloudService(xml), "at.ac.tuwien.dsg.comot.rsybl");
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

}
