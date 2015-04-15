package at.ac.tuwien.dsg.comot.m.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({ "classpath:properties/application.properties" })
@Profile(AppContextUi.PRECONFIGURED)
public class AppContextPreconfigured {

}
