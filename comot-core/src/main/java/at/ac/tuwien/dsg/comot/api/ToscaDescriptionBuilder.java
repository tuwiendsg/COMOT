package at.ac.tuwien.dsg.comot.api;

//import at.ac.tuwien.dsg.comot.common.model.CloudService;
import at.ac.tuwien.dsg.comot.common.model.CloudService;
import org.oasis.tosca.TDefinitions;

/**
 * @author omoser
 */
public interface ToscaDescriptionBuilder {

    TDefinitions buildToscaDefinitions(CloudService cloudService) throws ToscaDescriptionBuilderException;

    String toXml(CloudService cloudService) throws ToscaDescriptionBuilderException;

    void setValidating(boolean validating);

}
