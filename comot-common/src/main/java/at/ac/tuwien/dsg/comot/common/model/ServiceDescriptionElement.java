package at.ac.tuwien.dsg.comot.common.model;

import java.util.Map;
import java.util.Set;

/**
 * Created by omoser on 3/1/14.
 */
public interface ServiceDescriptionElement extends CloudEntity {

    Set<Strategy> getStrategies();

    Set<Constraint> getConstraints();

    Set<Requirement> getRequirements();

    Set<Capability> getCapabilities();
    
    Set<ElasticityCapability> getElasticityCapabilities();

    
    Map<String, Object> getProperties();
    
    


}