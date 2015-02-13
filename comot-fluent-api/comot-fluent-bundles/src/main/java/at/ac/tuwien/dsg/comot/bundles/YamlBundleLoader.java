package at.ac.tuwien.dsg.comot.bundles;

import at.ac.tuwien.dsg.comot.common.model.BundleConfig;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;

/**
 * @author omoser
 */
public class YamlBundleLoader extends AbstractBundleLoader implements BundleLoader {

    public static final String DEFAULT_BUNDLE_CONFIG = "bundle-configs.yaml";

    private static final BundleLoader INSTANCE = new YamlBundleLoader();

    private YamlBundleLoader() {
    }

    public static BundleLoader getInstance() {
        return INSTANCE;
    }

    public void initBundleCache() throws IOException {
        Iterable bundles = new Yaml(new Constructor(BundleConfig.class))
                .loadAll(new ClassPathResource(DEFAULT_BUNDLE_CONFIG).getInputStream());

        for (BundleConfig bundleConfig : (Iterable<BundleConfig>) bundles) {
            configs.put(bundleConfig.getId(), bundleConfig);
        }
    }
}
