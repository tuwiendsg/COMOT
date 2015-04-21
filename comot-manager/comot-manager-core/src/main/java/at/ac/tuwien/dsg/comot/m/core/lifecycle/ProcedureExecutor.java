package at.ac.tuwien.dsg.comot.m.core.lifecycle;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ProcedureExecutor {
	
	ManagerOfServiceInstance manager;
	GroupManager groupManager;
	
	@Async
	public void preemptiveStopController() throws InterruptedException{
		
		Thread.sleep(5000);
		
		
		
		
	}

}
