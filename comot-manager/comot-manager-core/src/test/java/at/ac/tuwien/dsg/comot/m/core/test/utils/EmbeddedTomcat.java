package at.ac.tuwien.dsg.comot.m.core.test.utils;

import java.io.File;
import java.io.IOException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;

public class EmbeddedTomcat {
	private Tomcat tomcat;

	public void start(int port) {
		try {
			tomcat = new Tomcat();
			// If I don't want to copy files around then the base directory must be '.'
			String baseDir = ".";
			tomcat.setPort(port);
			tomcat.setBaseDir(baseDir);
			tomcat.getHost().setAppBase(baseDir);
			tomcat.getHost().setDeployOnStartup(true);
			tomcat.getHost().setAutoDeploy(true);
			tomcat.start();
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {
		try {
			tomcat.stop();
			tomcat.destroy();
			// Tomcat creates a work folder where the temporary files are stored
			FileUtils.deleteDirectory(new File("work"));
		} catch (LifecycleException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deploy(String appName, String path) {
		tomcat.addWebapp(tomcat.getHost(), "/" + appName, path);
	}

	public String getApplicationUrl(String appName) {
		return String.format("http://%s:%d/%s", tomcat.getHost().getName(),
				tomcat.getConnector().getLocalPort(), appName);
	}

	public boolean isRunning() {
		return tomcat != null;
	}
}
