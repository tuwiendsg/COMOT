package at.ac.tuwien.dsg.comot.core.spring;

import javax.annotation.Resource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oasis.tosca.Definitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.core.dal.ServiceRepo;
import at.ac.tuwien.dsg.comot.core.model.ServiceEntity;
import at.ac.tuwien.dsg.comot.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.cs.mapper.ToscaMapper;
import at.ac.tuwien.dsg.comot.model.structure.CloudService;

@Configuration
@PropertySource({ "classpath:spring/properties/application.properties" })
@Profile(AppContextCore.SPRING_PROFILE_INSERT_DATA)
public class AppContextInsertData {

	public static final Logger log = LoggerFactory.getLogger(AppContextInsertData.class);

	@Resource
	public Environment env;

	@Autowired
	public SalsaClient salsaClient;
	@Autowired
	protected ToscaMapper mapperTosca;
	@Autowired
	protected ServiceRepo serviceRepo;

	@Bean
	public Object insertDeployedInSalsa() {

		try {
			String serviceId;
			String msg = salsaClient.getServices();

			JSONObject services = new JSONObject(msg);
			JSONArray array = services.getJSONArray("services");

			for (int i = 0; i < array.length(); i++) {

				try {

					serviceId = array.getJSONObject(i).getString("serviceId");

					Definitions def = salsaClient.getTosca(serviceId);
					CloudService deployedService = mapperTosca.createModel(def);
					ServiceEntity entity = new ServiceEntity(deployedService, deployedService);

					serviceRepo.save(entity);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	// @DependsOn({"entityManagerFactory", "processEngine", "userMngService"})
	// @Bean
	// public Object dbDataInsert() {
	//
	// // insert data from XML using dbUnit
	// FlatXmlDataSet dataSet = null;
	// try {
	// DatabaseConnection con = new DatabaseConnection(dataSource.getConnection());
	//
	// dataSet = new FlatXmlDataSet(new FlatXmlProducer(new InputSource(new ClassPathResource(
	// env.getRequiredProperty(ApplicationContext.DBUNIT_TESTDATA)).getInputStream())));
	// DatabaseOperation.CLEAN_INSERT.execute(con, dataSet);
	//
	// con.close();
	//
	// log.debug("Test data inserted from {}", env.getRequiredProperty(ApplicationContext.DBUNIT_TESTDATA));
	//
	// } catch (DatabaseUnitException | SQLException | IllegalStateException | IOException e) {
	//
	// log.error("Failed to insert data to DB using DbUnit. " + e.getLocalizedMessage());
	// e.printStackTrace();
	//
	// }
	//
	// return dataSet;
	// }

}
