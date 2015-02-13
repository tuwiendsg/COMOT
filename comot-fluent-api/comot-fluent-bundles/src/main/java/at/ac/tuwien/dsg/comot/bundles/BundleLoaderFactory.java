package at.ac.tuwien.dsg.comot.bundles;

/**
 * @author omoser
 */
public class BundleLoaderFactory {

    public static BundleLoader getDefaultBundleLoader() {
        String property = System.getProperty("comot.bundleloader", "JsonBundleLoader");
        switch (property) {
            case "JsonBundleLoader" : return JsonBundleLoader.getInstance();
            case "YamlBundleLoader" : return YamlBundleLoader.getInstance();
            default: return JsonBundleLoader.getInstance();
        }
    }
}
