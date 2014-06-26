package at.ac.tuwien.dsg.comot.common.model;

/**
 * @author omoser
 */
public final class CommonOperatingSystemSpecification {
   
    
//000000512 m1.tiny
//000000960 m1.micro
//000001920 m1.small
//000003750 m1.medium
//000005760 m2.medium
//000007680 m1.large
//000015360 m1.xlarge
//000030720 m1.2xlarge
//900000960 w1.tiny
//900001920 w1.small
//900003750 w1.medium
//900007680 w1.large
//900015360 w1.xlarge
    
    public static OperatingSystemSpecification OpenstackTiny(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("000000512") //.withInstanceType("m1.small")
                .withBaseImage("8f1428ac-f239-42e0-ab35-137f6e234101"); //.withBaseImage("ami-00000163");
    }
    public static OperatingSystemSpecification OpenstackMicro(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("000000960") //.withInstanceType("m1.small")
                .withBaseImage("8f1428ac-f239-42e0-ab35-137f6e234101"); //.withBaseImage("ami-00000163");
    }
    public static OperatingSystemSpecification OpenstackSmall(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("000001920") //.withInstanceType("m1.small")
                .withBaseImage("8f1428ac-f239-42e0-ab35-137f6e234101"); //.withBaseImage("ami-00000163");
    }

    public static OperatingSystemSpecification OpenstackMedium(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("000003750")
                .withBaseImage("8f1428ac-f239-42e0-ab35-137f6e234101"); // todo set correct base image for medium
    }

    public static OperatingSystemSpecification OpenstackLarge(String id) {
        return new OperatingSystemSpecification(id)
                .withInstanceType("m1.large")
                .withBaseImage("ami-00000163"); // todo set correct base image for large
    }
}
