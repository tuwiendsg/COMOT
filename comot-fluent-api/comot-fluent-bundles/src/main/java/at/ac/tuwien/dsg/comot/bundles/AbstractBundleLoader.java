package at.ac.tuwien.dsg.comot.bundles;

import at.ac.tuwien.dsg.comot.common.model.BundleConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author omoser
 */
public abstract class AbstractBundleLoader implements BundleLoader {

    protected AtomicBoolean cacheInitialized = new AtomicBoolean();

    protected Map<String, BundleConfig> configs = new HashMap<>();

    @Override
    public BundleConfig getBundleConfig(String bundleId) {
        checkInitState();

        if (configs.containsKey(bundleId)) {
            return configs.get(bundleId);
        }

        throw new IllegalArgumentException("No such bundle: " + bundleId);
    }

    private void checkInitState() {
        if (!cacheInitialized.get()) {
            throw new IllegalStateException("Bundle chage not yet initialized. Did you forget to call init()?");
        }
    }

    @Override
    public boolean isAvailable(String bundleId) {
        checkInitState();
        return configs.containsKey(bundleId);
    }

    @Override
    public void init() throws Exception {
        if (!cacheInitialized.getAndSet(true)) {
            initBundleCache();
        }
    }

    public abstract void initBundleCache() throws Exception;
}
