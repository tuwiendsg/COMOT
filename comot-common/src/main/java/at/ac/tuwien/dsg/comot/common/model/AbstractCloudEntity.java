package at.ac.tuwien.dsg.comot.common.model;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author omoser
 */
public abstract class AbstractCloudEntity implements CloudEntity {

    String id;

    String description;

    String type;

    String name;

    static Map<String, CloudEntity> context = new ConcurrentHashMap<>();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    AbstractCloudEntity(String id) {
        this.id = id;
        context.put(id, this);
    }

    public AbstractCloudEntity withId(final String id) {
        this.id = id;
        return this;
    }

    public AbstractCloudEntity withDescription(final String description) {
        this.description = description;
        return this;
    }

    public AbstractCloudEntity withName(final String name) {
        this.name = name;
        return this;
    }

    public AbstractCloudEntity ofType(final String type) {
        this.type = type;
        return this;
    }

    public Map<String, CloudEntity> getContext() {
        return ImmutableMap.copyOf(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractCloudEntity)) return false;

        AbstractCloudEntity that = (AbstractCloudEntity) o;

        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AbstractCloudEntity{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
