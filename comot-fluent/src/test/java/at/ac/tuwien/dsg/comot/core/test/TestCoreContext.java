package at.ac.tuwien.dsg.comot.core.test;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.core.ComotOrchestrator;

@Configuration
// @PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.core" })
public class TestCoreContext {

	@Resource
	public Environment env;

	@Autowired
	protected ComotOrchestrator orchestrator;

}
