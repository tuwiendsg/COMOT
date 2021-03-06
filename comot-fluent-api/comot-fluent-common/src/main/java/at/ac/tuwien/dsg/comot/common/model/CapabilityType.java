package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public enum CapabilityType {

    Variable("variable");

    final String type;

    CapabilityType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
