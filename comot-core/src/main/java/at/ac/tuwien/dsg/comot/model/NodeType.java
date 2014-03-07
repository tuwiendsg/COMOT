package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public enum NodeType {

    OperatingSystem("OPERATING_SYSTEM"),
    Software("software");

    final String type;

    NodeType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
