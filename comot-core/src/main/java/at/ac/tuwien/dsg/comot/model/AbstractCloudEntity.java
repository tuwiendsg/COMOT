package at.ac.tuwien.dsg.comot.model;

/**
 * Created by omoser on 3/1/14.
 */
public abstract class AbstractCloudEntity implements CloudEntity {

    String id;

    String description;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public CloudEntity withId(final String id) {
        this.id = id;
        return this;
    }

    public CloudEntity withDescription(final String description) {
        this.description = description;
        return this;
    }


}
