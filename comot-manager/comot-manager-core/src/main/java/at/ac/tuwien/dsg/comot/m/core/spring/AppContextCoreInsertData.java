package at.ac.tuwien.dsg.comot.m.core.spring;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.m.cs.connector.SalsaClient;
import at.ac.tuwien.dsg.comot.m.cs.mapper.ToscaMapper;

@Configuration
@PropertySource({ "classpath:spring/properties/application.properties" })
@Profile(AppContextCore.INSERT_INIT_DATA)
public class AppContextCoreInsertData {

	public static final Logger log = LoggerFactory.getLogger(AppContextCoreInsertData.class);

	@Resource
	public Environment env;

	@Autowired
	public SalsaClient salsaClient;
	@Autowired
	protected ToscaMapper mapperTosca;

	@Bean
	public Object insertDeployedInSalsa() {

		// try {
		//
		// serviceRepo.setFake(true); // XXX remove to use DB
		//
		// String serviceId;
		// String msg = salsaClient.getServices();
		//
		// JSONObject services = new JSONObject(msg);
		// JSONArray array = services.getJSONArray("services");
		//
		// for (int i = 0; i < array.length(); i++) {
		//
		// try {
		//
		// serviceId = array.getJSONObject(i).getString("serviceId");
		//
		// Definitions def = salsaClient.getTosca(serviceId);
		// CloudService deployedService = mapperTosca.createModel(def);
		// ServiceEntity entity = new ServiceEntity(deployedService);
		//
		// serviceRepo.save(entity);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		return null;
	}

}
