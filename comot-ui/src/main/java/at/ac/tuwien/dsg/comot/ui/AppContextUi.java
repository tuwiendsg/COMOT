package at.ac.tuwien.dsg.comot.ui;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import at.ac.tuwien.dsg.comot.core.spring.AppContextCore;
import at.ac.tuwien.dsg.comot.servrec.spring.AppContextServrec;

@Configuration
@Import({ AppContextCore.class, AppContextServrec.class })
@ComponentScan("at.ac.tuwien.dsg.comot.ui")
public class AppContextUi {

}
