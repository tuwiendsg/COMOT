package at.ac.tuwien.dsg.comot.common.model;

import java.util.UUID;

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
    public static OperatingSystemSpecification DockerDefault() {
        return new OperatingSystemSpecification("DockerDefault_" + UUID.randomUUID())
                .withProvider("localhost") // salsa ignore it
                .withInstanceType("000000512")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification LocalDocker() {
        return new OperatingSystemSpecification("LocalDocker_" + UUID.randomUUID())
                .withProvider("localhost");
    }

    public static OperatingSystemSpecification OpenstackTiny() {
        return new OperatingSystemSpecification("OpenstackTiny" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("000000512") //.withInstanceType("m1.small")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c"); //.withBaseImage("ami-00000163");
    }

    public static OperatingSystemSpecification OpenstackMicro() {
        return new OperatingSystemSpecification("OpenstackMicro" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("000000960") //.withInstanceType("m1.small")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c"); //.withBaseImage("ami-00000163");
    }

    public static OperatingSystemSpecification OpenstackSmall() {
        return new OperatingSystemSpecification("OpenstackSmall" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("000001920") //.withInstanceType("m1.small")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c"); //.withBaseImage("ami-00000163");
    }

    public static OperatingSystemSpecification OpenstackMedium() {
        return new OperatingSystemSpecification("OpenstackMedium" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("000003750")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackM2Medium() {
        return new OperatingSystemSpecification("OpenstackM2Medium" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("000003750")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackM1Large() {
        return new OperatingSystemSpecification("OpenstackM1Large" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("000007680")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackW1Tiny() {
        return new OperatingSystemSpecification("OpenstackW1Tiny" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("900000960")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackW1Small() {
        return new OperatingSystemSpecification("OpenstackW1Small" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("900001920")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackW1Medium() {
        return new OperatingSystemSpecification("OpenstackW1Medium" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("900003750")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackW1Large() {
        return new OperatingSystemSpecification("OpenstackW1Large" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("900007680")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }

    public static OperatingSystemSpecification OpenstackW1XLarge() {
        return new OperatingSystemSpecification("OpenstackW1XLarge" + UUID.randomUUID())
                .withProvider("dsg@openstack")
                .withInstanceType("900015360")
                .withBaseImage("a82e054f-4f01-49f9-bc4c-77a98045739c");
    }


    public static OperatingSystemSpecification FlexiantMicro() {
        return new OperatingSystemSpecification("FlexiantSmall" + UUID.randomUUID())
                .withProvider("celar@flexiant")
                .withInstanceType("1/512");
    }

    public static OperatingSystemSpecification FlexiantSmall() {
        return new OperatingSystemSpecification("FlexiantSmall" + UUID.randomUUID())
                .withProvider("celar@flexiant")
                .withInstanceType("1/1024");
    }

}
