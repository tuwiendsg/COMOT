package at.ac.tuwien.dsg.comot.model;

import java.util.HashSet;
import java.util.Set;

/**
 * @author omoser
 */
public class OperatingSystemSpecification extends AbstractCloudEntity {

    private String instanceType;

    private String provider;

    private String baseImage;

    private Set<String> packages = new HashSet<>();

    OperatingSystemSpecification(String id) {
        super(id);
    }

    public static OperatingSystemSpecification OperatingSystemSpecification(String id) {
        return new OperatingSystemSpecification(id);
    }

    public OperatingSystemSpecification withPackages(final Set<String> packages) {
        this.packages = packages;
        return this;
    }

    public OperatingSystemSpecification withBaseImage(final String baseImage) {
        this.baseImage = baseImage;
        return this;
    }

    public OperatingSystemSpecification withProvider(final String provider) {
        this.provider = provider;
        return this;
    }

    public OperatingSystemSpecification withInstanceType(final String instanceType) {
        this.instanceType = instanceType;
        return this;
    }

    public OperatingSystemSpecification addSoftwarePackage(String packageName) {
        this.packages.add(packageName);
        return this;
    }


    public String getInstanceType() {
        return instanceType;
    }

    public String getProvider() {
        return provider;
    }

    public String getBaseImage() {
        return baseImage;
    }

    public Set<String> getPackages() {
        return packages;
    }

    @Override
    public OperatingSystemSpecification ofType(String type) {
        return (OperatingSystemSpecification) super.ofType(type);
    }

    @Override
    public OperatingSystemSpecification withType(String type) {
        return (OperatingSystemSpecification) super.ofType(type);
    }

    public OperatingSystemSpecification ofType(ServiceNode.NodeType type) {
        return (OperatingSystemSpecification) super.ofType(type.toString());
    }

    @Override
    public OperatingSystemSpecification withName(String name) {
        return (OperatingSystemSpecification) super.withName(name);
    }

    @Override
    public OperatingSystemSpecification withDescription(String description) {
        return (OperatingSystemSpecification) super.withDescription(description);
    }
}
