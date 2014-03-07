package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public class Requirement extends AbstractCloudEntity {

    Requirement(String id) {
        super(id);
        context.put(id, this);
    }

    public static Requirement Requirement(String id) {
        return new Requirement(id);
    }
}
