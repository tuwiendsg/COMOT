package at.ac.tuwien.dsg.comot.common.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * @author omoser
 */
public final class Markers {

	public static final Marker API = MarkerFactory.getMarker("API");

	public static final Marker CORE = MarkerFactory.getMarker("CORE");

	public static final Marker CLIENT = MarkerFactory.getMarker("CLIENT");

}
