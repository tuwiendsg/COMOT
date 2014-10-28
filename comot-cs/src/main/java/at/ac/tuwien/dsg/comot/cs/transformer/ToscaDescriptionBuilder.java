package at.ac.tuwien.dsg.comot.cs.transformer;

//import at.ac.tuwien.dsg.comot.common.model.CloudService;
import org.oasis.tosca.TDefinitions;

import at.ac.tuwien.dsg.comot.common.model.CloudService;

/**
 * @author omoser
 */
public interface ToscaDescriptionBuilder {

    TDefinitions buildToscaDefinitions(CloudService cloudService) throws ToscaDescriptionBuilderException;

    String toXml(CloudService cloudService) throws ToscaDescriptionBuilderException;

    void setValidating(boolean validating);

}
