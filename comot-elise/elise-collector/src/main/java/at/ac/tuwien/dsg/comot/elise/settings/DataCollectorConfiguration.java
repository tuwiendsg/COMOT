package at.ac.tuwien.dsg.comot.elise.settings;

public class DataCollectorConfiguration {
    // Other configuration, e.g configure file for cloud credentials
    public final static String CURRENT_DIR = System.getProperty("user.dir");
    public final static String COLLECTOR_ADAPTOR_CONFIG_FILE = CURRENT_DIR+"/adaptor.conf";    
    public final static String ELISE_COLLECTOR_CONFIG_FILE = CURRENT_DIR + "/elise.conf";
}
