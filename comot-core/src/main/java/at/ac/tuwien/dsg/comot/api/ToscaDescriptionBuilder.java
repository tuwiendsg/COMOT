package at.ac.tuwien.dsg.comot.api;

import at.ac.tuwien.dsg.comot.common.model.CloudApplication;
import org.oasis.tosca.TDefinitions;

/**
 * @author omoser
 */
public interface ToscaDescriptionBuilder {

    TDefinitions buildToscaDefinitions(CloudApplication application) throws Exception;

    String toXml(CloudApplication application) throws Exception;


}
