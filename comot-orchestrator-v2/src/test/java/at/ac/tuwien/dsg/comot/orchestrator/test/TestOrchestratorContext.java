package at.ac.tuwien.dsg.comot.orchestrator.test;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import at.ac.tuwien.dsg.comot.orchestrator.ComotOrchestrator;

@Configuration
// @PropertySource({ "classpath:spring/properties/application.properties" })
@ComponentScan({ "at.ac.tuwien.dsg.comot.orchestrator" })
public class TestOrchestratorContext {

	@Resource
	public Environment env;

	@Autowired
	protected ComotOrchestrator orchestrator;

}
