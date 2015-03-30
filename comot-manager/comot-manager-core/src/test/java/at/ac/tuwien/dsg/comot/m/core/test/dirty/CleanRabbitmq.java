package at.ac.tuwien.dsg.comot.m.core.test.dirty;

import java.io.IOException;

public class CleanRabbitmq {

	/**
	 * Deletes also configuration such as users.
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException, InterruptedException {

		String path = "C:\\Program Files (x86)\\RabbitMQ Server\\rabbitmq_server-3.4.4\\sbin\\rabbitmqctl.bat";

		System.out.println("stop_app");
		Runtime.getRuntime().exec(new String[] { path, "stop_app" }).waitFor();

		System.out.println("reset");
		Runtime.getRuntime().exec(new String[] { path, "reset" }).waitFor();

		System.out.println("start_app");
		Runtime.getRuntime().exec(new String[] { path, "start_app" }).waitFor();

		System.out.println("done");
	}
}
