package at.ac.tuwien.dsg.comot.m.ui;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;

@Configuration
@EnableAsync
@Import({ AppContextCore.class, AppContextServrec.class })
@ComponentScan("at.ac.tuwien.dsg.comot.m.ui")
public class AppContextUi {

	public static final String CONFIGURABLE = "CONFIGURABLE";
	public static final String PRECONFIGURED = "PRECONFIGURED";

}
