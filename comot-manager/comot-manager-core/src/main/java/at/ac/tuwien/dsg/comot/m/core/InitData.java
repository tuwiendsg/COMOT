package at.ac.tuwien.dsg.comot.m.core;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.enums.Action;
import at.ac.tuwien.dsg.comot.m.common.enums.ComotEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.m.core.adapter.ControlAdapterStatic;
import at.ac.tuwien.dsg.comot.m.core.adapter.DeploymentAdapterStatic;
import at.ac.tuwien.dsg.comot.m.core.adapter.MonitoringAdapterStatic;
import at.ac.tuwien.dsg.comot.m.cs.UtilsCs;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.devel.structure.CloudService;
import at.ac.tuwien.dsg.comot.model.devel.structure.Template;
import at.ac.tuwien.dsg.comot.model.provider.ComotCustomEvent;
import at.ac.tuwien.dsg.comot.model.provider.ComotLifecycleEvent;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
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

	public void setUpTestData() throws URISyntaxException, EpsException, JAXBException, IOException {

		String fileBase = env.getProperty("dir.files");

		URI deploymentUri = new URI(env.getProperty("uri.deployemnt"));
		URI monitoringUri = new URI(env.getProperty("uri.monitoring"));
		URI controllerUri = new URI(env.getProperty("uri.controller"));

		// SALSA

		OfferedServiceUnit deployment = new OfferedServiceUnit();
		deployment.setId(Constants.SALSA_SERVICE_STATIC);
		deployment.setType(OsuType.EPS.toString());
		deployment.hasResource(new Resource(DeploymentAdapterStatic.class.getCanonicalName(),
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
		monitoring.setId(Constants.MELA_SERVICE_STATIC);
		monitoring.setType(OsuType.EPS.toString());
		monitoring.hasResource(new Resource(MonitoringAdapterStatic.class.getCanonicalName(),
				new ResourceOrQualityType(Constants.ADAPTER_CLASS)));
		monitoring.hasResource(new Resource(monitoringUri.getHost(), new ResourceOrQualityType(
				Constants.IP)));
		monitoring.hasResource(new Resource("" + monitoringUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		monitoring.hasResource(new Resource(
				"/MELA/mela.html?" + Constants.PLACE_HOLDER_INSTANCE_ID,
				new ResourceOrQualityType(Constants.VIEW)));

		monitoring.hasPrimitiveOperation(
				new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
		monitoring.hasPrimitiveOperation(
				new ComotCustomEvent("Start monitoring", ComotEvent.MELA_START.toString(), false));
		monitoring.hasPrimitiveOperation(
				new ComotCustomEvent("Stop monitoring", ComotEvent.MELA_STOP.toString(), false));

		// RSYBL

		OfferedServiceUnit control = new OfferedServiceUnit();
		control.setId(Constants.RSYBL_SERVICE_STATIC);
		control.setType(OsuType.EPS.toString());
		control.hasResource(new Resource(ControlAdapterStatic.class.getCanonicalName(), new ResourceOrQualityType(
				Constants.ADAPTER_CLASS)));
		control.hasResource(new Resource(controllerUri.getHost(), new ResourceOrQualityType(Constants.IP)));
		control.hasResource(new Resource("" + controllerUri.getPort(), new ResourceOrQualityType(
				Constants.PORT)));
		control.hasResource(new Resource(
				"/rSYBL/",
				new ResourceOrQualityType(Constants.VIEW)));

		control.hasPrimitiveOperation(
				new ComotLifecycleEvent("Stop controller", Action.STOP_CONTROLLER.toString()));

		control.hasPrimitiveOperation(
				new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
		control.hasPrimitiveOperation(
				new ComotCustomEvent("Start controller", ComotEvent.RSYBL_START.toString(), false));
		control.hasPrimitiveOperation(
				new ComotCustomEvent("Stop controller", ComotEvent.RSYBL_STOP.toString(), false));

		// DYNAMIC EPS MELA

		CloudService melaService = mapperTosca.createModel(UtilsCs
				.loadTosca(fileBase + "adapterMela/mela_tosca_with_adapter_from_salsa.xml"));

		OfferedServiceUnit monitoringDynamic = new OfferedServiceUnit();
		monitoringDynamic.setId(Constants.MELA_SERVICE_DYNAMIC);
		monitoringDynamic.setType(OsuType.EPS.toString());

		monitoringDynamic.hasPrimitiveOperation(
				new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
		monitoringDynamic.hasPrimitiveOperation(
				new ComotCustomEvent("Start monitoring", ComotEvent.MELA_START.toString(), false));
		monitoringDynamic.hasPrimitiveOperation(
				new ComotCustomEvent("Stop monitoring", ComotEvent.MELA_STOP.toString(), false));

		monitoringDynamic.setServiceTemplate(new Template(melaService.getId(), melaService));

		// DYNAMIC EPS RSYBL

		CloudService rsyblService = mapperTosca.createModel(UtilsCs
				.loadTosca(fileBase + "adapterRsybl/rsybl_mela_with_adapter_tosca.xml"));

		OfferedServiceUnit rsyblDynamic = new OfferedServiceUnit();
		rsyblDynamic.setId(Constants.RSYBL_SERVICE_DYNAMIC);
		rsyblDynamic.setType(OsuType.EPS.toString());

		rsyblDynamic.hasPrimitiveOperation(
				new ComotLifecycleEvent("Stop controller", Action.STOP_CONTROLLER.toString()));

		rsyblDynamic.hasPrimitiveOperation(
				new ComotCustomEvent("Set Metric Composition Rules", ComotEvent.SET_MCR.toString(), true));
		rsyblDynamic.hasPrimitiveOperation(
				new ComotCustomEvent("Start control", ComotEvent.RSYBL_START.toString(), false));
		rsyblDynamic.hasPrimitiveOperation(
				new ComotCustomEvent("Stop control", ComotEvent.RSYBL_STOP.toString(), false));

		rsyblDynamic.setServiceTemplate(new Template(rsyblService.getId(), rsyblService));

		// DYNAMIC EPS SALSA

		CloudService salsaService = mapperTosca.createModel(UtilsCs
				.loadTosca(fileBase + "adapterSalsa/salsa_tosca.xml"));

		OfferedServiceUnit salsaDynamic = new OfferedServiceUnit();
		salsaDynamic.setId(Constants.SALSA_SERVICE_DYNAMIC);
		salsaDynamic.setType(OsuType.EPS.toString());

		salsaDynamic.setServiceTemplate(new Template(salsaService.getId(), salsaService));

		// // INSERT
		infoService.addOsu(monitoringDynamic);
		infoService.addOsu(rsyblDynamic);
		infoService.addOsu(salsaDynamic);

		infoService.addOsu(deployment);
		infoService.addOsu(monitoring);
		infoService.addOsu(control);

		try {
			infoService.createTemplate(mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "test/helloElasticity/HelloElasticity_ShortNames.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}
		// try {
		// infoService.createTemplate(mapperTosca.createModel(UtilsCs
		// .loadTosca(fileBase + "test/helloElasticity/HelloElasticityNoDB.xml")));
		// } catch (JAXBException | IOException e) {
		// e.printStackTrace();
		// }
		// try {
		//
		// infoService.createTemplate(mapperTosca.createModel(UtilsCs
		// .loadTosca(fileBase + "test/helloElasticity/HelloElasticityNoDB_Constraint.xml")));
		//
		// } catch (JAXBException | IOException e) {
		// e.printStackTrace();
		// }
		try {
			infoService.createTemplate(mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "test/tosca/daas_m2m_fromSalsa.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

		try {
			infoService.createTemplate(mapperTosca.createModel(UtilsCs
					.loadTosca(fileBase + "test/eval/HelloElasticity_error.xml")));
		} catch (JAXBException | IOException e) {
			e.printStackTrace();
		}

	}

}
