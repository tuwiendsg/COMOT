package at.ac.tuwien.dsg.comot.elise.settings;

import java.util.UUID;
import org.apache.log4j.Logger;

public class DataCollectorConfiguration {

    static Logger logger = Logger.getLogger(DataCollectorConfiguration.class);
    public static final String CURRENT_DIR = System.getProperty("user.dir");
    public static final String COLLECTOR_ADAPTOR_CONFIG_FILE = CURRENT_DIR + "/adaptor.conf";
    public static final String ELISE_COLLECTOR_CONFIG_FILE = CURRENT_DIR + "/elise.conf";
    static final String p_name = "name";
    static final String p_ip = "ELISE_IP";
    static final String p_port = "ELISE_PORT";    
    static final String p_collectorID = "collectorID";

    public static String getName() {
        return getOrGenerate(p_name);
    }

    public static String getELISE_IP() {
        return getOrGenerate(p_ip);
    }

    public static String getELISE_port() {
        return getOrGenerate(p_port);
    }

    public static String getELISE_collectorID() {
        return getOrGenerate("collectorID");
    }

    public static String getELISE_REST_ENDPOINT() {
        return "http://" + getELISE_IP() + ":" + getELISE_port() + "/elise-service/rest";
    }
    
    public static String getELISE_REST_ENDPOINT_LOCAL() {
        return "http://localhost:" + getELISE_port() + "/elise-service/rest";
    }

    public static String getOrGenerate(String key) {
        String value = PropertiesManager.getParameter(key, ELISE_COLLECTOR_CONFIG_FILE);
        if ((value == null) || (value.isEmpty())) {
            logger.debug("There is no value for key: " + key + ". Generating one...");
            switch (key) {
                case p_name:
                    value = "unknown";
                    break;
                case p_ip:
                    value = "localhost";
                    break;
                case p_port:
                    value = "8480";
                    break;
                case p_collectorID:
                    value = UUID.randomUUID().toString();
                    break;
                case "entityUUID":
                    logger.error("Cannot get service unit UUID. Collector must query to ELISE service first to update the composed identification and get the UUID !");
                    break;
                default:
                    value = null;
            }
            logger.debug("Generated: " + key + "=" + value);
            PropertiesManager.saveParameter(key, value, ELISE_COLLECTOR_CONFIG_FILE);
        }
        return value;
    }
}
