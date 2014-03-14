package at.ac.tuwien.dsg.comot.model;

/**
 * @author omoser
 */
public final class CommonOperatingSystemSpecification {

    public static OperatingSystemSpecification OpenstackSmall(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("m1.small")
                .withBaseImage("ami-00000163");
    }

    public static OperatingSystemSpecification OpenstackMedium(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("m1.medium")
                .withBaseImage("ami-00000163"); // todo set correct base image for medium
    }

    public static OperatingSystemSpecification OpenstackLarge(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("m1.large")
                .withBaseImage("ami-00000163"); // todo set correct base image for large
    }
}
