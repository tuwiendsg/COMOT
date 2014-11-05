package at.ac.tuwien.dsg.comot.bundles;

import at.ac.tuwien.dsg.comot.common.fluent.BundleConfig;

/**
 * @author omoser
 */
public interface BundleLoader {

    BundleConfig getBundleConfig(String bundleId);

    boolean isAvailable(String bundleId);

    void init() throws Exception;
}
