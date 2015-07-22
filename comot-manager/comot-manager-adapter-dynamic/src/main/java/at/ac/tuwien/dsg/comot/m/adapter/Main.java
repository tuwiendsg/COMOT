/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.adapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import at.ac.tuwien.dsg.comot.m.adapter.general.EpsAdapterManager;
import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.common.InfoClient;
import at.ac.tuwien.dsg.comot.m.cs.adapter.processor.Control;
import at.ac.tuwien.dsg.comot.m.cs.adapter.processor.Deployment;
import at.ac.tuwien.dsg.comot.m.cs.adapter.processor.Monitoring;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String SERVICE_INSTANCE_AS_PROPERTY = "service";
	public static final String PROPERTIES_FILE = "./salsa.environment";

	private static AnnotationConfigApplicationContext context;

	public static void main(String[] args) {

		Options options = createOptions();

		try {
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("mh") && cmd.hasOption("ih") && cmd.hasOption("ip")) {

				Integer infoPort = null;

				try {
					infoPort = new Integer(cmd.getOptionValue("ip"));
				} catch (NumberFormatException e) {
					LOG.warn("infoPort must be a number");
					showHelp(options);
				}

				AppContextAdapter.setBrokerHost(cmd.getOptionValue("mh"));
				AppContextAdapter.setInfoHost(cmd.getOptionValue("ih"));
				AppContextAdapter.setInfoPort(infoPort);

				context = new AnnotationConfigApplicationContext(AppContextAdapter.class);

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						if (context != null) {
							context.close();
						}
					}
				});

				if (cmd.hasOption("m") || cmd.hasOption("r") || cmd.hasOption("s")) {

					Manager manager = context.getBean(EpsAdapterManager.class);
					String serviceId = getServiceInstanceId();
					String participantId = context.getBean(InfoClient.class)
							.getOsuInstanceByServiceId(serviceId).getId();

					if (cmd.hasOption("m")) {

						Monitoring processor = context.getBean(Monitoring.class);
						manager.start(participantId, processor);

					} else if (cmd.hasOption("r")) {

						Control processor = context.getBean(Control.class);
						manager.start(participantId, processor);

					} else if (cmd.hasOption("s")) {

						Deployment processor = context.getBean(Deployment.class);
						manager.start(participantId, processor);
					}

				} else {
					LOG.warn("No adapter type specified");
					showHelp(options);
				}
			} else {
				LOG.warn("Required parameters were not specified.");
				showHelp(options);
			}
		} catch (Exception e) {
			LOG.error("{}", e);
			showHelp(options);
		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("m", "mela", false, "Start MELA adapter");
		options.addOption("r", "rsybl", false, "Start rSYBL adapter");
		options.addOption("s", "salsa", false, "Start SALSA adapter");
		// options.addOption("id", "serviceInstanceId", true, "The id under which this service indtance is deployed");
		options.addOption("mh", "routerHost", true, "Host of the message router");
		options.addOption("ih", "infoHost", true, "Host of the information service");
		options.addOption("ip", "infoPort", true, "Port of the information service");
		return options;
	}

	private static void showHelp(Options options) {
		HelpFormatter h = new HelpFormatter();
		h.printHelp("java -jar epsAdapter.jar -[m|r] -mh <host> -ih <host> -ip <port>", options);
		throw new IllegalArgumentException();
	}

	private static String getServiceInstanceId() {

		String serviceId = null;

		try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {

			Properties prop = new Properties();
			prop.load(input);

			serviceId = prop.getProperty(SERVICE_INSTANCE_AS_PROPERTY);
			LOG.info("service={}", serviceId);

		} catch (IOException e) {
			LOG.error(" {}", e);
		}

		if (serviceId == null) {
			LOG.error("there is no property '{}'", SERVICE_INSTANCE_AS_PROPERTY);
			throw new IllegalArgumentException("there is no property " + SERVICE_INSTANCE_AS_PROPERTY);
		}

		return serviceId;

	}

}
