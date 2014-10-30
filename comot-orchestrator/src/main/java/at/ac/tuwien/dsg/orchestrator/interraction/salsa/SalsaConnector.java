/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.orchestrator.interraction.salsa;

import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.CloudService;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityState;
import at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.enums.SalsaEntityType;
import at.ac.tuwien.dsg.comot.api.ToscaDescriptionBuilderImpl;
import at.ac.tuwien.dsg.comot.client.DefaultSalsaClient;
import at.ac.tuwien.dsg.comot.client.SalsaResponse;
import at.ac.tuwien.dsg.comot.common.logging.Markers;
import at.ac.tuwien.dsg.csdg.inputProcessing.multiLevelModel.deploymentDescription.DeploymentDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

//
//UNDEPLOYED("undeployed"),
//	ALLOCATING("allocating"),
//	STAGING("staging"),
//	STAGING_ACTION("staging_action"),
//	CONFIGURING("configuring"),
////	STOPPED("stopped"),	
//	RUNNING("running"),	// the deployment action is executed
//	DEPLOYED("deployed"), // deployed
//	ERROR("error");
//

/**
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class SalsaConnector {

    private DefaultSalsaClient defaultSalsaClient;

    private static final Logger log = LoggerFactory.getLogger(SalsaConnector.class);

    {
        defaultSalsaClient = new DefaultSalsaClient();
        defaultSalsaClient.getConfiguration().setHost("128.130.172.215");

    }


    public DeploymentDescription getServiceDeploymentInfo(String serviceId) {
        DeploymentDescription deploymentInfo = null;

        if (log.isDebugEnabled()) {
            log.debug(Markers.CLIENT, "Getting deployment information for serviceId {}", serviceId);
        }

        SalsaResponse response = defaultSalsaClient.getServiceDeploymentInfo(serviceId);

        String serviceDescription = response.getMessage();
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
        boolean allRunning = true;

        do {
            allRunning = true;

            CloudService service = this.getStatus(serviceID);

            if (!service.getState().equals(SalsaEntityState.RUNNING)) {
                allRunning = false;
            } else {
                for (at.ac.tuwien.dsg.cloud.salsa.common.cloudservice.model.ServiceUnit serviceUnit : service.getAllComponentByType(SalsaEntityType.SOFTWARE)) {
                    if (!serviceUnit.getState().equals(SalsaEntityState.DEPLOYED)) {
                        allRunning = false;
                        break;
                    }
                }
            }

            try {
                Thread.sleep(5000);
                log.warn("Waiting until service " + serviceID + " is running");
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        } while (!allRunning);
    }

    public void deploy(at.ac.tuwien.dsg.comot.common.model.CloudService application) {
        ToscaDescriptionBuilderImpl tdbi = new ToscaDescriptionBuilderImpl();

        String tosca = tdbi.toXml(application);

        {

            try {

                URL url = new URL("http://" + defaultSalsaClient.getConfiguration().getHost() + ":" + defaultSalsaClient.getConfiguration().getPort()
                        + "/salsa-engine/rest/services/xml");

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

                //read error
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
                //read error
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

    public SalsaConnector withDefaultSalsaClient(final DefaultSalsaClient defaultSalsaClient) {
        this.defaultSalsaClient = defaultSalsaClient;
        return this;
    }


}
