package at.ac.tuwien.dsg.comot.model;

import java.util.Set;

/**
 * Created by omoser on 3/1/14.
 */
public interface ServiceDescriptionElement extends CloudEntity {

    Set<ServiceStrategy> getStrategies();

    Set<ServiceConstraint> getConstraints();

}