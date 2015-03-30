package at.ac.tuwien.dsg.comot.m.adapter;

import java.util.Map;

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
import at.ac.tuwien.dsg.comot.m.adapter.processor.Monitoring;
import at.ac.tuwien.dsg.comot.m.common.Constants;
import at.ac.tuwien.dsg.comot.m.common.EpsAction;
import at.ac.tuwien.dsg.comot.m.common.InformationClient;
import at.ac.tuwien.dsg.comot.m.common.Type;
import at.ac.tuwien.dsg.comot.m.common.Utils;
import at.ac.tuwien.dsg.comot.m.common.events.CustomEvent;
import at.ac.tuwien.dsg.comot.m.common.exception.EpsException;
import at.ac.tuwien.dsg.comot.model.provider.OfferedServiceUnit;

public class Main {

	protected static Logger log = LoggerFactory.getLogger(Main.class);

	public static final String SERVICE_INSTANCE_AS_PROPERTY = "service";
	private static String serviceId;
	private static String instanceId;
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
					log.warn("infoPort must be a number");
					showHelp(options);
				}

				AppContextAdapter.setBrokerHost(cmd.getOptionValue("mh"));
				AppContextAdapter.setInfoHost(cmd.getOptionValue("ih"));
				AppContextAdapter.setInfoPort(infoPort);

				context = new AnnotationConfigApplicationContext(AppContextAdapter.class);
				info = context.getBean(InformationClient.class);

				if (cmd.hasOption("m") || cmd.hasOption("r")) {

					Manager manager = context.getBean(PerInstanceQueueManager.class);

					setServiceInstanceId();
					setParticipantId();

					if (cmd.hasOption("m")) {

						Monitoring processor = context.getBean(Monitoring.class);
						manager.start(participantId, processor);

					} else if (cmd.hasOption("r")) {

						Control processor = context.getBean(Control.class);
						manager.start(participantId, processor);
					}

					confirmCreation();

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
		h.printHelp("java -jar epsAdapter.jar -[m|r] -mh <host> -ih <host> -ip <port> -id <id>", options);
		System.exit(-1);
	}

	private static void setServiceInstanceId() {

		instanceId = System.getProperty(SERVICE_INSTANCE_AS_PROPERTY);

		if (instanceId == null) {
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				if (SERVICE_INSTANCE_AS_PROPERTY.equals(envName)) {
					instanceId = env.get(envName);
					break;
				}
			}

		}
		if (instanceId == null) {
			log.error("there is no System.getProperty or System.getenv() '{}'", SERVICE_INSTANCE_AS_PROPERTY);
			System.exit(-1);
		}

	}

	public static void setParticipantId() throws EpsException {

		serviceId = info.getServiceInstance(instanceId).getId();
		OfferedServiceUnit osu = info.getOsuByServiceId(serviceId);
		participantId = info.createOsuInstance(osu.getId());
		info.createOsuInstanceDynamic(osu.getId(), instanceId, participantId);

	}

	public static void confirmCreation() throws Exception {

		RabbitTemplate amqp = context.getBean(RabbitTemplate.class);

		CustomEvent event = new CustomEvent(serviceId, instanceId, serviceId, EpsAction.EPS_DYNAMIC_CREATED.toString(),
				participantId, System.currentTimeMillis(), null, null);

		amqp.convertAndSend(Constants.EXCHANGE_CUSTOM_EVENT,
				instanceId + "." + null + "." + EpsAction.EPS_DYNAMIC_CREATED + "." + Type.SERVICE,
				Utils.asJsonString(event));

		log.info("Success creating adapter '{}' of serviceType {}", participantId, serviceId);
	}

}
