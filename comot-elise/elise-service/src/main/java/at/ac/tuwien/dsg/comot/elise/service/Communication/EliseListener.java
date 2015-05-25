/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.Communication;

import at.ac.tuwien.dsg.comot.elise.common.EliseManager.EliseManager;
import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorInterface.MessageSubscribeInterface;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorKafka.KafkaSubscribe;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT.MQTTPublish;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT.MQTTSubscribe;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * This class listen to the request from other elise and execute the command
 *
 * @author hungld
 *
 */
public class EliseListener {

    static Logger logger = EliseConfiguration.logger;
    String eliseRestLocal = EliseConfiguration.getRESTEndpointLocal();

    @PostConstruct
    public void init() {
        logger.debug("Subscribing to the control topic : at.ac.tuwien.dsg.elise.query");
        MessageSubscribeInterface subscriber = new MQTTSubscribe() {
            Map<String, String> answeredElises = new HashMap();

            @Override
            public void handleMessage(EliseMessage message) {
                String fromElise = message.getFromElise();
                String feedbackTopic = message.getFeedbackTopic();
                logger.debug("Retrieve the request from ELISE: " + fromElise + ", feedback topic: " + feedbackTopic);
                if (feedbackTopic.equals(this.answeredElises.get(fromElise))) {
                    logger.debug("Neglect duplicated subscribing message from ELISE: " + fromElise + ", topic: " + feedbackTopic);
                    return;
                }
                this.answeredElises.put(fromElise, feedbackTopic);

                logger.debug("Got a control message from other ELISE, checking local service health: " + checkLocalHealth());

                logger.debug("Querying to local service, response time can be long ...");
                String response;
                switch (message.getCommand()) {
                    case discover:
                        response = "healthy";
                        break;
                    case queryInstance:
                        response = queryAllServiceInstanceOfLocalELISE(message.getPayload());
                        break;
                    case queryProvider:
                        response = "{\"message\":\"Not support query provider yet\"}";
                        break;
                    case answer:
                        return;
                    default:
                        response = "{\"error\":\"Unknown Elise command !\"}";
                        break;

                }
                MQTTPublish publisher = new MQTTPublish();
                logger.debug("Pushing answer data...");
                EliseMessage resMsg = new EliseMessage(EliseMessage.COMMAND.answer, EliseConfiguration.getEliseID(), feedbackTopic, null, response);
                publisher.pushMessageAndDisconnect(resMsg);
            }
        };
        subscriber.subscribe(EliseQueueTopic.QUERY_TOPIC);
    }

    // query
    private String queryAllServiceInstanceOfLocalELISE(String query) {
        logger.debug("Querying to local ELISE: " + EliseConfiguration.getRESTEndpointLocal());
//        ObjectMapper mapper = new ObjectMapper();
//        JavaType javaType = mapper.getTypeFactory().constructCollectionLikeType(HashSet.class, UnitInstance.class);
//        UnitInstanceDAO unitInstanceDAO = (UnitInstanceDAO) JAXRSClientFactory.create(EliseConfiguration.getRESTEndpointLocal(), HashSet.class, Collections.singletonList(new JacksonJsonProvider()));
//        
//        Set<UnitInstance> instances = unitInstanceDAO.queryUnitInstance(EliseQuery.fromJson(query));
//        logger.debug("Querying to local ELISE: " + EliseConfiguration.getRESTEndpointLocal() + ". instance num: " + instances.size());
//        ObjectMapper mapper = new ObjectMapper();
//
//        try {
//            return mapper.writeValueAsString(instances);
//
//        } catch (IOException ex) {
//            return null;
//        }

        String queryURL = EliseConfiguration.getRESTEndpointLocal()+"/unitinstance/query";
        WebClient client = WebClient.create(queryURL);
        logger.debug("Querying to local REST: " + queryURL);

        HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();

        conduit.getClient().setReceiveTimeout(180000L);
        conduit.getClient().setConnectionTimeout(30000L);
        return (String) client.accept(new String[]{"application/json"}).type("application/json").post(query,String.class);
    }

    private String checkLocalHealth() {
        EliseManager elise = (EliseManager) JAXRSClientFactory.create(EliseConfiguration.getRESTEndpointLocal(), EliseManager.class, Collections.singletonList(new JacksonJsonProvider()));
        return elise.health();
    }

}
