package at.ac.tuwien.dsg.comot.m.cs.mapper;

public class IdResolver {

	public static final String SUFFIX_PROPERTY_SALSA = "_property_salsa";
	public static final String SUFFIX_INSTANCE = "_instance_";

	public static String nodeToProperty(String id) {
		return id + SUFFIX_PROPERTY_SALSA;
	}

	public static String uniqueInstance(String id, int nr) {
		return id + SUFFIX_INSTANCE + nr;
	}

	public static String nodeFromInstance(String str) {
		int last = str.lastIndexOf("_");
		String temp = str.substring(0, last - 1);
		String node = temp.substring(0, temp.lastIndexOf("_"));
		return node;
	}

	public static int instanceFromInstance(String str) {
		int last = str.lastIndexOf("_");
		Integer instance = new Integer(str.substring(last + 1));
		return instance;
	}
}
