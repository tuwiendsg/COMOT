package at.ac.tuwien.dsg.comot.m.cs.mapper;

public class IdResolver {

	public static final String SUFFIX_OSU = "_OSU";
	public static final String SUFFIX_OSU_INSTANCE = "_OSU_INSTANCE";

	public static String uniqueInstance(String csInstanceId, String unitId, int nr) {
		return csInstanceId + "_" + unitId + "_" + nr;
	}

	public static String osuFromUnit(String id) {
		return id + SUFFIX_OSU;
	}

	public static String osuInstanceFromUnit(String id) {
		return id + SUFFIX_OSU_INSTANCE;
	}

}
