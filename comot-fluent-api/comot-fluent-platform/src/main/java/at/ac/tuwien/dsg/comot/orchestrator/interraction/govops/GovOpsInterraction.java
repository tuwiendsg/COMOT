/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.orchestrator.interraction.govops;

import at.ac.tuwien.dsg.comot.common.model.ServiceUnit;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Moldovan E-Mail: d.moldovan@dsg.tuwien.ac.at
 */
public class GovOpsInterraction {

    private static final Logger log = LoggerFactory.getLogger(GovOpsInterraction.class);

    private String GOvOps_BASE_IP = "128.130.172.199";
    private Integer GOvOps_BASE_PORT = 8080;
    private String GOvOps_BASE_URL = "/APIManager";

    /**
     *
     * Show global scope: http://url:8080/APIManager/governanceScope/globalScope
     *
     * Start/stop sensor based on governance scope (in this case only sensors on
     * type FM5300)
     * http://url:8080/APIManager/governanceScope/invokeScope/type=FM5300/cStartStopSensor/start
     * http://url:8080/APIManager/governanceScope/invokeScope/type=FM5300/cStartStopSensor/stop
     *
     * Same as above, but directly reference a specific gateway
     * http://url:8080/APIManager/mapper/invoke/10.99.0.102:9080/cStartStopSensor/start
     *
     *
     * Set sensor update rate (mqtt push in seconds):
     * http://url:8080/APIManager/governanceScope/invokeScope/type=FM5300/cChangeSensorRate/update?args=10
     *
     */
    public void enforceCapabilityOnSingleUnit(ServiceUnit unit, String capabilityName) {

        HttpHost endpoint = new HttpHost(GOvOps_BASE_IP, GOvOps_BASE_PORT);

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI prepareConfigURI = UriBuilder.fromPath(GOvOps_BASE_URL + "/mapper/invoke/" + unit.getId() + "/" + capabilityName).build();
            HttpGet prepareConfig = new HttpGet(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public void enforceCapabilityOnClassOfUnits(ServiceUnit unit, String capabilityName) {

        HttpHost endpoint = new HttpHost(GOvOps_BASE_IP, GOvOps_BASE_PORT);

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI prepareConfigURI = UriBuilder.fromPath(GOvOps_BASE_URL + "/governanceScope/invokeScope/type=" + unit.getType() + "/" + capabilityName).build();
            HttpGet prepareConfig = new HttpGet(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public void enforceCapabilityOnSingleUnit(ServiceUnit unit, String capabilityName, String args) {

        HttpHost endpoint = new HttpHost(GOvOps_BASE_IP, GOvOps_BASE_PORT);

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI prepareConfigURI = UriBuilder.fromPath(GOvOps_BASE_URL + "/mapper/invoke/" + unit.getId() + "/" + capabilityName
                    + "?args=" + args).build();
            HttpGet prepareConfig = new HttpGet(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public void enforceCapabilityOnClassOfUnits(ServiceUnit unit, String capabilityName, String args) {

        HttpHost endpoint = new HttpHost(GOvOps_BASE_IP, GOvOps_BASE_PORT);

        {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            URI prepareConfigURI = UriBuilder.fromPath(GOvOps_BASE_URL
                    + "/governanceScope/invokeScope/type=" + unit.getType() + "/" + capabilityName
                    + "?args=" + args
            ).build();
            HttpGet prepareConfig = new HttpGet(prepareConfigURI);

            try {
                HttpResponse httpResponse = httpClient.execute(endpoint, prepareConfig);
                EntityUtils.consume(httpResponse.getEntity());

            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public void setIp(String GOvOps_BASE_IP) {
        this.GOvOps_BASE_IP = GOvOps_BASE_IP;
    }

    public void setPort(Integer GOvOps_BASE_PORT) {
        this.GOvOps_BASE_PORT = GOvOps_BASE_PORT;
    }

    public void setBaseURI(String GOvOps_BASE_URL) {
        this.GOvOps_BASE_URL = GOvOps_BASE_URL;
    }

    

}
