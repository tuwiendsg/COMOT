package at.ac.tuwien.dsg.comot.m.adapter.example;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import at.ac.tuwien.dsg.comot.m.adapter.general.EpsAdapterManager;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.MngPath;
import at.ac.tuwien.dsg.comot.model.provider.ComotCustomEvent;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;
import at.ac.tuwien.dsg.comot.model.type.OsuType;

@Component
public class SampleStandalone {

	@Autowired
	protected SampleProcessor processor;
	@Autowired
	protected EpsAdapterManager manager;

	@Autowired
	protected ApplicationContext context;

	public static String SOME_CUSTOM_EVENT = "SOME_CUSTOM_EVENT";

	public void startAdapter() throws Exception {

		String baseUri = "http://localhost:8580/comot/rest/";

		// describe the new EPS
		OfferedServiceUnit eps = new OfferedServiceUnit("OurCustomEPSid", "Our Custom EPS Name",
				OsuType.EPS.toString(),
				new String[] { Constants.ROLE_OBSERVER });
		eps.hasPrimitiveOperation( // optionally define custom event
		new ComotCustomEvent("Do something", SOME_CUSTOM_EVENT, false, ComotCustomEvent.Type.ACTION));

		Client client = ClientBuilder.newClient();

		// insert the description of the EPS into the management system
		Response response = client.target(baseUri)
				.path(MngPath.EPS_EXTERNAL)
				.request(MediaType.WILDCARD)
				.post(Entity.xml(eps));
		String epsInstanceId = response.readEntity(String.class);
		client.close();

		// start adapter
		manager.start(epsInstanceId, processor);
	}
}
