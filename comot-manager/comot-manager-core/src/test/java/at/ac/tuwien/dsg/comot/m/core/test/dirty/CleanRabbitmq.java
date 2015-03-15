package at.ac.tuwien.dsg.comot.m.core.test.dirty;

import java.io.IOException;

import at.ac.tuwien.dsg.comot.m.common.test.UtilsTest;

public class CleanRabbitmq {

	public static void main(String[] args) throws IOException {

		String path = "C:\\Program Files (x86)\\RabbitMQ Server\\rabbitmq_server-3.4.4\\sbin\\rabbitmqctl.bat";

		Runtime.getRuntime().exec(new String[] { path, "stop_app" });
		UtilsTest.sleepSeconds(3);
		Runtime.getRuntime().exec(new String[] { path, "reset" });
		UtilsTest.sleepSeconds(3);
		Runtime.getRuntime().exec(new String[] { path, "start_app" });
		UtilsTest.sleepSeconds(3);

		System.out.println("done");
	}
}
