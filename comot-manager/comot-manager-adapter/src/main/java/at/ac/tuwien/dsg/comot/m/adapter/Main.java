package at.ac.tuwien.dsg.comot.m.adapter;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Control;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Monitoring;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;

import com.rabbitmq.client.ConnectionFactory;

public class Main {

	protected static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		Options options = createOptions();

		try {
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);

			ApplicationContext context;

			if (cmd.hasOption("mh") && cmd.hasOption("ih") && cmd.hasOption("ip") && cmd.hasOption("id")) {

				Integer infoPort = null;

				try {
					infoPort = new Integer(cmd.getOptionValue("ip"));
				} catch (NumberFormatException e) {
					log.warn("infoPort must be a number");
					showHelp(options);
				}

				String id = cmd.getOptionValue("id");
				String infoHost = cmd.getOptionValue("ih");
				String managerHost = cmd.getOptionValue("mp");

				context = new AnnotationConfigApplicationContext(AppContextAdapter.class);
				((ConfigurableApplicationContext) context).close();

				// set info service
				InformationClient info = context.getBean(InformationClient.class);
				info.setBaseUri(UriBuilder.fromUri(info.getBaseUri()).host(infoHost).port(infoPort).build());

				ConnectionFactory rabbit = context.getBean(ConnectionFactory.class);
				rabbit.setHost(managerHost);

				if (cmd.hasOption("m") || cmd.hasOption("r")) {

					Manager manager = context.getBean(PerInstanceQueueManager.class);

					if (cmd.hasOption("m")) {

						Monitoring processor = context.getBean(Monitoring.class);
						manager.start(id, processor);

					} else if (cmd.hasOption("r")) {
						Control processor = context.getBean(Control.class);
						manager.start(id, processor);
					}

				} else {
					log.warn("No adapter type specified");
				}

			} else {
				log.warn("Required parameters were not specified.");
				showHelp(options);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: " + e.getClass() + ", msg: " + e.getLocalizedMessage());
			showHelp(options);

		}
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("m", "mela", false, "Start MELA adapter");
		options.addOption("r", "rsybl", false, "Start rSYBL adapter");
		options.addOption("id", "participantId", false, "Unique identifier in the management system");
		options.addOption("mh", "routerHost", true, "Host of the message router");
		options.addOption("ih", "infoHost", true, "Host of the information service");
		options.addOption("ip", "infoPort", true, "Port of the information service");
		return options;
	}

	private static void showHelp(Options options) {
		HelpFormatter h = new HelpFormatter();
		h.printHelp("java -jar adapter.jar -[m|r] -mh <host> -mp <port> -ih <host> -ip <port> -id <id>", options);
		System.exit(-1);
	}

}
