/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.integration.interraction.salsa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.client.SalsaResponse;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;

public class SalsaInterraction {

	private DefaultSalsaClient defaultSalsaClient;
	
	private static final long CHECK_STATE_TIMEOUT = 10000;

	private static final Logger log = LoggerFactory.getLogger(SalsaInterraction.class);

	{
		defaultSalsaClient = new DefaultSalsaClient();
		defaultSalsaClient.getConfiguration().setHost("128.130.172.215");

	}

	public DeploymentDescription getServiceDeploymentInfo(String serviceId) {
		DeploymentDescription deploymentInfo = new DeploymentDescription();

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Getting deployment information for serviceId {}", serviceId);
		}

		SalsaResponse response = defaultSalsaClient.getServiceDeploymentInfo(serviceId);

		String serviceDescription = response.getMessage();
		if (response.isExpected()) {
			try {
				JAXBContext a = JAXBContext.newInstance(DeploymentDescription.class);
				Unmarshaller u = a.createUnmarshaller();
				if (!serviceDescription.equalsIgnoreCase("")) {
					Object object = u.unmarshal(new StringReader(serviceDescription));
					deploymentInfo = (DeploymentDescription) object;
				}
			} catch (JAXBException e) {
				log.error(e.getStackTrace().toString());
			}
		}

		return deploymentInfo;
	}

	public CloudService getStatus(String serviceId) {
		CloudService deploymentInfo = null;

		if (log.isDebugEnabled()) {
			log.debug(Markers.CLIENT, "Getting deployment information for serviceId {}", serviceId);
		}

		SalsaResponse response = defaultSalsaClient.status(serviceId);

		String serviceDescription = response.getMessage();
		try {
			JAXBContext a = JAXBContext.newInstance(CloudService.class);
			Unmarshaller u = a.createUnmarshaller();
			if (!serviceDescription.equalsIgnoreCase("")) {
				Object object = u.unmarshal(new StringReader(serviceDescription));
				deploymentInfo = (CloudService) object;
			}
		} catch (JAXBException e) {
			log.error(e.getStackTrace().toString());
		}

		return deploymentInfo;
	}

	public void waitUntilRunning(String serviceID) {

		boolean inProgress = true;
		CloudService service;

		do {

			try {
				Thread.sleep(CHECK_STATE_TIMEOUT);
			} catch (InterruptedException ex) {
				log.error(ex.getMessage(), ex);
			}

			inProgress = true;
			service = this.getStatus(serviceID);
			
			log.info("Waiting until service " + serviceID + " is running. Now in state={}", service.getState());

			if (service.getState().equals(SalsaEntityState.DEPLOYED)
					|| service.getState().equals(SalsaEntityState.RUNNING)) {
				inProgress = false;

				for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit serviceUnit : service
						.getAllComponentByType(SalsaEntityType.SOFTWARE)) {

					if (serviceUnit != null
							&& !(serviceUnit.getState().equals(SalsaEntityState.DEPLOYED) || serviceUnit.getState()
									.equals(SalsaEntityState.RUNNING))) {
						inProgress = true;
						break;
					}
				}
			}

		} while (inProgress);

	}

	public void deploy(at.ac.tuwien.dsg.comot.common.model.CloudService application) {
		ToscaDescriptionBuilderImpl tdbi = new ToscaDescriptionBuilderImpl();

		String tosca = tdbi.toXml(application);

		{

			try {

				URL url = new URL("http://" + defaultSalsaClient.getConfiguration().getHost() + ":"
						+ defaultSalsaClient.getConfiguration().getPort() + "/salsa-engine/rest/services/xml");

				HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
				httpCon.setDoOutput(true);
				httpCon.setRequestProperty("Content-Type", "application/xml");
				httpCon.setRequestProperty("Accept", "application/xml");
				httpCon.setRequestMethod("PUT");
				OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
				BufferedWriter writer = new BufferedWriter(out);
				writer.write(tosca);
				writer.flush();
				out.close();

				// read error
				{
					InputStream stream = httpCon.getErrorStream();
					if (stream != null) {
						String line = "";
						BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
						while ((line = reader.readLine()) != null) {
							log.error(line);
						}
					}
				}
				// read error
				{
					InputStream stream = httpCon.getInputStream();
					String line = "";
					if (stream != null) {
						BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
						while ((line = reader.readLine()) != null) {
							log.warn(line);
						}
					}
				}

			} catch (IOException ex) {
				log.error(ex.getMessage(), ex);
			}
		}

	}

	public SalsaInterraction withDefaultSalsaClient(final DefaultSalsaClient defaultSalsaClient) {
		this.defaultSalsaClient = defaultSalsaClient;
		return this;
	}

}
