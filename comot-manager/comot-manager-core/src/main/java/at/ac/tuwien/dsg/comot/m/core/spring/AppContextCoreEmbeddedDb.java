package at.ac.tuwien.dsg.comot.m.core.spring;

import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

@Configuration
@PropertySource({ "classpath:spring/properties/test.properties" })
@Profile(AppContextCore.EMBEDDED_H2_DB)
public class AppContextCoreEmbeddedDb {

	@Resource
	protected Environment env;

	// this creates a new embedded DB with every new app context (not just data source)
	@Bean
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
	}

	@Bean
	public Properties jpaProperties() {
		return new Properties() {
			private static final long serialVersionUID = -1625799711343021143L;
			{
				setProperty(AppContextCore.HDM2DLL_AUTO, env.getRequiredProperty(AppContextCore.HDM2DLL_AUTO));
				setProperty(AppContextCore.HIBERNATE_DIALECT,
						env.getRequiredProperty(AppContextCore.HIBERNATE_DIALECT));
				setProperty(AppContextCore.HIBERNATE_SHOW_SQL,
						env.getRequiredProperty(AppContextCore.HIBERNATE_SHOW_SQL));
			}
		};
	}

	@Bean(destroyMethod = "stop")
	public Server hTwoServer() throws SQLException {
		return Server.createTcpServer().start();

	}

	@Bean(destroyMethod = "stop")
	public Server hTwoServerWeb() throws SQLException {
		return Server.createWebServer().start();

	}

}
