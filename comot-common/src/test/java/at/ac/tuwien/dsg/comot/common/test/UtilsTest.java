package at.ac.tuwien.dsg.comot.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilsTest {

	private final static Logger log = LoggerFactory.getLogger(UtilsTest.class);

	public static final String TEST_FILE_BASE = "./../resources/test/";

	public static void sleepInfinit() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void sleepSeconds(int seconds) {
		try {
			log.debug("Waiting {} seconds", seconds);
			Thread.sleep(seconds * 1000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
