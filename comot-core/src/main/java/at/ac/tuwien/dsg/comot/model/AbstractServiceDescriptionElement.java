package at.ac.tuwien.dsg.comot.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by omoser on 3/1/14.
 */
public abstract class AbstractServiceDescriptionElement extends AbstractCloudEntity implements ServiceDescriptionElement {

    Set<ServiceConstraint> constraints = new HashSet<>();

    Set<ServiceStrategy> strategies = new HashSet<>();

    @Override
    public Set<ServiceStrategy> getStrategies() {
        return strategies;
    }

    @Override
    public Set<ServiceConstraint> getConstraints() {
        return constraints;
    }


}
