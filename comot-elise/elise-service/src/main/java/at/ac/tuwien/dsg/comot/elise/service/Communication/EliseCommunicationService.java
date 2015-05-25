/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.comot.elise.service.Communication;

import at.ac.tuwien.dsg.comot.elise.common.EliseManager.EliseManager;
import at.ac.tuwien.dsg.comot.elise.common.DAOInterface.UnitInstanceDAO;
import at.ac.tuwien.dsg.comot.elise.common.message.EliseMessage;
import at.ac.tuwien.dsg.comot.elise.common.message.EliseQuery;
import at.ac.tuwien.dsg.comot.elise.common.message.EliseQueryRule;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT.MQTTPublish;
import at.ac.tuwien.dsg.comot.elise.service.QueueAdaptorMQTT.MQTTSubscribe;
import at.ac.tuwien.dsg.comot.elise.service.utils.EliseConfiguration;
import at.ac.tuwien.dsg.comot.model.elasticunit.generic.MetricValue;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.ServiceInstance;
import at.ac.tuwien.dsg.comot.model.elasticunit.runtime.UnitInstance;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.parboiled.common.StringUtils;

/**
 *
 * @author hungld
 */
@Path("/communication")
public class EliseCommunicationService {

    static Logger logger = EliseConfiguration.logger;
    static String listenerTopic = "at.ac.tuwien.dsg.comot.elise.listener";

    static int eliseCounter = 0;

    @GET
    @Path("/count")
    public int count() {
        String uuid = UUID.randomUUID().toString();
        eliseCounter = 0;
        MQTTSubscribe sub = new MQTTSubscribe() {
            @Override
            public void handleMessage(EliseMessage paramEliseMessage) {
                increateEliseCounter();
                logger.debug("Get a response from ELISE. Counter: " + eliseCounter);
            }
        };

        sub.subscribe(EliseQueueTopic.getFeedBackTopic(uuid));
        MQTTPublish pub = new MQTTPublish();
        logger.debug("Pub: " + pub.genClientID());

        pub.pushMessage(new EliseMessage(EliseMessage.COMMAND.discover, EliseConfiguration.getEliseID(), EliseQueueTopic.QUERY_TOPIC, EliseQueueTopic.getFeedBackTopic(uuid), null));

        try {
            Thread.sleep(5000L);        // wait 5 secs for other elises answer
        } catch (InterruptedException ex) {
            this.logger.debug(ex.getMessage());
        }
        this.logger.debug("Found " + eliseCounter + " ELISE(s)");
        return eliseCounter;
    }

    static long startTime;
    int count;
    String rtLogFile;

    @POST
    @Path("/queryUnitInstance")
    @Consumes(MediaType.APPLICATION_JSON)
    public String querySetOfInstance(EliseQuery query,
            @DefaultValue("false") @QueryParam("isUpdated") final boolean isUpdated, // the information will be update continuously
            @DefaultValue("false") @QueryParam("notify") final boolean isNotified) {  // the change will be notify to the topic: at.ac.tuwien.dsg.elise.notification
        logger.debug("Broadcast a query to gather instances... Query: " + query.toString());
        String uuid = UUID.randomUUID().toString();
        logger.debug("UUID of the request: " + uuid);

        // clean local DB
        EliseManager eliseDB = (EliseManager) JAXRSClientFactory.create(EliseConfiguration.getRESTEndpointLocal(), EliseManager.class, Collections.singletonList(new JacksonJsonProvider()));
        eliseDB.cleanDB();

        count = 0;
        rtLogFile = ("log/rt_log_" + uuid);
        startTime = Calendar.getInstance().getTimeInMillis();

        MQTTSubscribe sub = new MQTTSubscribe() {
            Map<String, String> answeredElises = new HashMap();

            @Override
            public void handleMessage(EliseMessage message) {
                String fromElise = message.getFromElise();
                String fromTopic = message.getTopic();
                long originTime = message.getTimeStamp();

                String jsonHeader = "Count, FromElise, responseTime, updateTime \n";
                //increateCountAndWriteData(jsonHeader);

                logger.debug("Retrieve the answer from ELISE: " + fromElise + ", topic: " + fromTopic + ", orginial timestamp: " + originTime);
                if (fromTopic != null) {
                    if (fromTopic.equals(this.answeredElises.get(fromElise))) {
                        logger.debug("Duplicate subscribing message from ELISE: " + fromElise + ", topic: " + fromTopic);
                        return;
                    }
                    this.answeredElises.put(fromElise, fromTopic);
                }
                ObjectMapper mapper = new ObjectMapper();
                JavaType javatype = mapper.getTypeFactory().constructCollectionType(Set.class, UnitInstance.class);
                try {
                    Set<UnitInstance> uis = mapper.readValue(message.getPayload(), javatype);
                    logger.debug("Recieved " + uis.size() + " unitinstance. Saving....");
                    UnitInstanceDAO unitInstanceDAO = (UnitInstanceDAO) JAXRSClientFactory.create(EliseConfiguration.getRESTEndpointLocal(), UnitInstanceDAO.class, Collections.singletonList(new JacksonJsonProvider()));
                    for (UnitInstance u : uis) {
                        unitInstanceDAO.addUnitInstance(u);
                    }
                    long now = Calendar.getInstance().getTimeInMillis();
                    long responseTime = now - startTime;
                    long updateTime = now - message.getTimeStamp();

                    String jsonLine = padLeft(count+"",3) + "," 
                                    + padLeft(message.getFromElise(),20) + "," 
                                    + padLeft(responseTime+"",7) + "," 
                                    + updateTime + "\n";
                    
                    //String jsonLine = count + "," + message.getFromElise() + "," + responseTime + "," + updateTime + "\n";

                    logger.debug("Adding done in: " + responseTime + " ms");
                    increateCountAndWriteData(jsonLine);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(EliseCommunicationService.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (isNotified) {
                    MQTTPublish pub = new MQTTPublish();
                    // this is the response, so its "fromTopic" is the feedback topic of the query
                    pub.pushCustomData(buildNotification(fromTopic, originTime), EliseQueueTopic.NOTIFICATION_TOPIC);

//                    long currentTimeStamp = System.currentTimeMillis();
//                    long derivation = currentTimeStamp - originTime;                    
                }

            }
        };

        logger.debug("Subscribing the topic: " + EliseQueueTopic.getFeedBackTopic(uuid));
        sub.subscribe(EliseQueueTopic.getFeedBackTopic(uuid));
        logger.debug("Subscribe to feedback topic done, now push request ...");
        MQTTPublish pub = new MQTTPublish();
        logger.debug("Pub: " + pub.genClientID());
        pub.pushMessage(new EliseMessage(EliseMessage.COMMAND.queryInstance, EliseConfiguration.getEliseID(), EliseQueueTopic.QUERY_TOPIC, EliseQueueTopic.getFeedBackTopic(uuid), query.toJson()));
        logger.debug("Push message done, just waiting for the message ...");

        try {
            Thread.sleep(15000);    // for BLOCKING call, after 15 sec, return the result
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // save unit instance
        UnitInstanceDAO unitInstanceDAO = (UnitInstanceDAO) JAXRSClientFactory.create(EliseConfiguration.getRESTEndpointLocal(), UnitInstanceDAO.class, Collections.singletonList(new JacksonJsonProvider()));
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(filterInstance(unitInstanceDAO.getUnitInstanceList(), query));
        } catch (IOException ex) {
            logger.error("Cannot convert the result to Json to send. ");
            ex.printStackTrace();
            return null;
        }

    }

    private String buildNotification(String feedbackTopic, long timeStamp) {
        Date date = new Date(timeStamp);
        return feedbackTopic + "," + timeStamp + "," + date.toString();
    }

    public String health() {
        return "Listening";
    }

    private synchronized void increateCountAndWriteData(String line) {
        try {
            File file = new File(this.rtLogFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWritter = new FileWriter(file.getName(), true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(line);
            bufferWritter.close();
            this.count += 1;
        } catch (IOException e) {
            this.logger.error("Cannot create log file for response time !");
        }
    }

    private synchronized void increateEliseCounter() {
        eliseCounter += 1;
    }

    private Set<UnitInstance> filterInstance(Set<UnitInstance> instances, EliseQuery query) {
        Set<UnitInstance> filtered = new HashSet<>();

        logger.debug("Filter " + instances.size() + " of the category: " + query.getCategory().toString());
        int rulefulfill = 0;    // 0: N/A, 1: fulfill, -1: violate
        for (UnitInstance u : instances) {
            logger.debug("Checking instance: " + u.getId() + "/" + u.getName());
            for (MetricValue value : u.findAllMetricValues()) {
                for (EliseQueryRule rule : query.getRules()) {
                    logger.debug("Comparing unit(" + value.getName() + "=" + value.getValue() + " with the rule " + rule.toString());
                    if (value.getName().equals(rule.getMetric())) {  // if the metric name is match
                        if (rule.isFulfilled(value.getValue())) {    // check if value is fulfill
                            logger.debug("One rule fulfilled !");
                            rulefulfill += 1;                           // add one to the counting of fulfilled value
                        } else {
                            logger.debug("A rule is violated ! BREAK !");
                            rulefulfill -= 1;                           // or reduce it and break as a rule is violated
                            break;
                        }
                    }
                }
                // if all the condition is fulfill, fulfill = rule.size()                
                if (rulefulfill == query.getRules().size()) {           // if rules are fulfilled
                    logger.debug("Fullfill all rules, now checking unit instance if fullfill. Fulfilled count: " + rulefulfill + " with no. of rules: " + query.getRules().size());
                    Set<String> capas = u.findAllPrimitiveNames();       // also check capability
                    if (capas.containsAll(query.getHasCapabilities())) {
                        filtered.add(u);
                    }
                }
            }

        }
        return filtered;
    }

    public static String padLeft(String s, int n) {        
        return String.format("%1$" + n + "s", s);
    }
}
