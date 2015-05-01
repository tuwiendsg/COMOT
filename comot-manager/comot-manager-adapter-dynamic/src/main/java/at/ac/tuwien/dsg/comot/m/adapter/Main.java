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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Control;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Deployment;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Monitoring;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.enums.EpsEvent;
import at.ac.tuwien.dsg.comot.m.common.enums.Type;
import at.ac.tuwien.dsg.comot.m.common.event.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;

public class Main {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	public static final String SERVICE_INSTANCE_AS_PROPERTY = "service";
	public static final String PROPERTIES_FILE = "./salsa.environment";

	// private static String templateId;
	private static String serviceId;
	private static String participantId;
	private static AnnotationConfigApplicationContext context;
	private static InformationClient info;

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
				info = context.getBean(InformationClient.class);

				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						if (context != null) {
							context.close();
						}
					}
				});

				if (cmd.hasOption("m") || cmd.hasOption("r") || cmd.hasOption("s")) {

					Manager manager = context.getBean(PerInstanceQueueManager.class);

					setServiceInstanceId();
					setParticipantId();

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

					confirmCreation();

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

	private static void setServiceInstanceId() {

		InputStream input = null;
		try {
			input = new FileInputStream(PROPERTIES_FILE);

			Properties prop = new Properties();
			prop.load(input);

			serviceId = prop.getProperty(SERVICE_INSTANCE_AS_PROPERTY);
			LOG.info("service={}", serviceId);

		} catch (IOException e) {
			LOG.error(" {}", e);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					LOG.error("{}", e);
				}
			}
		}

		if (serviceId == null) {
			LOG.error("there is no property '{}'", SERVICE_INSTANCE_AS_PROPERTY);
			throw new IllegalArgumentException("there is no property " + SERVICE_INSTANCE_AS_PROPERTY);
		}

	}

	public static void setParticipantId() throws EpsException {

		participantId = info.getOsuInstanceByServiceId(serviceId).getId();

	}

	public static void confirmCreation() throws Exception {

		RabbitTemplate amqp = context.getBean(RabbitTemplate.class);

		CustomEvent event = new CustomEvent(serviceId, serviceId, EpsEvent.EPS_DYNAMIC_CREATED.toString(),
				participantId, System.currentTimeMillis(), null, null);

		amqp.convertAndSend(Constants.EXCHANGE_REQUESTS,
				serviceId + "." + event.getClass().getSimpleName() + "." + EpsEvent.EPS_DYNAMIC_CREATED + "."
						+ Type.SERVICE,
				Utils.asJsonString(event));

		LOG.info("Success creating adapter '{}' of of service '{}'", participantId, serviceId);
	}

}
