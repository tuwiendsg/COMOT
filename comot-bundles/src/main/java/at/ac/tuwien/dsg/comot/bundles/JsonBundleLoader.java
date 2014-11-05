package at.ac.tuwien.dsg.comot.bundles;

import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.core.io.ClassPathResource;

import java.util.Iterator;

/**
 * @author omoser
 */
public class JsonBundleLoader extends AbstractBundleLoader implements BundleLoader {

    public static final String DEFAULT_BUNDLE_CONFIG = "bundle-configs.json";

    private static final BundleLoader INSTANCE = new JsonBundleLoader();

    private ObjectMapper mapper = new ObjectMapper();

    private JsonBundleLoader() {
    }

    public static BundleLoader getInstance() {
        return INSTANCE;
    }


    @Override
    public void initBundleCache() throws Exception {
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Iterator<BundleConfig> bundleConfigs = mapper.reader(BundleConfig.class)
                .readValues(new ClassPathResource(DEFAULT_BUNDLE_CONFIG).getInputStream());

        while (bundleConfigs.hasNext()) {
            BundleConfig config = bundleConfigs.next();
            configs.put(config.getId(), config);
        }

    }
}
