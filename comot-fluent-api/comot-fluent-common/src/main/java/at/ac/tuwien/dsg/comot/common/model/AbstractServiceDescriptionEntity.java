package at.ac.tuwien.dsg.comot.common.model;

import java.util.*;

/**
 * @author omoser
 */
public abstract class AbstractServiceDescriptionEntity extends AbstractCloudEntity implements ServiceDescriptionElement {

    Set<Constraint> constraints = new HashSet<>();

    Set<Strategy> strategies = new HashSet<>();

    Set<Requirement> requirements = new HashSet<>();

    Set<Capability> capabilities = new HashSet<>();

    Set<ElasticityCapability> elasticityCapabilities = new HashSet<>();

    Map<LifecyclePhase, AbstractLifecycleAction> lifecycleActions = new HashMap<>();

//    Map<String, Object> properties = new HashMap<>();
    AbstractServiceDescriptionEntity(String id) {
        super(id);
    }

    public boolean hasConstraints() {
        return !constraints.isEmpty();
    }

    public boolean hasRequirements() {
        return !requirements.isEmpty();
    }

    public boolean hasCapabilities() {
        return !capabilities.isEmpty();
    }

    public boolean hasStrategies() {
        return !strategies.isEmpty();
    }

    public boolean hasElasticityCapabilities() {
        return !elasticityCapabilities.isEmpty();
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
    public Set<ElasticityCapability> getElasticityCapabilities() {
        return elasticityCapabilities;
    }

    @Override
    public Map<LifecyclePhase, AbstractLifecycleAction> getLifecycleActions() {
        return lifecycleActions;
    }

    public AbstractServiceDescriptionEntity withLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.put(phase, action);
        return this;
    }

    public AbstractServiceDescriptionEntity removeLifecycleAction(LifecyclePhase phase, AbstractLifecycleAction action) {
        lifecycleActions.remove(phase);
        return this;
    }

    @Override
    public AbstractLifecycleAction getLifecycleAction(LifecyclePhase phase) {
        return lifecycleActions.get(phase);
    }

//    @Override
//    public Map<String, Object> getProperties() {
//        return properties;
//    }
    public AbstractServiceDescriptionEntity exposes(Capability... capabilities) {
        this.capabilities.addAll(Arrays.asList(capabilities));
        return this;
    }

    public AbstractServiceDescriptionEntity provides(ElasticityCapability... capabilities) {
        this.elasticityCapabilities.addAll(Arrays.asList(capabilities));
        return this;
    }

    public AbstractServiceDescriptionEntity requires(Requirement... requirements) {
        this.requirements.addAll(Arrays.asList(requirements));
        return this;
    }

    public AbstractServiceDescriptionEntity constrainedBy(Constraint... constraints) {
        Set<Constraint> con = new HashSet<Constraint>();
        con.addAll(Arrays.asList(constraints));
        for (Constraint c : this.constraints){
           if (!con.contains(c)){
              con.add(c);
           }
        }
        this.constraints=con;
        return this;
    }

    public AbstractServiceDescriptionEntity controlledBy(Strategy... strategies) {
        Set<Strategy> str = new HashSet<Strategy>();
        str.addAll(Arrays.asList(strategies));
        for (Strategy s : this.strategies){
           if (!str.contains(s)){
              str.add(s);
           }
        }
        this.strategies=str;
        return this;
    }

}
