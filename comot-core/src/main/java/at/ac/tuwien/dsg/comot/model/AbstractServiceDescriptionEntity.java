package at.ac.tuwien.dsg.comot.model;

import java.util.*;

/**
 * @author omoser
 */
public abstract class AbstractServiceDescriptionEntity extends AbstractCloudEntity implements ServiceDescriptionElement {

    Set<Constraint> constraints = new HashSet<>();

    Set<Strategy> strategies = new HashSet<>();

    Set<Requirement> requirements = new HashSet<>();

    Set<Capability> capabilities = new HashSet<>();

    Map<String, Object> properties = new HashMap<>();

    public AbstractServiceDescriptionEntity(String id) {
        super(id);
    }

    @Override
    public Set<Strategy> getStrategies() {
        return strategies;
    }

    @Override
    public Set<Constraint> getConstraints() {
        return constraints;
    }

    @Override
    public Set<Requirement> getRequirements() {
        return requirements;
    }

    @Override
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    public AbstractServiceDescriptionEntity provides(Capability... capabilities) {
        this.capabilities.addAll(Arrays.asList(capabilities));
        return this;
    }

    public AbstractServiceDescriptionEntity requires(Requirement... requirements) {
        this.requirements.addAll(Arrays.asList(requirements));
        return this;
    }

    public AbstractServiceDescriptionEntity constrainedBy(Constraint... constraints) {
        this.constraints.addAll(Arrays.asList(constraints));
        return this;
    }

    public AbstractServiceDescriptionEntity controlledBy(Strategy... strategies) {
        this.strategies.addAll(Arrays.asList(strategies));
        return this;
    }

    // todo public AbstractServiceDescriptionElement features(Properties...)

}
