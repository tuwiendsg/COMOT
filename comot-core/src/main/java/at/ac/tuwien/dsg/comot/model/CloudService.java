package at.ac.tuwien.dsg.comot.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by omoser on 3/1/14.
 */
public class CloudService extends AbstractServiceDescriptionElement {

    private Set<ServiceTopolgy> components = new HashSet<>();

    public Set<ServiceTopolgy> components() {
        return this.components;
    }

    public CloudService withComponents(final Set<ServiceTopolgy> components) {
        this.components = components;
        return this;
    }

    public CloudService addComponent(final ServiceTopolgy component) {
        components.add(component);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloudService)) return false;

        CloudService that = (CloudService) o;

        if (components != null ? !components.equals(that.components) : that.components != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (components != null ? components.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CloudService{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", components=" + components +
                '}';
    }
}
