package at.ac.tuwien.dsg.comot.m.ui;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import at.ac.tuwien.dsg.comot.m.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.m.recorder.AppContextServrec;

@Configuration
@Import({ AppContextCore.class, AppContextServrec.class })
@ComponentScan("at.ac.tuwien.dsg.comot.m.ui")
public class AppContextUi {

}
