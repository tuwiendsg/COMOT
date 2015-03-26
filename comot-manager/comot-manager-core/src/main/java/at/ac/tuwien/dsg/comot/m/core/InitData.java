package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.processor.ControlAdapter;
import at.ac.tuwien.dsg.comot.m.adapter.processor.MonitoringAdapter;
import at.ac.tuwien.dsg.comot.m.common.ComotAction;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.adapter.DeploymentAdapter;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.provider.PrimitiveOperation;
import at.ac.tuwien.dsg.comot.model.provider.Resource;
import at.ac.tuwien.dsg.comot.model.provider.ResourceOrQualityType;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

@Component
public class InitData {

	@Autowired
	protected InformationClient infoService;
	@Autowired
	protected ToscaMapper mapperTosca;
	@javax.annotation.Resource
	public Environment env;

	public void setUpTestData() throws URISyntaxException, EpsException {

		URI deploymentUri = new URI(env.getProperty("uri.deployemnt"));
		URI monitoringUri = new URI(env.getProperty("uri.monitoring"));
		URI controllerUri = new URI(env.getProperty("uri.controller"));

		// SALSA

		OfferedServiceUnit deployment = new OfferedServiceUnit();
		deployment.setId(Constants.SALSA_SERVICE_PUBLIC_ID);
		deployment.setType(OsuType.EPS.toString());
		deployment.hasResource(new Resource(DeploymentAdapter.class.getCanonicalName(),
				new ResourceOrQualityType(Constants.ADAPTER_CLASS)));
		deployment.hasResource(new Resource(deploymentUri.getHost(),
				new ResourceOrQualityType(Constants.IP)));
		deployment.hasResource(new Resource("" + deploymentUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		deployment.hasResource(new Resource(
				"/salsa-engine?id=" + Constants.PLACE_HOLDER_INSTANCE_ID,
				new ResourceOrQualityType(Constants.VIEW)));

		// MELA
		// TODO use metrics
		OfferedServiceUnit monitoring = new OfferedServiceUnit();
		monitoring.setId(Constants.MELA_SERVICE_PUBLIC_ID);
		monitoring.setType(OsuType.EPS.toString());
		monitoring.hasResource(new Resource(MonitoringAdapter.class.getCanonicalName(),
				new ResourceOrQualityType(Constants.ADAPTER_CLASS)));
		monitoring.hasResource(new Resource(monitoringUri.getHost(), new ResourceOrQualityType(
				Constants.IP)));
		monitoring.hasResource(new Resource("" + monitoringUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		monitoring.hasResource(new Resource(
				"/MELA/mela.html?" + Constants.PLACE_HOLDER_INSTANCE_ID,
				new ResourceOrQualityType(Constants.VIEW)));

		monitoring.hasPrimitiveOperation(
				new PrimitiveOperation("Set Metric Composition Rules", ComotAction.MELA_SET_MCR.toString()));
		monitoring.hasPrimitiveOperation(
				new PrimitiveOperation("Start monitoring", ComotAction.MELA_START.toString()));
		monitoring.hasPrimitiveOperation(
				new PrimitiveOperation("Stop monitoring", ComotAction.MELA_STOP.toString()));

		// RSYBL

		OfferedServiceUnit control = new OfferedServiceUnit();
		control.setId(Constants.RSYBL_SERVICE_PUBLIC_ID);
		control.setType(OsuType.EPS.toString());
		control.hasResource(new Resource(ControlAdapter.class.getCanonicalName(), new ResourceOrQualityType(
				Constants.ADAPTER_CLASS)));
		control.hasResource(new Resource(controllerUri.getHost(), new ResourceOrQualityType(Constants.IP)));
		control.hasResource(new Resource("" + controllerUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		control.hasResource(new Resource(
				"/rSYBL/",
				new ResourceOrQualityType(Constants.VIEW)));

		control.hasPrimitiveOperation(
				new PrimitiveOperation("Set Metric Composition Rules", ComotAction.RSYBL_SET_MCR.toString()));
		control.hasPrimitiveOperation(
				new PrimitiveOperation("Start controller", ComotAction.RSYBL_START.toString()));
		control.hasPrimitiveOperation(
				new PrimitiveOperation("Stop controller", ComotAction.RSYBL_STOP.toString()));

		// // INSERT

		infoService.addOsu(deployment);
		infoService.addOsu(monitoring);
		infoService.addOsu(control);

		try {
			infoService.createService(mapperTosca.createModel(UtilsCs
					.loadTosca("./../resources/test/helloElasticity/HelloElasticityNoDB.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

		try {

			infoService.createService(mapperTosca.createModel(UtilsCs
					.loadTosca("./../resources/test/elasticWS/elasticWS_tosca_enhanced.xml")));

		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

		try {
			infoService.createService(mapperTosca.createModel(UtilsCs
					.loadTosca("./../resources/test/tosca/daas_m2m_fromSalsa.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

		try {
			infoService.createService(mapperTosca.createModel(UtilsCs
					.loadTosca("./../resources/test/tosca/ExampleExecutableOnVM.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

	}

}
