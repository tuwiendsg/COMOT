package at.ac.tuwien.dsg.comot.m.adapter;

import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import at.ac.tuwien.dsg.comot.m.adapter.general.Manager;
import at.ac.tuwien.dsg.comot.m.adapter.general.PerInstanceQueueManager;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Control;
import at.ac.tuwien.dsg.comot.m.adapter.processor.Monitoring;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.events.StateMessage;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

public class Main {

	protected static Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		Options options = createOptions();

		try {
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);

			AnnotationConfigApplicationContext context;

			if (cmd.hasOption("mh") && cmd.hasOption("ih") && cmd.hasOption("ip")) {

				Integer infoPort = null;

				try {
					infoPort = new Integer(cmd.getOptionValue("ip"));
				} catch (NumberFormatException e) {
					log.warn("infoPort must be a number");
					showHelp(options);
				}

				AppContextAdapter.setBrokerHost(cmd.getOptionValue("mh"));
				AppContextAdapter.setInfoHost(cmd.getOptionValue("ih"));
				AppContextAdapter.setInfoPort(infoPort);

				context = new AnnotationConfigApplicationContext(AppContextAdapter.class);

				if (cmd.hasOption("m") || cmd.hasOption("r")) {

					Manager manager = context.getBean(PerInstanceQueueManager.class);

					String osuId = null;
					String id = null;// = UUID.randomUUID().toString();//cmd.getOptionValue("id");

					if (cmd.hasOption("m")) {

						osuId = Constants.MELA_SERVICE_DYNAMIC;
						id = osuId + "_" + UUID.randomUUID().toString();

						Monitoring processor = context.getBean(Monitoring.class);
						manager.start(id, processor);

					} else if (cmd.hasOption("r")) {

						osuId = Constants.RSYBL_SERVICE_DYNAMIC;
						id = osuId + "_" + UUID.randomUUID().toString();

						Control processor = context.getBean(Control.class);
						manager.start(id, processor);
					}

					initProcedure(id, osuId, context);

				} else {
					log.warn("No adapter type specified");
					showHelp(options);
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

		// ((ConfigurableApplicationContext) context).close();
	}

	private static Options createOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "print help");
		options.addOption("m", "mela", false, "Start MELA adapter");
		options.addOption("r", "rsybl", false, "Start rSYBL adapter");
		// options.addOption("id", "participantId", true, "Unique identifier in the management system");
		options.addOption("mh", "routerHost", true, "Host of the message router");
		options.addOption("ih", "infoHost", true, "Host of the information service");
		options.addOption("ip", "infoPort", true, "Port of the information service");
		return options;
	}

	private static void showHelp(Options options) {
		HelpFormatter h = new HelpFormatter();
		System.out.println("dead");
		h.printHelp("java -jar epsAdapter.jar -[m|r] -mh <host> -ih <host> -ip <port> -id <id>", options);
		System.exit(-1);
	}

	public static void initProcedure(String id, String osuId, ApplicationContext context) throws Exception {

		InformationClient info = context.getBean(InformationClient.class);
		RabbitTemplate amqp = context.getBean(RabbitTemplate.class);

		OfferedServiceUnit osu = info.getOsu(osuId);
		if (osu == null) {
			throw new Exception("The information service does not contain OfferedServiceUnit with id=" + osuId);
		}

		CustomEvent event = new CustomEvent(null, null, null, EpsAction.EPS_DYNAMIC_CREATED.toString(),
				id, Constants.EPS_BUILDER, osuId);
		StateMessage msg = new StateMessage(event, null, null);

		amqp.convertAndSend(Constants.EXCHANGE_DYNAMIC_REGISTRATION, osuId, Utils.asJsonString(msg));

		log.info("Success creating adapter '{}' of type {}", id, osuId);
	}

}
