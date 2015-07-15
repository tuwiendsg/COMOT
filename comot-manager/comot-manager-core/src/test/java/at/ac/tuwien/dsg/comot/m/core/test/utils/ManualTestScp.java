package at.ac.tuwien.dsg.comot.m.core.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import at.ac.tuwien.dsg.comot.m.common.ConfigConstants;
import at.ac.tuwien.dsg.comot.m.common.ServiceClient;
import at.ac.tuwien.dsg.comot.m.core.UtilsFile;
import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.core.test.AppContextT;
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;

import com.jcraft.jsch.JSchException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AppContextCore.class, AppContextT.class })
@ActiveProfiles({ AppContextServrec.IMPERMANENT_NEO4J_DB })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class ManualTestScp {

	private static final Logger LOG = LoggerFactory.getLogger(ServiceClient.class);

	@javax.annotation.Resource
	public Environment env;

	public static String TEST_FILE = "pom.xml";

	@Test
	public void testFileTransfer() throws IOException, JSchException {
		LOG.info("++++++SCP START");

		String pem = env.getProperty(ConfigConstants.RESOURCE_PATH) + env.getProperty(ConfigConstants.REPO_PEM);
		String user = env.getProperty(ConfigConstants.REPO_USERNAME);
		String host = env.getProperty(ConfigConstants.REPO_HOST);
		String rfile = env.getProperty(ConfigConstants.REPO_PATH) + TEST_FILE;

		UtilsFile.upload(new File(TEST_FILE), host, rfile, user, new File(pem));

		// verify that the text file exists
		URL url = new URL(env.getProperty(ConfigConstants.REPO_URL) + TEST_FILE);
		Scanner s = new Scanner(url.openStream());

		LOG.info("++++++SCP END");

	}
}