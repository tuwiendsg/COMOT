package at.ac.tuwien.dsg.comot.m.ui;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({ "file:config/application.properties" })
@Profile(AppContextUi.CONFIGURABLE)
public class AppContextConfigurable {

}
