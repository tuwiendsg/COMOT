package at.ac.tuwien.dsg.comot.elise.service.settings;

public class EliseConfiguration {

    // the case of using embbeded DB
    public final static String ELISE_HOME = "/opt/comotElise";
    public final static String DATA_BASE_STORAGE = ELISE_HOME + "/comotElise.db";    

    // in the case of using separate DB
    public final static String DATA_BASE_REMOTE_ENDPOINT = "http://localhost:7474/db/data";
}
