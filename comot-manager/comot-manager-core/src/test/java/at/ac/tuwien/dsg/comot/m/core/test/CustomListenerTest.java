package at.ac.tuwien.dsg.comot.m.core.test;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import at.ac.tuwien.dsg.comot.m.common.eps.DeploymentClient;

@RunWith(MockitoJUnitRunner.class)
public class CustomListenerTest {

	@Mock
	private DeploymentClient deployment;

	// @Test
	// public void testAkTomuPoslemParameterrEXYZ_TakSaMiZavolaDeploymentClientXYZ() throws ClassNotFoundException,
	// EpsException, IOException, JAXBException, ComotException, InterruptedException {
	//
	// // priprav data
	// StateMessage sm = new StateMessage();
	//
	// CloudService nejakyServis = new CloudService();
	// CloudService nejakyServis2 = new CloudService();
	// CloudService nejakyServis3 = new CloudService();
	// Mockito.when(deployment.deploy(Mockito.any(CloudService.class))).thenReturn(nejakyServis);
	// Mockito.when(deployment.refreshStatus(Mockito.any(CloudService.class)))
	// .thenReturn(nejakyServis)
	// .thenReturn(nejakyServis2)
	// .thenReturn(nejakyServis3);
	//
	// // zavolaj
	// CustomListener tested = tested();
	// tested.onLifecycleEvent(sm, "inst", "action", "groupId", Action.ALLOCATED, "optionalMessage", null, null);
	//
	// // skontroluj
	// Mockito.verify(deployment, Mockito.times(1)).deploy(Mockito.any(CloudService.class));
	// Mockito.verify(deployment, Mockito.times(3)).refreshStatus(Mockito.any(CloudService.class));
	// }
	//
	// public CustomListener tested() {
	// DeploymentAdapter da = new DeploymentAdapter();
	// da.setDeployment(deployment);
	// da.setInfoService(new InformationServiceMock());
	// return da.new CustomListener("dasd");
	// }

}
